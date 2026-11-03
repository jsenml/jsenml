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

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.jsenml.SenmlExtensions;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.SenmlRecordBuilder;
import io.github.jsenml.SenmlTextFormat;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;

final class SenmlRecordTypeAdapter extends TypeAdapter<SenmlRecord> {

  /**
   * Writes given record as a SenML JSON object, with present fields in the order of RFC 8428
   * examples followed by extensions, numbers formatted by {@code SenmlTextFormat.formatNumber} and
   * octet extensions as base64url strings.
   *
   * @param out JSON writer
   * @param record record to write
   * @throws IOException if writing fails
   */
  @Override
  public void write(JsonWriter out, SenmlRecord record) throws IOException {
    out.beginObject();
    writeString(out, BASE_NAME, record.getBaseName());
    writeNumber(out, BASE_TIME, record.getBaseTime());
    writeString(out, BASE_UNIT, record.getBaseUnit());
    writeNumber(out, BASE_VALUE, record.getBaseValue());
    writeNumber(out, BASE_SUM, record.getBaseSum());
    if (record.getBaseVersion() != null) {
      out.name(BASE_VERSION).value(record.getBaseVersion());
    }
    writeString(out, NAME, record.getName());
    writeString(out, UNIT, record.getUnit());
    writeNumber(out, TIME, record.getTime());
    writeNumber(out, UPDATE_TIME, record.getUpdateTime());
    writeNumber(out, VALUE, record.getValue());
    writeString(out, STRING_VALUE, record.getStringValue());
    if (record.getBooleanValue() != null) {
      out.name(BOOLEAN_VALUE).value(record.getBooleanValue());
    }
    byte[] dataValue = record.getDataValue();
    if (dataValue != null) {
      out.name(DATA_VALUE).value(SenmlTextFormat.formatBytes(dataValue));
    }
    writeNumber(out, SUM, record.getSum());
    SenmlExtensions extensions = record.getExtensions();
    for (String extension : extensions.names()) {
      Object extensionValue = extensions.get(extension);
      if (extensionValue instanceof String string) {
        out.name(extension).value(string);
      } else if (extensionValue instanceof Double number) {
        out.name(extension).jsonValue(SenmlTextFormat.formatNumber(number));
      } else if (extensionValue instanceof Boolean bool) {
        out.name(extension).value(bool);
      } else if (extensionValue instanceof byte[] bytes) {
        out.name(extension).value(SenmlTextFormat.formatBytes(bytes));
      }
    }
    out.endObject();
  }

  private static void writeString(JsonWriter out, String label, @Nullable String value)
      throws IOException {
    if (value != null) {
      out.name(label).value(value);
    }
  }

  private static void writeNumber(JsonWriter out, String label, @Nullable Double value)
      throws IOException {
    if (value != null) {
      out.name(label).jsonValue(SenmlTextFormat.formatNumber(value));
    }
  }

  /**
   * Reads a SenML JSON object as a record, rejecting duplicated labels. Unknown fields with a
   * string, number or boolean value and a valid extension name are kept as extensions, including
   * those that are mandatory to understand (ending with {@code _}); whether those are acceptable is
   * checked by {@code SenmlPack.resolve}. Other unknown fields are skipped, unless they are
   * mandatory to understand, which, like fields of wrong type and values the record builder rejects
   * (e.g. non-finite numbers, invalid base64url), are rejected with a {@link JsonSyntaxException}.
   *
   * @param in JSON reader
   * @return record read
   * @throws IOException if reading fails or input is not a valid SenML JSON record
   */
  @Override
  public SenmlRecord read(JsonReader in) throws IOException {
    try {
      return readRecord(in);
    } catch (IllegalArgumentException e) {
      // a value rejected by the record builder is invalid input, not a programming error
      throw new JsonSyntaxException("Invalid SenML record: " + e.getMessage(), e);
    }
  }

  private SenmlRecord readRecord(JsonReader in) throws IOException {
    expect(in, JsonToken.BEGIN_OBJECT, "SenML record must be a JSON object");
    SenmlRecordBuilder builder = SenmlRecord.builder();
    Set<String> seen = new HashSet<>();
    in.beginObject();
    while (in.hasNext()) {
      String label = in.nextName();
      if (!seen.add(label)) {
        throw new JsonParseException(
            "SenML field '" + label + "' is duplicated at " + in.getPreviousPath());
      }
      switch (label) {
        case BASE_NAME -> builder.baseName(readString(in, label));
        case BASE_TIME -> builder.baseTime(readNumber(in, label));
        case BASE_UNIT -> builder.baseUnit(readString(in, label));
        case BASE_VALUE -> builder.baseValue(readNumber(in, label));
        case BASE_SUM -> builder.baseSum(readNumber(in, label));
        case BASE_VERSION -> builder.baseVersion(readInteger(in, label));
        case NAME -> builder.name(readString(in, label));
        case UNIT -> builder.unit(readString(in, label));
        case VALUE -> builder.value(readNumber(in, label));
        case STRING_VALUE -> builder.stringValue(readString(in, label));
        case BOOLEAN_VALUE -> builder.booleanValue(readBoolean(in, label));
        case DATA_VALUE -> builder.dataValue(readString(in, label));
        case SUM -> builder.sum(readNumber(in, label));
        case TIME -> builder.time(readNumber(in, label));
        case UPDATE_TIME -> builder.updateTime(readNumber(in, label));
        default -> readExtension(in, label, builder);
      }
    }
    in.endObject();
    return builder.build();
  }

  private static String readString(JsonReader in, String label) throws IOException {
    expect(in, JsonToken.STRING, "SenML field '" + label + "' must be a string");
    return in.nextString();
  }

  private static double readNumber(JsonReader in, String label) throws IOException {
    expect(in, JsonToken.NUMBER, "SenML field '" + label + "' must be a number");
    return in.nextDouble();
  }

  private static int readInteger(JsonReader in, String label) throws IOException {
    expect(in, JsonToken.NUMBER, "SenML field '" + label + "' must be an integer");
    // JsonReader.nextInt() accepts integral literals like 10.0 or 1e1, which other backends reject
    try {
      return Integer.parseInt(in.nextString());
    } catch (NumberFormatException e) {
      throw new JsonParseException("SenML field '" + label + "' must be an integer", e);
    }
  }

  private static boolean readBoolean(JsonReader in, String label) throws IOException {
    expect(in, JsonToken.BOOLEAN, "SenML field '" + label + "' must be a boolean");
    return in.nextBoolean();
  }

  private void readExtension(JsonReader in, String label, SenmlRecordBuilder builder)
      throws IOException {
    JsonToken token = in.peek();
    if (SenmlExtensions.isValidName(label)) {
      if (token == JsonToken.STRING) {
        builder.extension(label, in.nextString());
        return;
      }
      if (token == JsonToken.NUMBER) {
        builder.extension(label, in.nextDouble());
        return;
      }
      if (token == JsonToken.BOOLEAN) {
        builder.extension(label, in.nextBoolean());
        return;
      }
    }
    if (SenmlTextFormat.isMandatoryToUnderstand(label)) {
      throw new JsonParseException(
          "SenML field '"
              + label
              + "' is mandatory to understand but cannot be kept as an extension, got "
              + token);
    }
    in.skipValue();
  }

  private static void expect(JsonReader in, JsonToken token, String message) throws IOException {
    if (in.peek() != token) {
      throw new JsonParseException(message + ", got " + in.peek() + " at " + in.getPath());
    }
  }
}
