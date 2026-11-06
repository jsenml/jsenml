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

package io.github.jsenml.spring.boot.autoconfigure.springdoc;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NullAway")
class DefaultSenmlModelConverterTest {

  private final ModelConverters converters = new ModelConverters();

  {
    converters.addConverter(new DefaultSenmlModelConverter());
  }

  @Test
  void givenRecord_whenResolving_thenDefinesSchemaWithSenmlTextFormat() {
    Map<String, Schema> schemas = converters.readAll(SenmlRecord.class);

    assertThat(schemas).containsOnlyKeys("SenmlRecord");
    Schema<?> record = schemas.get("SenmlRecord");
    assertThat(record.getProperties())
        .containsOnlyKeys(
            "bn", "bt", "bu", "bv", "bs", "bver", "n", "u", "v", "vs", "vb", "vd", "s", "t", "ut");
    assertThat(record.getProperties().get("bver").getType()).isEqualTo("integer");
  }

  @Test
  void givenPack_whenResolving_thenDefinesArrayOfRecords() {
    Map<String, Schema> schemas = converters.readAll(SenmlPack.class);

    assertThat(schemas).containsOnlyKeys("SenmlPack", "SenmlRecord");
    Schema<?> pack = schemas.get("SenmlPack");
    assertThat(pack.getType()).isEqualTo("array");
    assertThat(pack.getItems().get$ref()).isEqualTo("#/components/schemas/SenmlRecord");
  }

  @Test
  void givenTypeUsingSenmlTypes_whenResolving_thenReferencesSenmlSchemas() {
    Map<String, Schema> schemas = converters.readAll(Measurement.class);

    Schema<?> measurement = schemas.get("Measurement");
    Map<String, Schema> properties = measurement.getProperties();
    assertThat(properties.get("device").getType()).isEqualTo("string");
    assertThat(properties.get("pack").get$ref()).isEqualTo("#/components/schemas/SenmlPack");
    assertThat(properties.get("records").getItems().get$ref())
        .isEqualTo("#/components/schemas/SenmlRecord");
    Schema<?> record = schemas.get("SenmlRecord");
    assertThat(record.getProperties()).doesNotContainKey("baseName");
  }

  record Measurement(String device, SenmlPack pack, List<SenmlRecord> records) {}
}
