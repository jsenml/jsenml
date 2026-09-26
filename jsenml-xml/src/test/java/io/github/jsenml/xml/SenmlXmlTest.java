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

package io.github.jsenml.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jsenml.SenmlException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.xml.spi.SenmlXmlCodecProvider;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SenmlXmlTest {

  @Test
  void givenRegisteredProvider_whenGettingDefaultXmlCodec_thenUsesIt() {
    assertThat(SenmlXml.getXmlCodecProviders())
        .extracting(SenmlXmlCodecProvider::name)
        .containsExactly("xml-only");
    assertThat(SenmlXml.xmlCodec()).isEqualTo(new TestProviders.NamedXmlCodec("xml-only"));
    assertThat(SenmlXml.xmlCodec("xml-only")).isSameAs(SenmlXml.xmlCodec());
    assertThat(SenmlXml.toXml(SenmlPack.empty())).isEqualTo("xml-only");
    assertThat(SenmlXml.toXmlBytes(SenmlPack.empty()))
        .isEqualTo("xml-only".getBytes(StandardCharsets.UTF_8));
    assertThat(SenmlXml.fromXml("<x/>").getFirst().getStringValue()).isEqualTo("<x/>");
    assertThat(
            SenmlXml.fromXml("żółć".getBytes(StandardCharsets.UTF_8)).getFirst().getStringValue())
        .isEqualTo("żółć");
  }

  @Test
  void givenUnknownProviderName_whenGettingXmlCodec_thenThrows() {
    assertThatThrownBy(() -> SenmlXml.xmlCodec("unknown"))
        .isInstanceOf(SenmlException.class)
        .hasMessageContaining(SenmlXmlCodecProvider.class.getName())
        .hasMessageContaining("[xml-only]");
  }
}
