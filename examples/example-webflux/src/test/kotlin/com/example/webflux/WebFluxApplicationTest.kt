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

import io.github.jsenml.cbor.SenmlCborCodec
import io.github.jsenml.cbor.jackson.JacksonSenmlCborCodec
import io.github.jsenml.json.SenmlJsonCodec
import io.github.jsenml.kotlin.buildSenmlPack
import io.github.jsenml.xml.SenmlXmlCodec
import io.github.jsenml.xml.jackson.JacksonSenmlXmlCodec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WebFluxApplicationTest(@Autowired context: ApplicationContext) {

  private val client = WebTestClient.bindToApplicationContext(context).build()

  @Test
  fun `given packs in every format when posting them then get returns all records as json`() {
    val cbor =
        JacksonSenmlCborCodec()
            .encode(
                buildSenmlPack {
                  record {
                    baseName("urn:dev:example:")
                    baseTime(1700000000)
                    name("humidity")
                    unit("%RH")
                    value(40)
                  }
                },
            )

    post(
        SenmlJsonCodec.MEDIA_TYPE,
        """[{"bn":"urn:dev:example:","bt":1700000000,"n":"temp","u":"Cel","v":21.5}]""",
    )
    post(SenmlCborCodec.MEDIA_TYPE, cbor)
    post(
        SenmlXmlCodec.MEDIA_TYPE,
        """<sensml xmlns="urn:ietf:params:xml:ns:senml">""" +
            """<senml bn="urn:dev:example:" bt="1700000000" n="open" vb="true"/></sensml>""",
    )

    client
        .get()
        .uri("/packs")
        .accept(SENML_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentTypeCompatibleWith(SENML_JSON)
        .expectBody()
        .json(
            """
            [
              {"n":"urn:dev:example:temp","u":"Cel","t":1700000000,"v":21.5},
              {"n":"urn:dev:example:humidity","u":"%RH","t":1700000000,"v":40},
              {"n":"urn:dev:example:open","t":1700000000,"vb":true}
            ]
            """,
        )
  }

  @Test
  fun `given stored pack when getting it as cbor and xml then returns its records`() {
    post(SenmlJsonCodec.MEDIA_TYPE, """[{"n":"urn:dev:example:temp","t":1700000000,"v":21.5}]""")
    val expected = buildSenmlPack {
      record {
        name("urn:dev:example:temp")
        time(1700000000)
        value(21.5)
      }
    }

    assertThat(JacksonSenmlCborCodec().decode(get(SenmlCborCodec.MEDIA_TYPE))).isEqualTo(expected)
    assertThat(JacksonSenmlXmlCodec().decode(get(SenmlXmlCodec.MEDIA_TYPE))).isEqualTo(expected)
  }

  @Test
  fun `given stored pack when deleting then get returns no records`() {
    post(SenmlJsonCodec.MEDIA_TYPE, """[{"n":"urn:dev:example:temp","t":1700000000,"v":21.5}]""")

    client.delete().uri("/packs").exchange().expectStatus().isNoContent()

    client
        .get()
        .uri("/packs")
        .accept(SENML_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("[]")
  }

  @Test
  fun `given pack without name when posting then rejects it`() {
    client
        .post()
        .uri("/packs")
        .contentType(SENML_JSON)
        .bodyValue("""[{"v":1}]""")
        .exchange()
        .expectStatus()
        .isBadRequest()
  }

  @Test
  fun `given index when getting then redirects to swagger ui`() {
    client
        .get()
        .uri("/")
        .exchange()
        .expectStatus()
        .isFound()
        .expectHeader()
        .location(IndexController.SWAGGER_UI)
  }

  @Test
  fun `given swagger ui path when getting then redirects to swagger ui resources`() {
    client.get().uri(IndexController.SWAGGER_UI).exchange().expectStatus().is3xxRedirection()
  }

  @Test
  fun `given invalid data value when posting then rejects it as bad request`() {
    client
        .post()
        .uri("/packs")
        .contentType(SENML_JSON)
        .bodyValue("""[{"n":"a","t":1700000000,"vd":"!!!"}]""")
        .exchange()
        .expectStatus()
        .isBadRequest()
  }

  @Test
  fun `given springdoc when getting api docs then describes senml schemas`() {
    client
        .get()
        .uri("/v3/api-docs")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.components.schemas.SenmlPack.items")
        .exists()
  }

  private fun get(mediaType: String): ByteArray {
    val type = MediaType.parseMediaType(mediaType)
    return client
        .get()
        .uri("/packs")
        .accept(type)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentTypeCompatibleWith(type)
        .expectBody(ByteArray::class.java)
        .returnResult()
        .responseBody!!
  }

  private fun post(mediaType: String, body: Any) {
    client
        .post()
        .uri("/packs")
        .contentType(MediaType.parseMediaType(mediaType))
        .bodyValue(body)
        .exchange()
        .expectStatus()
        .isNoContent()
  }

  companion object {
    private val SENML_JSON = MediaType.parseMediaType(SenmlJsonCodec.MEDIA_TYPE)
  }
}
