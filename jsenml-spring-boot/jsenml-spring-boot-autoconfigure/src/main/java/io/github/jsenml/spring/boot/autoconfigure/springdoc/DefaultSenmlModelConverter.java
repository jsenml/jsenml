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

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.SenmlTextFormat;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/** Default {@link SenmlModelConverter}, registered by {@link SenmlSpringDocAutoConfiguration}. */
final class DefaultSenmlModelConverter implements SenmlModelConverter {

  DefaultSenmlModelConverter() {}

  @Override
  public @Nullable Schema<?> resolve(
      AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
    Class<?> rawClass =
        type.getType() != null ? Json.mapper().constructType(type.getType()).getRawClass() : null;
    if (SenmlRecord.class.equals(rawClass)) {
      context.defineModel(RECORD_SCHEMA_NAME, recordSchema());
      return reference(RECORD_SCHEMA_NAME);
    }
    if (SenmlPack.class.equals(rawClass)) {
      context.defineModel(RECORD_SCHEMA_NAME, recordSchema());
      context.defineModel(PACK_SCHEMA_NAME, packSchema());
      return reference(PACK_SCHEMA_NAME);
    }
    return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
  }

  private Schema<?> recordSchema() {
    Map<String, Schema> properties = new LinkedHashMap<>();
    properties.put(SenmlTextFormat.BASE_NAME, typed("string", "Base Name"));
    properties.put(SenmlTextFormat.BASE_TIME, typed("number", "Base Time"));
    properties.put(SenmlTextFormat.BASE_UNIT, typed("string", "Base Unit"));
    properties.put(SenmlTextFormat.BASE_VALUE, typed("number", "Base Value"));
    properties.put(SenmlTextFormat.BASE_SUM, typed("number", "Base Sum"));
    properties.put(SenmlTextFormat.BASE_VERSION, typed("integer", "Base Version"));
    properties.put(SenmlTextFormat.NAME, typed("string", "Name"));
    properties.put(SenmlTextFormat.UNIT, typed("string", "Unit"));
    properties.put(SenmlTextFormat.TIME, typed("number", "Time"));
    properties.put(SenmlTextFormat.UPDATE_TIME, typed("number", "Update Time"));
    properties.put(SenmlTextFormat.VALUE, typed("number", "Value"));
    properties.put(SenmlTextFormat.STRING_VALUE, typed("string", "String Value"));
    properties.put(SenmlTextFormat.BOOLEAN_VALUE, typed("boolean", "Boolean Value"));
    properties.put(SenmlTextFormat.DATA_VALUE, typed("string", "Data Value, base64url-encoded"));
    properties.put(SenmlTextFormat.SUM, typed("number", "Sum"));
    return typed(
            "object",
            "SenML Record, see RFC 8428, Section 4. Fields not defined by RFC 8428 are extensions.")
        .properties(properties);
  }

  private Schema<?> packSchema() {
    Map<String, Object> voltage = new LinkedHashMap<>();
    voltage.put(SenmlTextFormat.BASE_NAME, "urn:dev:ow:10e2073a01080063:");
    voltage.put(SenmlTextFormat.NAME, "voltage");
    voltage.put(SenmlTextFormat.UNIT, "V");
    voltage.put(SenmlTextFormat.VALUE, 120.1);
    Map<String, Object> current = new LinkedHashMap<>();
    current.put(SenmlTextFormat.NAME, "current");
    current.put(SenmlTextFormat.UNIT, "A");
    current.put(SenmlTextFormat.VALUE, 1.2);
    return typed("array", "SenML Pack, see RFC 8428, Section 4.")
        .items(reference(RECORD_SCHEMA_NAME))
        .example(List.of(voltage, current));
  }

  private Schema<?> typed(String type, String description) {
    Schema<?> schema = new Schema<>();
    schema.setType(type);
    schema.addType(type);
    schema.setDescription(description);
    return schema;
  }

  private Schema<?> reference(String name) {
    return new Schema<>().$ref(Components.COMPONENTS_SCHEMAS_REF + name);
  }
}
