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

package io.github.jsenml.spring.boot.autoconfigure.http.codec;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.json.SenmlJson;
import io.github.jsenml.json.SenmlJsonCodec;
import io.github.jsenml.spring.boot.autoconfigure.SenmlPackController;
import io.github.jsenml.spring.boot.autoconfigure.jackson.SenmlJacksonAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.codec.autoconfigure.CodecsAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

class SenmlJsonWebFluxTest {

  private static final SenmlPack PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("Cel")
              .value(23.1)
              .build());

  private final ReactiveWebApplicationContextRunner contextRunner =
      new ReactiveWebApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(CodecsAutoConfiguration.class, WebFluxAutoConfiguration.class))
          .withUserConfiguration(SenmlPackController.class);

  @Test
  void givenJackson_whenPostingSenmlJsonBody_thenReadsAndWritesSenmlJson() {
    contextRunner
        .withConfiguration(
            AutoConfigurations.of(
                JacksonAutoConfiguration.class, SenmlJacksonAutoConfiguration.class))
        .run(this::assertEcho);
  }

  private void assertEcho(ApplicationContext context) {
    MediaType type = MediaType.parseMediaType(SenmlJsonCodec.MEDIA_TYPE);
    byte[] body = SenmlJson.toJsonBytes(PACK);

    byte[] response =
        WebTestClient.bindToApplicationContext(context)
            .build()
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
}
