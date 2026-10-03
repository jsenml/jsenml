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

package io.github.jsenml.cbor.jackson;

import static io.github.jsenml.cbor.SenmlCborFormat.BASE_NAME;
import static io.github.jsenml.cbor.SenmlCborFormat.BASE_SUM;
import static io.github.jsenml.cbor.SenmlCborFormat.BASE_TIME;
import static io.github.jsenml.cbor.SenmlCborFormat.BASE_UNIT;
import static io.github.jsenml.cbor.SenmlCborFormat.BASE_VALUE;
import static io.github.jsenml.cbor.SenmlCborFormat.BASE_VERSION;
import static io.github.jsenml.cbor.SenmlCborFormat.BOOLEAN_VALUE;
import static io.github.jsenml.cbor.SenmlCborFormat.DATA_VALUE;
import static io.github.jsenml.cbor.SenmlCborFormat.NAME;
import static io.github.jsenml.cbor.SenmlCborFormat.STRING_VALUE;
import static io.github.jsenml.cbor.SenmlCborFormat.SUM;
import static io.github.jsenml.cbor.SenmlCborFormat.TIME;
import static io.github.jsenml.cbor.SenmlCborFormat.UNIT;
import static io.github.jsenml.cbor.SenmlCborFormat.UPDATE_TIME;
import static io.github.jsenml.cbor.SenmlCborFormat.VALUE;

import io.github.jsenml.SenmlExtensions;
import io.github.jsenml.SenmlRecord;
import java.util.Objects;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

final class SenmlCborRecordSerializer extends StdSerializer<SenmlRecord> {

  /** Integral values up to 2^53 are exactly representable as double, so they can be integers. */
  private static final double MAX_EXACT_INTEGER = 0x1p53;

  SenmlCborRecordSerializer() {
    super(SenmlRecord.class);
  }

  /**
   * Writes given record as a SenML CBOR map, with integer keys of present fields in the order of
   * RFC 8428 examples followed by extensions with text-string keys, numbers in their most compact
   * exact form and the data value and octet extensions as byte strings.
   *
   * @param record record to write
   * @param gen CBOR generator
   * @param ctxt serialization context
   */
  @Override
  public void serialize(SenmlRecord record, JsonGenerator gen, SerializationContext ctxt) {
    gen.writeStartObject(record, countFields(record));
    writeString(gen, BASE_NAME, record.getBaseName());
    writeNumber(gen, BASE_TIME, record.getBaseTime());
    writeString(gen, BASE_UNIT, record.getBaseUnit());
    writeNumber(gen, BASE_VALUE, record.getBaseValue());
    writeNumber(gen, BASE_SUM, record.getBaseSum());
    if (record.getBaseVersion() != null) {
      gen.writePropertyId(BASE_VERSION);
      gen.writeNumber(record.getBaseVersion());
    }
    writeString(gen, NAME, record.getName());
    writeString(gen, UNIT, record.getUnit());
    writeNumber(gen, TIME, record.getTime());
    writeNumber(gen, UPDATE_TIME, record.getUpdateTime());
    writeNumber(gen, VALUE, record.getValue());
    writeString(gen, STRING_VALUE, record.getStringValue());
    if (record.getBooleanValue() != null) {
      gen.writePropertyId(BOOLEAN_VALUE);
      gen.writeBoolean(record.getBooleanValue());
    }
    byte[] dataValue = record.getDataValue();
    if (dataValue != null) {
      gen.writePropertyId(DATA_VALUE);
      gen.writeBinary(dataValue);
    }
    writeNumber(gen, SUM, record.getSum());
    SenmlExtensions extensions = record.getExtensions();
    for (String extension : extensions.names()) {
      Object extensionValue = extensions.get(extension);
      gen.writeName(extension);
      if (extensionValue instanceof String string) {
        gen.writeString(string);
      } else if (extensionValue instanceof Double number) {
        writeCompactNumber(gen, number);
      } else if (extensionValue instanceof Boolean bool) {
        gen.writeBoolean(bool);
      } else if (extensionValue instanceof byte[] bytes) {
        gen.writeBinary(bytes);
      }
    }
    gen.writeEndObject();
  }

  /** Counts present fields, so that the map is written with definite length, as in RFC 8428. */
  private int countFields(SenmlRecord record) {
    long fields =
        Stream.of(
                record.getBaseName(),
                record.getBaseTime(),
                record.getBaseUnit(),
                record.getBaseValue(),
                record.getBaseSum(),
                record.getBaseVersion(),
                record.getName(),
                record.getUnit(),
                record.getTime(),
                record.getUpdateTime(),
                record.getValue(),
                record.getStringValue(),
                record.getBooleanValue(),
                record.getDataValue(),
                record.getSum())
            .filter(Objects::nonNull)
            .count();
    return Math.toIntExact(fields) + record.getExtensions().size();
  }

  private void writeString(JsonGenerator gen, int key, @Nullable String value) {
    if (value != null) {
      gen.writePropertyId(key);
      gen.writeString(value);
    }
  }

  private void writeNumber(JsonGenerator gen, int key, @Nullable Double value) {
    if (value != null) {
      gen.writePropertyId(key);
      writeCompactNumber(gen, value);
    }
  }

  private void writeCompactNumber(JsonGenerator gen, double number) {
    if (number == Math.rint(number) && Math.abs(number) <= MAX_EXACT_INTEGER) {
      gen.writeNumber((long) number);
    } else if ((double) (float) number == number) {
      gen.writeNumber((float) number);
    } else {
      gen.writeNumber(number);
    }
  }
}
