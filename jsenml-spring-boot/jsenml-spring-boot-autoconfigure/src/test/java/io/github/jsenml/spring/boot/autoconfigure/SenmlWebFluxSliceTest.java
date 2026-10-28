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

package io.github.jsenml.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.json.SenmlJson;
import io.github.jsenml.json.SenmlJsonCodec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
class SenmlWebFluxSliceTest {

  private static final SenmlPack PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("Cel")
              .value(23.1)
              .build());

  @Autowired private WebTestClient client;

  @Test
  void givenWebFluxTestSlice_whenPostingSenmlJsonBody_thenReadsAndWritesSenmlJson() {
    MediaType type = MediaType.parseMediaType(SenmlJsonCodec.MEDIA_TYPE);
    byte[] body = SenmlJson.toJsonBytes(PACK);

    byte[] response =
        client
            .post()
            .uri(SenmlPackController.PATH)
            .contentType(type)
            .accept(type)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(type)
            .expectBody(byte[].class)
            .returnResult()
            .getResponseBody();

    assertThat(response).isEqualTo(body);
  }

  @Configuration
  @Import(SenmlPackController.class)
  static class TestConfiguration {}
}
