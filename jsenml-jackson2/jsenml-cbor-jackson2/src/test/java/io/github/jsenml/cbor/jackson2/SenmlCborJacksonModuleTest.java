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

package io.github.jsenml.cbor.jackson2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SenmlCborJacksonModuleTest {

  private final CBORMapper mapper =
      CBORMapper.builder().addModule(new SenmlCborJacksonModule()).build();

  @Test
  void givenRecord_whenSerializingWithPlainMapper_thenWritesSenmlCborMap() throws Exception {
    SenmlRecord record = SenmlRecord.builder().name("a").booleanValue(true).build();

    byte[] cbor = mapper.writeValueAsBytes(record);

    assertThat(HexFormat.of().formatHex(cbor)).isEqualTo("a200616104f5");
    assertThat(mapper.readValue(cbor, SenmlRecord.class)).isEqualTo(record);
  }

  @Test
  void givenPackNestedInOtherObject_whenSerializingWithPlainMapper_thenWritesSenmlCborArray()
      throws Exception {
    // the outer map is written by Jackson itself, with indefinite length (bf ... ff)
    Map<String, SenmlPack> payload =
        Map.of("pack", SenmlPack.of(SenmlRecord.builder().name("a").build()));

    byte[] cbor = mapper.writeValueAsBytes(payload);

    assertThat(HexFormat.of().formatHex(cbor)).isEqualTo("bf647061636b81a1006161ff");
  }

  @Test
  void givenFindAndAddModules_whenBuildingMapper_thenDoesNotRegisterCborModule() throws Exception {
    CBORMapper discovering = CBORMapper.builder().findAndAddModules().build();

    assertThat(discovering.getRegisteredModuleIds())
        .doesNotContain(new SenmlCborJacksonModule().getTypeId());
  }

  @Test
  void givenValueRejectedByBuilder_whenReadingWithPlainMapper_thenReportsInputMismatch()
      throws Exception {
    // record {n: "a", v: +Infinity as half-precision float}
    byte[] cbor = HexFormat.of().parseHex("81a200616102f97c00");

    assertThatThrownBy(() -> mapper.readValue(cbor, SenmlPack.class))
        .isInstanceOf(MismatchedInputException.class)
        .hasMessageContaining("'v'");
  }
}
