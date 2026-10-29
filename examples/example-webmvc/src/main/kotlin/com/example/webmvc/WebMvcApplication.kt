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

package com.example.webmvc

import io.github.jsenml.SenmlPack
import io.github.jsenml.SenmlRecord
import io.github.jsenml.SenmlViolationException
import io.github.jsenml.cbor.SenmlCborCodec
import io.github.jsenml.json.SenmlJsonCodec
import io.github.jsenml.kotlin.toSenmlPack
import io.github.jsenml.xml.SenmlXmlCodec
import io.swagger.v3.oas.annotations.Hidden
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

fun main(args: Array<String>) {
  runApplication<WebMvcApplication>(*args)
}

/** Example application accepting SenML packs in JSON, CBOR and XML. */
@SpringBootApplication //
class WebMvcApplication

/** In-memory store of resolved records received in all packs, whatever their format. */
@Component
class SenmlStore {

  private val records = mutableListOf<SenmlRecord>()

  @Synchronized
  fun add(pack: SenmlPack) {
    records += pack.resolve()
  }

  @Synchronized //
  fun all(): SenmlPack = records.toSenmlPack()

  @Synchronized //
  fun clear() = records.clear()
}

/**
 * Accepts SenML packs in JSON, CBOR and XML, each with its own handler, returns all stored records
 * in the format requested by `Accept` (SenML JSON by default), whatever format they were received
 * in, or clears them.
 */
@RestController
@RequestMapping("/packs")
class SenmlController(private val store: SenmlStore) {

  private val log = LoggerFactory.getLogger(javaClass)

  @PostMapping(consumes = [SenmlJsonCodec.MEDIA_TYPE])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun addJson(@RequestBody pack: SenmlPack) = add(pack, "JSON")

  @PostMapping(consumes = [SenmlCborCodec.MEDIA_TYPE])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun addCbor(@RequestBody pack: SenmlPack) = add(pack, "CBOR")

  @PostMapping(consumes = [SenmlXmlCodec.MEDIA_TYPE])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun addXml(@RequestBody pack: SenmlPack) = add(pack, "XML")

  @GetMapping(
      produces = [SenmlJsonCodec.MEDIA_TYPE, SenmlCborCodec.MEDIA_TYPE, SenmlXmlCodec.MEDIA_TYPE],
  )
  fun all(): SenmlPack {
    val pack = store.all()
    log.info("Returning {} records", pack.size())
    return pack
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun clear() {
    store.clear()
    log.info("Cleared all records")
  }

  @ExceptionHandler(SenmlViolationException::class)
  fun handleViolations(e: SenmlViolationException): ProblemDetail {
    log.warn("Rejected pack: {}", e.message)
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)
  }

  private fun add(pack: SenmlPack, format: String) {
    log.info("Received {} records as SenML {}", pack.size(), format)
    store.add(pack)
  }
}

/** Redirects the index page to Swagger UI. */
@Hidden
@Controller
class IndexController {

  @GetMapping("/") //
  fun index() = "redirect:/swagger-ui/index.html"
}
