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

package io.github.jsenml.xml.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.dataformat.xml.XmlMapper;

class SenmlXmlJacksonModuleTest {

  private final XmlMapper mapper =
      XmlMapper.builder().addModule(new SenmlXmlJacksonModule()).build();

  @Test
  void givenRecord_whenSerializingWithPlainMapper_thenWritesSenmlElement() {
    SenmlRecord record = SenmlRecord.builder().name("a").booleanValue(true).build();

    String xml = mapper.writeValueAsString(record);

    assertThat(xml)
        .isEqualTo("<senml xmlns=\"urn:ietf:params:xml:ns:senml\" n=\"a\" vb=\"true\"/>");
    assertThat(mapper.readValue(xml, SenmlRecord.class)).isEqualTo(record);
  }

  @Test
  void givenPackNestedInOtherObject_whenSerializingWithPlainMapper_thenWritesItUnderProperty() {
    Payload payload = new Payload(SenmlPack.of(SenmlRecord.builder().name("a").value(1).build()));

    String xml = mapper.writeValueAsString(payload);

    assertThat(xml).contains("<pack><senml");
    assertThat(mapper.readValue(xml, Payload.class)).isEqualTo(payload);
  }

  @Test
  void givenFindAndAddModules_whenBuildingMapper_thenDoesNotRegisterXmlModule() {
    XmlMapper discovering = XmlMapper.builder().findAndAddModules().build();

    assertThat(discovering.registeredModules())
        .noneMatch(module -> module instanceof SenmlXmlJacksonModule);
  }

  @Test
  void givenValueRejectedByBuilder_whenReadingWithPlainMapper_thenReportsInputMismatch() {
    String xml =
        "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"><senml n=\"a\" vd=\"!!!\"/></sensml>";

    assertThatThrownBy(() -> mapper.readValue(xml, SenmlPack.class))
        .isInstanceOf(MismatchedInputException.class)
        .hasMessageContaining("'vd'");
  }

  record Payload(SenmlPack pack) {}
}
