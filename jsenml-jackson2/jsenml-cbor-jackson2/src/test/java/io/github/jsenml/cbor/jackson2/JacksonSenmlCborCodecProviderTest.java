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

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.cbor.SenmlCbor;
import io.github.jsenml.cbor.spi.SenmlCborCodecProvider;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class JacksonSenmlCborCodecProviderTest {

  @Test
  void givenJacksonCborOnClasspath_whenGettingDefaultCborCodec_thenUsesJacksonBackend()
      throws Exception {
    assertThat(ServiceLoader.load(SenmlCborCodecProvider.class))
        .extracting(SenmlCborCodecProvider::name)
        .containsExactly(JacksonSenmlCborCodecProvider.NAME);
    assertThat(SenmlCbor.cborCodec()).isInstanceOf(JacksonSenmlCborCodec.class);
    assertThat(SenmlCbor.cborCodec(JacksonSenmlCborCodecProvider.NAME))
        .isSameAs(SenmlCbor.cborCodec());
  }

  @Test
  void givenPack_whenRoundTrippingThroughSenmlCbor_thenUsesJacksonBackend() throws Exception {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("room").stringValue("żółć").build());

    assertThat(SenmlCbor.fromCbor(SenmlCbor.toCbor(pack))).isEqualTo(pack);
  }
}
