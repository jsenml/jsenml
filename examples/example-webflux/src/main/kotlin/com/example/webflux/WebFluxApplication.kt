/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.webflux

import io.github.jsenml.SenmlPack
import io.github.jsenml.SenmlRecord
import io.github.jsenml.SenmlViolationException
import io.github.jsenml.cbor.SenmlCborCodec
import io.github.jsenml.json.SenmlJsonCodec
import io.github.jsenml.kotlin.toSenmlPack
import io.github.jsenml.xml.SenmlXmlCodec
import io.swagger.v3.oas.annotations.Hidden
import java.net.URI
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.http.codec.CodecCustomizer
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.cbor.JacksonCborDecoder
import org.springframework.http.codec.cbor.JacksonCborEncoder
import org.springframework.http.codec.xml.JacksonXmlDecoder
import org.springframework.http.codec.xml.JacksonXmlEncoder
import org.springframework.stereotype.Component
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tools.jackson.dataformat.cbor.CBORMapper
import tools.jackson.dataformat.xml.XmlMapper

fun main(args: Array<String>) {
  runApplication<WebFluxApplication>(*args)
}

/** Example reactive application accepting SenML packs in JSON, CBOR and XML. */
@SpringBootApplication
class WebFluxApplication {

  /**
   * Reads and writes SenML CBOR and XML with Boot's [CBORMapper] and [XmlMapper], which have SenML
   * support registered by jsenml autoconfiguration. Spring Boot passes its mapper to the JSON
   * codecs only, which support `application/senml+json` as a `+json` media type, while the CBOR and
   * XML codecs get new mappers, and the CBOR ones support `application/cbor` only.
   */
  @Bean
  fun senmlCodecCustomizer(cborMapper: CBORMapper, xmlMapper: XmlMapper) = CodecCustomizer {
    val defaults = it.defaultCodecs()
    val senmlCbor = MediaType.parseMediaType(SenmlCborCodec.MEDIA_TYPE)
    defaults.jacksonCborDecoder(
        JacksonCborDecoder(cborMapper, MediaType.APPLICATION_CBOR, senmlCbor),
    )
    defaults.jacksonCborEncoder(
        SingleValueCborEncoder(cborMapper, MediaType.APPLICATION_CBOR, senmlCbor),
    )
    defaults.jacksonXmlDecoder(JacksonXmlDecoder(xmlMapper))
    defaults.jacksonXmlEncoder(SingleValueXmlEncoder(xmlMapper))
  }
}

/**
 * [JacksonCborEncoder] rejects every `encode` call as unsupported stream encoding, including single
 * values (e.g. response bodies) that WebFlux passes as [Mono], so they are encoded with
 * `encodeValue`.
 */
class SingleValueCborEncoder(mapper: CBORMapper, vararg mimeTypes: MimeType) :
    JacksonCborEncoder(mapper, *mimeTypes) {

  override fun encode(
      inputStream: Publisher<*>,
      bufferFactory: DataBufferFactory,
      elementType: ResolvableType,
      mimeType: MimeType?,
      hints: Map<String, Any>?,
  ): Flux<DataBuffer> =
      if (inputStream is Mono<*>) {
        inputStream
            .cast(Any::class.java)
            .map { encodeValue(it, bufferFactory, elementType, mimeType, hints) }
            .flux()
      } else {
        super.encode(inputStream, bufferFactory, elementType, mimeType, hints)
      }
}

/** [JacksonXmlEncoder] counterpart of [SingleValueCborEncoder]. */
class SingleValueXmlEncoder(mapper: XmlMapper) : JacksonXmlEncoder(mapper) {

  override fun encode(
      inputStream: Publisher<*>,
      bufferFactory: DataBufferFactory,
      elementType: ResolvableType,
      mimeType: MimeType?,
      hints: Map<String, Any>?,
  ): Flux<DataBuffer> =
      if (inputStream is Mono<*>) {
        inputStream
            .cast(Any::class.java)
            .map { encodeValue(it, bufferFactory, elementType, mimeType, hints) }
            .flux()
      } else {
        super.encode(inputStream, bufferFactory, elementType, mimeType, hints)
      }
}

/**
 * In-memory store of resolved records received in all packs, whatever their format. Guarded by a
 * [Mutex], which suspends instead of blocking event loop threads.
 */
@Component
class SenmlStore {

  private val mutex = Mutex()
  private val records = mutableListOf<SenmlRecord>()

  suspend fun add(pack: SenmlPack) {
    mutex.withLock { records += pack.resolve() }
  }

  suspend fun all(): SenmlPack = mutex.withLock { records.toSenmlPack() }

  suspend fun clear() {
    mutex.withLock { records.clear() }
  }
}

/**
 * Accepts SenML packs in JSON, CBOR and XML, each with its own suspending handler, returns all
 * stored records in the format requested by `Accept` (SenML JSON by default), whatever format they
 * were received in, or clears them.
 */
@RestController
@RequestMapping("/packs")
class SenmlController(private val store: SenmlStore) {

  private val log = LoggerFactory.getLogger(javaClass)

  @PostMapping(consumes = [SenmlJsonCodec.MEDIA_TYPE])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  suspend fun addJson(@RequestBody pack: SenmlPack) = add(pack, "JSON")

  @PostMapping(consumes = [SenmlCborCodec.MEDIA_TYPE])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  suspend fun addCbor(@RequestBody pack: SenmlPack) = add(pack, "CBOR")

  @PostMapping(consumes = [SenmlXmlCodec.MEDIA_TYPE])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  suspend fun addXml(@RequestBody pack: SenmlPack) = add(pack, "XML")

  @GetMapping(
      produces = [SenmlJsonCodec.MEDIA_TYPE, SenmlCborCodec.MEDIA_TYPE, SenmlXmlCodec.MEDIA_TYPE],
  )
  suspend fun all(): SenmlPack {
    val pack = store.all()
    log.info("Returning {} records", pack.size())
    return pack
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  suspend fun clear() {
    store.clear()
    log.info("Cleared all records")
  }

  @ExceptionHandler(SenmlViolationException::class)
  fun handleViolations(e: SenmlViolationException): ProblemDetail {
    log.warn("Rejected pack: {}", e.message)
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)
  }

  private suspend fun add(pack: SenmlPack, format: String) {
    log.info("Received {} records as SenML {}", pack.size(), format)
    store.add(pack)
  }
}

/**
 * Redirects the index page to Swagger UI. WebFlux resolves no `redirect:` view names without a
 * template engine, so the redirect is built as a response entity.
 */
@Hidden
@RestController
class IndexController {

  @GetMapping("/")
  fun index(): ResponseEntity<Unit> =
      ResponseEntity.status(HttpStatus.FOUND).location(URI.create(SWAGGER_UI)).build()

  companion object {
    const val SWAGGER_UI = "/swagger-ui.html"
  }
}
