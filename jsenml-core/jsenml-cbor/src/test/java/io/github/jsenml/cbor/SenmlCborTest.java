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

package io.github.jsenml.cbor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.github.jsenml.SenmlException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.cbor.spi.SenmlCborCodecProvider;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SenmlCborTest {

  @Test
  void givenRegisteredProviders_whenGettingProviders_thenOrdersByPriority() {
    assertThat(SenmlCbor.getCborCodecProviders())
        .extracting(SenmlCborCodecProvider::name, SenmlCborCodecProvider::getPriority)
        .containsExactly(tuple("cbor-high", 10), tuple("cbor-low", 0));
  }

  @Test
  void givenRegisteredProviders_whenGettingDefaultCborCodec_thenUsesHighestPriority() {
    assertThat(SenmlCbor.cborCodec()).isEqualTo(new TestProviders.NamedCborCodec("cbor-high"));
    assertThat(SenmlCbor.toCbor(SenmlPack.empty()))
        .isEqualTo("cbor-high".getBytes(StandardCharsets.UTF_8));
    assertThat(SenmlCbor.fromCbor("x".getBytes(StandardCharsets.UTF_8)).getFirst().getStringValue())
        .isEqualTo("x");
  }

  @Test
  void givenProviderName_whenGettingCborCodec_thenUsesThatProviderAndCachesCodec() {
    assertThat(SenmlCbor.cborCodec("cbor-low"))
        .isEqualTo(new TestProviders.NamedCborCodec("cbor-low"));
    assertThat(SenmlCbor.cborCodec("cbor-low")).isSameAs(SenmlCbor.cborCodec("cbor-low"));
  }

  @Test
  void givenUnknownProviderName_whenGettingCborCodec_thenThrows() {
    assertThatThrownBy(() -> SenmlCbor.cborCodec("unknown"))
        .isInstanceOf(SenmlException.class)
        .hasMessageContaining(SenmlCborCodecProvider.class.getName())
        .hasMessageContaining("[cbor-high, cbor-low]");
  }
}
