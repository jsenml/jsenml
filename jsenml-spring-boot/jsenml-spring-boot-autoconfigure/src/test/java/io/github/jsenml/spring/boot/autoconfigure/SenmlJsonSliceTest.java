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

import com.google.gson.Gson;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.cbor.SenmlCbor;
import io.github.jsenml.json.SenmlJson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.cbor.CBORMapper;

@JsonTest
class SenmlJsonSliceTest {

  private static final SenmlPack PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("Cel")
              .value(23.1)
              .build());

  @Autowired private JsonMapper jsonMapper;

  @Autowired private CBORMapper cborMapper;

  @Autowired private Gson gson;

  @Test
  void givenJsonTestSlice_whenWritingPackWithJacksonMappers_thenWritesSenml() {
    assertThat(jsonMapper.writeValueAsString(PACK)).isEqualTo(SenmlJson.toJson(PACK));
    assertThat(cborMapper.writeValueAsBytes(PACK)).isEqualTo(SenmlCbor.toCbor(PACK));
  }

  @Test
  void givenJsonTestSlice_whenWritingPackWithGson_thenWritesSenmlJson() {
    assertThat(gson.toJson(PACK)).isEqualTo(SenmlJson.toJson(PACK));
  }

  @Configuration
  static class TestConfiguration {}
}
