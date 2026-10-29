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

package com.example.producer

import io.github.jsenml.SenmlPack
import io.github.jsenml.cbor.SenmlCbor
import io.github.jsenml.cbor.SenmlCborCodec
import io.github.jsenml.json.SenmlJson
import io.github.jsenml.json.SenmlJsonCodec
import io.github.jsenml.kotlin.buildSenmlPack
import io.github.jsenml.xml.SenmlXml
import io.github.jsenml.xml.SenmlXmlCodec
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.system.exitProcess

/**
 * Produces packs of random measurements of a simulated device and sends them to an example
 * application, e.g.:
 * ```
 * ./gradlew :examples:example-producer:run --args="cbor --count=5"
 * ```
 */
fun main(args: Array<String>) {
  val options =
      try {
        ProducerOptions.parse(args)
      } catch (e: IllegalArgumentException) {
        System.err.println(e.message)
        System.err.println(ProducerOptions.USAGE)
        exitProcess(1)
      }
  val device = "urn:dev:producer:%04x:".format(Random.nextInt(0x10000))
  HttpClient.newHttpClient().use { client ->
    repeat(options.count) { index ->
      val pack = randomPack(device, Instant.now(), Random)
      val request =
          HttpRequest.newBuilder(options.url)
              .header("Content-Type", options.format.mediaType)
              .POST(HttpRequest.BodyPublishers.ofByteArray(options.format.encode(pack)))
              .build()
      val response = client.send(request, HttpResponse.BodyHandlers.ofString())
      println("#${index + 1} ${options.format} ${response.statusCode()} $pack")
      if (index < options.count - 1) {
        Thread.sleep(options.interval)
      }
    }
  }
}

/** Creates a pack of random temperature, humidity and door state of given device. */
fun randomPack(device: String, now: Instant, random: Random): SenmlPack = buildSenmlPack {
  record {
    baseName(device)
    baseTime(now.epochSecond)
    name("temperature")
    unit("Cel")
    value(random.nextDouble(18.0, 26.0).roundToTenths())
  }
  record {
    name("humidity")
    unit("%RH")
    value(random.nextDouble(30.0, 60.0).roundToTenths())
  }
  record {
    name("door")
    booleanValue(random.nextBoolean())
  }
}

private fun Double.roundToTenths(): Double = (this * 10).roundToInt() / 10.0

/** Command line options of the producer. */
data class ProducerOptions(
    val format: SenmlFormat,
    val url: URI = URI.create("http://localhost:8080/packs"),
    val count: Int = 10,
    val interval: Duration = Duration.ofSeconds(1),
) {

  companion object {
    const val USAGE =
        "Usage: <json|cbor|xml> [--url=http://localhost:8080/packs] [--count=10] [--interval-ms=1000]"

    /** Parses command line arguments: the format, followed by optional `--name=value` options. */
    fun parse(args: Array<String>): ProducerOptions {
      val format = args.firstOrNull { !it.startsWith("--") }
      require(format != null) { "Missing format" }
      var options = ProducerOptions(SenmlFormat.of(format))
      for (arg in args.filter { it.startsWith("--") }) {
        val (name, value) =
            arg.removePrefix("--").split("=", limit = 2).let { it[0] to it.getOrNull(1) }
        require(value != null) { "Missing value of option '$arg'" }
        options =
            when (name) {
              "url" -> options.copy(url = URI.create(value))
              "count" -> options.copy(count = value.toInt())
              "interval-ms" -> options.copy(interval = Duration.ofMillis(value.toLong()))
              else -> throw IllegalArgumentException("Unknown option '$arg'")
            }
      }
      return options
    }
  }
}

/** SenML representation packs are sent in, with its media type and encoder. */
enum class SenmlFormat(val mediaType: String, val encode: (SenmlPack) -> ByteArray) {
  JSON(SenmlJsonCodec.MEDIA_TYPE, SenmlJson::toJsonBytes),
  CBOR(SenmlCborCodec.MEDIA_TYPE, SenmlCbor::toCbor),
  XML(SenmlXmlCodec.MEDIA_TYPE, SenmlXml::toXmlBytes);

  companion object {
    /** Returns the format of given case-insensitive name, e.g. `json`. */
    fun of(name: String): SenmlFormat =
        entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: throw IllegalArgumentException(
                "Unknown format '$name', expected one of ${entries.map { it.name.lowercase() }}",
            )
  }
}
