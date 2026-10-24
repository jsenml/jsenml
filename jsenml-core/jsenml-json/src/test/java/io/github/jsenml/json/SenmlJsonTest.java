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

package io.github.jsenml.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.github.jsenml.SenmlException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.json.spi.SenmlJsonCodecProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SenmlJsonTest {

  @Test
  void givenRegisteredProviders_whenGettingProviders_thenOrdersByPriorityAndDropsDuplicates() {
    assertThat(SenmlJson.getJsonCodecProviders())
        .extracting(SenmlJsonCodecProvider::name, SenmlJsonCodecProvider::getPriority)
        .containsExactly(tuple("high", 10), tuple("low", 0));
  }

  @Test
  void givenRegisteredProviders_whenGettingDefaultJsonCodec_thenUsesHighestPriority() {
    assertThat(SenmlJson.jsonCodec()).isEqualTo(new TestProviders.NamedCodec("high"));
    assertThat(SenmlJson.toJson(SenmlPack.empty())).isEqualTo("high");
    assertThat(SenmlJson.fromJson("x").getRecords().getFirst().getName()).isEqualTo("high");
  }

  @Test
  void givenRegisteredProviders_whenEncodingToJsonBytes_thenUsesDefaultCodecAndUtf8() {
    assertThat(SenmlJson.toJsonBytes(SenmlPack.empty()))
        .isEqualTo("high".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void givenUtf8Bytes_whenDecodingFromJson_thenUsesDefaultCodecAndUtf8() {
    SenmlPack pack = SenmlJson.fromJson("żółć".getBytes(StandardCharsets.UTF_8));

    assertThat(pack.getFirst().getName()).isEqualTo("high");
    assertThat(pack.getFirst().getStringValue()).isEqualTo("żółć");
  }

  @Test
  void givenProviderName_whenGettingJsonCodec_thenUsesThatProviderAndCachesCodec() {
    assertThat(SenmlJson.jsonCodec("low")).isEqualTo(new TestProviders.NamedCodec("low"));
    assertThat(SenmlJson.jsonCodec("low")).isSameAs(SenmlJson.jsonCodec("low"));
  }

  @Test
  void givenUnknownProviderName_whenGettingJsonCodec_thenThrows() {
    assertThatThrownBy(() -> SenmlJson.jsonCodec("unknown"))
        .isInstanceOf(SenmlException.class)
        .hasMessageContaining("'unknown'")
        .hasMessageContaining("[high, low]");
  }

  @Test
  void givenClassLoaderWithoutProviders_whenLoadingProviders_thenReturnsEmptyList()
      throws Exception {
    try (URLClassLoader empty = new URLClassLoader(new URL[0], null)) {
      assertThat(SenmlJson.loadJsonCodecProviders(empty)).isEmpty();
    }
  }
}
