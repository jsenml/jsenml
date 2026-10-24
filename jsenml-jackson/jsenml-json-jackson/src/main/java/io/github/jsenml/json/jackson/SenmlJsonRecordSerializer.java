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

package io.github.jsenml.json.jackson;

import static io.github.jsenml.SenmlTextFormat.BASE_NAME;
import static io.github.jsenml.SenmlTextFormat.BASE_SUM;
import static io.github.jsenml.SenmlTextFormat.BASE_TIME;
import static io.github.jsenml.SenmlTextFormat.BASE_UNIT;
import static io.github.jsenml.SenmlTextFormat.BASE_VALUE;
import static io.github.jsenml.SenmlTextFormat.BASE_VERSION;
import static io.github.jsenml.SenmlTextFormat.BOOLEAN_VALUE;
import static io.github.jsenml.SenmlTextFormat.DATA_VALUE;
import static io.github.jsenml.SenmlTextFormat.NAME;
import static io.github.jsenml.SenmlTextFormat.STRING_VALUE;
import static io.github.jsenml.SenmlTextFormat.SUM;
import static io.github.jsenml.SenmlTextFormat.TIME;
import static io.github.jsenml.SenmlTextFormat.UNIT;
import static io.github.jsenml.SenmlTextFormat.UPDATE_TIME;
import static io.github.jsenml.SenmlTextFormat.VALUE;

import io.github.jsenml.SenmlExtensions;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.SenmlTextFormat;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

final class SenmlJsonRecordSerializer extends StdSerializer<SenmlRecord> {

  SenmlJsonRecordSerializer() {
    super(SenmlRecord.class);
  }

  /**
   * Writes given record as a SenML JSON object, with present fields in the order of RFC 8428
   * examples followed by extensions, numbers in plain notation and octet extensions as base64url
   * strings.
   *
   * @param record record to write
   * @param gen JSON generator
   * @param ctxt serialization context
   */
  @Override
  public void serialize(SenmlRecord record, JsonGenerator gen, SerializationContext ctxt) {
    gen.writeStartObject();
    writeString(gen, BASE_NAME, record.getBaseName());
    writeNumber(gen, BASE_TIME, record.getBaseTime());
    writeString(gen, BASE_UNIT, record.getBaseUnit());
    writeNumber(gen, BASE_VALUE, record.getBaseValue());
    writeNumber(gen, BASE_SUM, record.getBaseSum());
    if (record.getBaseVersion() != null) {
      gen.writeNumberProperty(BASE_VERSION, record.getBaseVersion());
    }
    writeString(gen, NAME, record.getName());
    writeString(gen, UNIT, record.getUnit());
    writeNumber(gen, TIME, record.getTime());
    writeNumber(gen, UPDATE_TIME, record.getUpdateTime());
    writeNumber(gen, VALUE, record.getValue());
    writeString(gen, STRING_VALUE, record.getStringValue());
    if (record.getBooleanValue() != null) {
      gen.writeBooleanProperty(BOOLEAN_VALUE, record.getBooleanValue());
    }
    byte[] dataValue = record.getDataValue();
    if (dataValue != null) {
      gen.writeStringProperty(DATA_VALUE, SenmlTextFormat.formatBytes(dataValue));
    }
    writeNumber(gen, SUM, record.getSum());
    SenmlExtensions extensions = record.getExtensions();
    for (String extension : extensions.names()) {
      Object extensionValue = extensions.get(extension);
      if (extensionValue instanceof String string) {
        gen.writeStringProperty(extension, string);
      } else if (extensionValue instanceof Double number) {
        gen.writeName(extension);
        gen.writeNumber(SenmlTextFormat.formatNumber(number));
      } else if (extensionValue instanceof Boolean bool) {
        gen.writeBooleanProperty(extension, bool);
      } else if (extensionValue instanceof byte[] bytes) {
        gen.writeStringProperty(extension, SenmlTextFormat.formatBytes(bytes));
      }
    }
    gen.writeEndObject();
  }

  private void writeString(JsonGenerator gen, String label, @Nullable String value) {
    if (value != null) {
      gen.writeStringProperty(label, value);
    }
  }

  private void writeNumber(JsonGenerator gen, String label, @Nullable Double value) {
    if (value != null) {
      gen.writeName(label);
      gen.writeNumber(SenmlTextFormat.formatNumber(value));
    }
  }
}
