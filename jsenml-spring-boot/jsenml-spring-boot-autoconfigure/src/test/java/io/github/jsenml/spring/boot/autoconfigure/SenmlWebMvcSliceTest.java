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
import io.github.jsenml.cbor.SenmlCbor;
import io.github.jsenml.cbor.SenmlCborCodec;
import io.github.jsenml.json.SenmlJson;
import io.github.jsenml.json.SenmlJsonCodec;
import io.github.jsenml.xml.SenmlXml;
import io.github.jsenml.xml.SenmlXmlCodec;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest
class SenmlWebMvcSliceTest {

  private static final SenmlPack PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("Cel")
              .value(23.1)
              .build());

  @Autowired private MockMvc mockMvc;

  static Stream<Arguments> representations() {
    return Stream.of(
        Arguments.of(SenmlJsonCodec.MEDIA_TYPE, SenmlJson.toJsonBytes(PACK)),
        Arguments.of(SenmlCborCodec.MEDIA_TYPE, SenmlCbor.toCbor(PACK)),
        Arguments.of(SenmlXmlCodec.MEDIA_TYPE, SenmlXml.toXmlBytes(PACK)));
  }

  @ParameterizedTest
  @MethodSource("representations")
  void givenWebMvcTestSlice_whenPostingSenmlBody_thenReadsAndWritesSenml(
      String mediaType, byte[] body) {
    MediaType type = MediaType.parseMediaType(mediaType);

    byte[] response =
        RestTestClient.bindTo(mockMvc)
            .build()
            .post()
            .uri(SenmlPackController.PATH)
            .contentType(type)
            .accept(type)
            .body(body)
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
