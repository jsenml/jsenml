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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.cbor.SenmlCbor;
import io.github.jsenml.cbor.SenmlCborCodec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@RestClientTest
class SenmlRestClientSliceTest {

  private static final MediaType SENML_CBOR = MediaType.parseMediaType(SenmlCborCodec.MEDIA_TYPE);

  private static final SenmlPack PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("Cel")
              .value(23.1)
              .build());

  @Autowired private RestClient.Builder builder;

  @Autowired private MockRestServiceServer server;

  @Test
  void givenRestClientTestSlice_whenExchangingSenmlCbor_thenWritesAndReadsSenmlCbor() {
    byte[] cbor = SenmlCbor.toCbor(PACK);
    server
        .expect(requestTo("/packs"))
        .andExpect(content().contentType(SENML_CBOR))
        .andExpect(content().bytes(cbor))
        .andRespond(withSuccess(cbor, SENML_CBOR));

    SenmlPack response =
        builder
            .build()
            .post()
            .uri("/packs")
            .contentType(SENML_CBOR)
            .accept(SENML_CBOR)
            .body(PACK)
            .retrieve()
            .body(SenmlPack.class);

    assertThat(response).isEqualTo(PACK);
    server.verify();
  }

  @Configuration
  static class TestConfiguration {}
}
