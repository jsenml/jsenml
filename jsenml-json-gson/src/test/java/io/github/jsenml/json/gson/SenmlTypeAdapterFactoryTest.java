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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SenmlTypeAdapterFactoryTest {

  private final Gson gson =
      new GsonBuilder().registerTypeAdapterFactory(new SenmlTypeAdapterFactory()).create();

  @Test
  void givenRecord_whenSerializingWithPlainGson_thenWritesSenmlObject() {
    SenmlRecord record = SenmlRecord.builder().name("a").value(1.5).build();

    assertThat(gson.toJson(record)).isEqualTo("{\"n\":\"a\",\"v\":1.5}");
    assertThat(gson.fromJson("{\"n\":\"a\",\"v\":1.5}", SenmlRecord.class)).isEqualTo(record);
  }

  @Test
  void givenPackNestedInOtherObject_whenSerializingWithPlainGson_thenWritesSenmlArray() {
    Map<String, SenmlPack> payload =
        Map.of("pack", SenmlPack.of(SenmlRecord.builder().name("a").build()));

    assertThat(gson.toJson(payload)).isEqualTo("{\"pack\":[{\"n\":\"a\"}]}");
  }

  @Test
  void givenNull_whenSerializingWithPlainGson_thenWritesNull() {
    assertThat(gson.toJson(null, SenmlPack.class)).isEqualTo("null");
    assertThat(gson.fromJson("null", SenmlPack.class)).isNull();
  }

  @Test
  void givenValueRejectedByBuilder_whenReadingWithPlainGson_thenThrowsJsonSyntaxException() {
    assertThatThrownBy(() -> gson.fromJson("[{\"n\":\"a\",\"vd\":\"!!!\"}]", SenmlPack.class))
        .isInstanceOf(JsonSyntaxException.class)
        .hasMessageContaining("'vd'");
  }
}
