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

package io.github.jsenml.json.jackson2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SenmlJsonJacksonModuleTest {

  @Test
  void givenFindAndAddModules_whenBuildingMapper_thenDiscoversSenmlModule() throws Exception {
    JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("a").value(1.5).build());

    assertThat(mapper.writeValueAsString(pack)).isEqualTo("[{\"n\":\"a\",\"v\":1.5}]");
    assertThat(mapper.readValue("[{\"n\":\"a\",\"v\":1.5}]", SenmlPack.class)).isEqualTo(pack);
  }

  @Test
  void givenRecord_whenSerializingWithPlainMapper_thenWritesSenmlObject() throws Exception {
    JsonMapper mapper = JsonMapper.builder().addModule(new SenmlJsonJacksonModule()).build();
    SenmlRecord record = SenmlRecord.builder().name("a").booleanValue(true).build();

    assertThat(mapper.writeValueAsString(record)).isEqualTo("{\"n\":\"a\",\"vb\":true}");
    assertThat(mapper.readValue("{\"n\":\"a\",\"vb\":true}", SenmlRecord.class)).isEqualTo(record);
  }

  @Test
  void givenPackNestedInOtherObject_whenSerializingWithPlainMapper_thenWritesSenmlArray()
      throws Exception {
    JsonMapper mapper = JsonMapper.builder().addModule(new SenmlJsonJacksonModule()).build();
    Map<String, SenmlPack> payload =
        Map.of("pack", SenmlPack.of(SenmlRecord.builder().name("a").build()));

    assertThat(mapper.writeValueAsString(payload)).isEqualTo("{\"pack\":[{\"n\":\"a\"}]}");
  }

  @Test
  void givenValueRejectedByBuilder_whenReadingWithPlainMapper_thenReportsInputMismatch()
      throws Exception {
    JsonMapper mapper = JsonMapper.builder().addModule(new SenmlJsonJacksonModule()).build();

    assertThatThrownBy(() -> mapper.readValue("[{\"n\":\"a\",\"vd\":\"!!!\"}]", SenmlPack.class))
        .isInstanceOf(MismatchedInputException.class)
        .hasMessageContaining("'vd'");
    assertThatThrownBy(() -> mapper.readValue("[{\"n\":\"a\",\"v\":1e400}]", SenmlPack.class))
        .isInstanceOf(MismatchedInputException.class)
        .hasMessageContaining("'v'");
  }
}
