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

import io.github.jsenml.json.SenmlJson
import java.net.URI
import java.time.Duration
import java.time.Instant
import kotlin.random.Random
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ProducerTest {

  @Test
  fun `given format only when parsing options then uses defaults`() {
    assertThat(ProducerOptions.parse(arrayOf("CBOR")))
        .isEqualTo(
            ProducerOptions(
                SenmlFormat.CBOR,
                URI.create("http://localhost:8080/packs"),
                10,
                Duration.ofSeconds(1),
            ),
        )
  }

  @Test
  fun `given all options when parsing options then reads them`() {
    val options =
        ProducerOptions.parse(
            arrayOf("--url=http://example.com/packs", "cbor", "--count=3", "--interval-ms=50"),
        )

    assertThat(options)
        .isEqualTo(
            ProducerOptions(
                SenmlFormat.CBOR,
                URI.create("http://example.com/packs"),
                3,
                Duration.ofMillis(50),
            ),
        )
  }

  @Test
  fun `given missing or unknown format when parsing options then throws`() {
    assertThatThrownBy { ProducerOptions.parse(arrayOf("--count=3")) }
        .isInstanceOf(IllegalArgumentException::class.java)
    assertThatThrownBy { ProducerOptions.parse(arrayOf("yaml")) }
        .isInstanceOf(IllegalArgumentException::class.java)
    assertThatThrownBy { ProducerOptions.parse(arrayOf("json", "--speed=1")) }
        .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given random pack when resolving then records are valid and absolute`() {
    val now = Instant.ofEpochSecond(1700000000)

    val resolved = randomPack("urn:dev:producer:0001:", now, Random(42)).resolve()

    assertThat(resolved.map { it.name })
        .containsExactly(
            "urn:dev:producer:0001:temperature",
            "urn:dev:producer:0001:humidity",
            "urn:dev:producer:0001:door",
        )
    assertThat(resolved.map { it.time }).containsOnly(1.7e9)
  }

  @ParameterizedTest
  @EnumSource(SenmlFormat::class)
  fun `given random pack when encoding in every format then round trips through json`(
      format: SenmlFormat,
  ) {
    val pack = randomPack("urn:dev:producer:0001:", Instant.now(), Random(42))

    assertThat(format.encode(pack)).isNotEmpty()
    assertThat(SenmlJson.fromJson(SenmlJson.toJson(pack))).isEqualTo(pack)
  }
}
