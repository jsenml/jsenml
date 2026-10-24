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

package io.github.jsenml.json.gson;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.json.SenmlJson;
import io.github.jsenml.json.spi.SenmlJsonCodecProvider;
import java.nio.charset.StandardCharsets;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class GsonSenmlJsonCodecProviderTest {

  @Test
  void givenGsonOnClasspath_whenGettingDefaultJsonCodec_thenUsesGsonBackend() {
    assertThat(ServiceLoader.load(SenmlJsonCodecProvider.class))
        .extracting(SenmlJsonCodecProvider::name)
        .containsExactly(GsonSenmlJsonCodecProvider.NAME);
    assertThat(SenmlJson.jsonCodec()).isInstanceOf(GsonSenmlJsonCodec.class);
    assertThat(SenmlJson.jsonCodec("gson")).isSameAs(SenmlJson.jsonCodec());
  }

  @Test
  void givenPack_whenRoundTrippingThroughSenmlJsonBytes_thenUsesGsonBackendWithUtf8() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("room").stringValue("żółć").build());

    byte[] json = SenmlJson.toJsonBytes(pack);

    assertThat(new String(json, StandardCharsets.UTF_8))
        .isEqualTo("[{\"n\":\"room\",\"vs\":\"żółć\"}]");
    assertThat(SenmlJson.fromJson(json)).isEqualTo(pack);
  }
}
