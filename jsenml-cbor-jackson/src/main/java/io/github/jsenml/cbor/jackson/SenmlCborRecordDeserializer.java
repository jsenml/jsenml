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
import io.github.jsenml.SenmlRecordBuilder;
import io.github.jsenml.cbor.SenmlCborFormat;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

final class SenmlCborRecordDeserializer extends StdDeserializer<SenmlRecord> {

  SenmlCborRecordDeserializer() {
    super(SenmlRecord.class);
  }

  /**
   * Reads a SenML CBOR map as a record. Integer keys must be registered SenML labels. Text-string
   * keys with a text string, number, boolean or byte string value and a valid extension name are
   * kept as extensions, including those that are mandatory to understand (ending with {@code _});
   * whether those are acceptable is checked by {@code SenmlPack.resolve}. Other text-string keys
   * are skipped, unless they are mandatory to understand, which, like unknown integer keys and
   * fields of wrong type, are reported as an input mismatch.
   *
   * <p>Jackson exposes integer map keys as their decimal text, so a (non-conforming) text-string
   * key such as {@code "-1"} is read as the integer key it spells.
   *
   * <p>Values the record builder rejects (e.g. non-finite numbers) are reported as an input
   * mismatch.
   *
   * @param p CBOR parser, positioned at the start of the map
   * @param ctxt deserialization context
   * @return record read
   */
  @Override
  public SenmlRecord deserialize(JsonParser p, DeserializationContext ctxt) {
    try {
      return read(p, ctxt);
    } catch (IllegalArgumentException e) {
      // a value rejected by the record builder is invalid input, not a programming error
      return ctxt.reportInputMismatch(this, "Invalid SenML record: %s", e.getMessage());
    }
  }

  private SenmlRecord read(JsonParser p, DeserializationContext ctxt) {
    if (p.currentToken() != JsonToken.START_OBJECT) {
      return ctxt.reportInputMismatch(
          this, "SenML record must be a CBOR map, got %s", p.currentToken());
    }
    SenmlRecordBuilder builder = SenmlRecord.builder();
    for (String key = p.nextName(); key != null; key = p.nextName()) {
      p.nextToken();
      Integer label = parseIntegerLabel(key);
      if (label == null) {
        readExtension(p, ctxt, key, builder);
        continue;
      }
      switch (label) {
        case BASE_NAME -> builder.baseName(readString(p, ctxt, key));
        case BASE_TIME -> builder.baseTime(readNumber(p, ctxt, key));
        case BASE_UNIT -> builder.baseUnit(readString(p, ctxt, key));
        case BASE_VALUE -> builder.baseValue(readNumber(p, ctxt, key));
        case BASE_SUM -> builder.baseSum(readNumber(p, ctxt, key));
        case BASE_VERSION -> builder.baseVersion(readInteger(p, ctxt, key));
        case NAME -> builder.name(readString(p, ctxt, key));
        case UNIT -> builder.unit(readString(p, ctxt, key));
        case VALUE -> builder.value(readNumber(p, ctxt, key));
        case STRING_VALUE -> builder.stringValue(readString(p, ctxt, key));
        case BOOLEAN_VALUE -> builder.booleanValue(readBoolean(p, ctxt, key));
        case DATA_VALUE -> builder.dataValue(readBytes(p, ctxt, key));
        case SUM -> builder.sum(readNumber(p, ctxt, key));
        case TIME -> builder.time(readNumber(p, ctxt, key));
        case UPDATE_TIME -> builder.updateTime(readNumber(p, ctxt, key));
        default ->
            ctxt.reportInputMismatch(this, "SenML CBOR label %s is not a registered label", key);
      }
    }
    return builder.build();
  }

  private @Nullable Integer parseIntegerLabel(String key) {
    try {
      return Integer.valueOf(key);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private String readString(JsonParser p, DeserializationContext ctxt, String key) {
    if (p.currentToken() != JsonToken.VALUE_STRING) {
      return ctxt.reportInputMismatch(this, "SenML CBOR label %s must be a text string", key);
    }
    return p.getString();
  }

  private double readNumber(JsonParser p, DeserializationContext ctxt, String key) {
    if (!p.currentToken().isNumeric()) {
      return ctxt.reportInputMismatch(this, "SenML CBOR label %s must be a number", key);
    }
    return p.getDoubleValue();
  }

  private int readInteger(JsonParser p, DeserializationContext ctxt, String key) {
    if (p.currentToken() != JsonToken.VALUE_NUMBER_INT) {
      return ctxt.reportInputMismatch(this, "SenML CBOR label %s must be an integer", key);
    }
    return p.getIntValue();
  }

  private boolean readBoolean(JsonParser p, DeserializationContext ctxt, String key) {
    if (!p.currentToken().isBoolean()) {
      return ctxt.reportInputMismatch(this, "SenML CBOR label %s must be a boolean", key);
    }
    return p.getBooleanValue();
  }

  private byte[] readBytes(JsonParser p, DeserializationContext ctxt, String key) {
    if (p.currentToken() == JsonToken.VALUE_EMBEDDED_OBJECT
        && p.getEmbeddedObject() instanceof byte[] bytes) {
      return bytes;
    }
    return ctxt.reportInputMismatch(this, "SenML CBOR label %s must be a byte string", key);
  }

  private void readExtension(
      JsonParser p, DeserializationContext ctxt, String key, SenmlRecordBuilder builder) {
    JsonToken token = p.currentToken();
    if (SenmlExtensions.isValidName(key)) {
      if (token == JsonToken.VALUE_STRING) {
        builder.extension(key, p.getString());
        return;
      }
      if (token.isNumeric()) {
        builder.extension(key, p.getDoubleValue());
        return;
      }
      if (token.isBoolean()) {
        builder.extension(key, p.getBooleanValue());
        return;
      }
      if (token == JsonToken.VALUE_EMBEDDED_OBJECT
          && p.getEmbeddedObject() instanceof byte[] bytes) {
        builder.extension(key, bytes);
        return;
      }
    }
    if (SenmlCborFormat.isMandatoryToUnderstand(key)) {
      ctxt.reportInputMismatch(
          this,
          "SenML CBOR label '%s' is mandatory to understand but cannot be kept as an extension",
          key);
    }
    p.skipChildren();
  }
}
