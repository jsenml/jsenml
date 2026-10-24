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

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.xml.SenmlXml;
import io.github.jsenml.xml.spi.SenmlXmlCodecProvider;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class JacksonSenmlXmlCodecProviderTest {

  @Test
  void givenJacksonXmlOnClasspath_whenGettingDefaultXmlCodec_thenUsesJacksonBackend() {
    assertThat(ServiceLoader.load(SenmlXmlCodecProvider.class))
        .extracting(SenmlXmlCodecProvider::name)
        .containsExactly(JacksonSenmlXmlCodecProvider.NAME);
    assertThat(SenmlXml.xmlCodec()).isInstanceOf(JacksonSenmlXmlCodec.class);
    assertThat(SenmlXml.xmlCodec("jackson")).isSameAs(SenmlXml.xmlCodec());
  }

  @Test
  void givenPack_whenRoundTrippingThroughSenmlXml_thenUsesJacksonBackend() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("room").stringValue("żółć").build());

    assertThat(SenmlXml.fromXml(SenmlXml.toXml(pack))).isEqualTo(pack);
  }
}
