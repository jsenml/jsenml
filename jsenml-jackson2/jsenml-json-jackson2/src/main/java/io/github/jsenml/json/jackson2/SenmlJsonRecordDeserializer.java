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

package io.github.jsenml.json.jackson2;

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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.jsenml.SenmlExtensions;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.SenmlRecordBuilder;
import io.github.jsenml.SenmlTextFormat;
import java.io.IOException;
import java.io.Serial;

final class SenmlJsonRecordDeserializer extends StdDeserializer<SenmlRecord> {

  @Serial //
  private static final long serialVersionUID = 1L;

  SenmlJsonRecordDeserializer() {
    super(SenmlRecord.class);
  }

  /**
   * Reads a SenML JSON object as a record. Unknown fields with a string, number or boolean value
   * and a valid extension name are kept as extensions, including those that are mandatory to
   * understand (ending with {@code _}); whether those are acceptable is checked by {@code
   * SenmlPack.resolve}. Other unknown fields are skipped, unless they are mandatory to understand,
   * which, like fields of wrong type and values the record builder rejects (e.g. non-finite
   * numbers, invalid base64url), are reported as an input mismatch.
   *
   * @param p JSON parser, positioned at the start of the object
   * @param ctxt deserialization context
   * @return record read
   */
  @Override
  public SenmlRecord deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    try {
      return read(p, ctxt);
    } catch (IllegalArgumentException e) {
      // a value rejected by the record builder is invalid input, not a programming error
      return ctxt.reportInputMismatch(this, "Invalid SenML record: %s", e.getMessage());
    }
  }

  private SenmlRecord read(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (p.currentToken() != JsonToken.START_OBJECT) {
      return ctxt.reportInputMismatch(
          this, "SenML record must be a JSON object, got %s", p.currentToken());
    }
    SenmlRecordBuilder builder = SenmlRecord.builder();
    for (String label = p.nextFieldName(); label != null; label = p.nextFieldName()) {
      p.nextToken();
      switch (label) {
        case BASE_NAME -> builder.baseName(readString(p, ctxt, label));
        case BASE_TIME -> builder.baseTime(readNumber(p, ctxt, label));
        case BASE_UNIT -> builder.baseUnit(readString(p, ctxt, label));
        case BASE_VALUE -> builder.baseValue(readNumber(p, ctxt, label));
        case BASE_SUM -> builder.baseSum(readNumber(p, ctxt, label));
        case BASE_VERSION -> builder.baseVersion(readInteger(p, ctxt, label));
        case NAME -> builder.name(readString(p, ctxt, label));
        case UNIT -> builder.unit(readString(p, ctxt, label));
        case VALUE -> builder.value(readNumber(p, ctxt, label));
        case STRING_VALUE -> builder.stringValue(readString(p, ctxt, label));
        case BOOLEAN_VALUE -> builder.booleanValue(readBoolean(p, ctxt, label));
        case DATA_VALUE -> builder.dataValue(readString(p, ctxt, label));
        case SUM -> builder.sum(readNumber(p, ctxt, label));
        case TIME -> builder.time(readNumber(p, ctxt, label));
        case UPDATE_TIME -> builder.updateTime(readNumber(p, ctxt, label));
        default -> readExtension(p, ctxt, label, builder);
      }
    }
    return builder.build();
  }

  private String readString(JsonParser p, DeserializationContext ctxt, String label)
      throws IOException {
    if (p.currentToken() != JsonToken.VALUE_STRING) {
      return ctxt.reportInputMismatch(this, "SenML field '%s' must be a string", label);
    }
    return p.getText();
  }

  private double readNumber(JsonParser p, DeserializationContext ctxt, String label)
      throws IOException {
    if (!p.currentToken().isNumeric()) {
      return ctxt.reportInputMismatch(this, "SenML field '%s' must be a number", label);
    }
    return p.getDoubleValue();
  }

  private int readInteger(JsonParser p, DeserializationContext ctxt, String label)
      throws IOException {
    if (p.currentToken() != JsonToken.VALUE_NUMBER_INT) {
      return ctxt.reportInputMismatch(this, "SenML field '%s' must be an integer", label);
    }
    return p.getIntValue();
  }

  private boolean readBoolean(JsonParser p, DeserializationContext ctxt, String label)
      throws IOException {
    if (!p.currentToken().isBoolean()) {
      return ctxt.reportInputMismatch(this, "SenML field '%s' must be a boolean", label);
    }
    return p.getBooleanValue();
  }

  private void readExtension(
      JsonParser p, DeserializationContext ctxt, String label, SenmlRecordBuilder builder)
      throws IOException {
    JsonToken token = p.currentToken();
    if (SenmlExtensions.isValidName(label)) {
      if (token == JsonToken.VALUE_STRING) {
        builder.extension(label, p.getText());
        return;
      }
      if (token.isNumeric()) {
        builder.extension(label, p.getDoubleValue());
        return;
      }
      if (token.isBoolean()) {
        builder.extension(label, p.getBooleanValue());
        return;
      }
    }
    if (SenmlTextFormat.isMandatoryToUnderstand(label)) {
      ctxt.reportInputMismatch(
          this,
          "SenML field '%s' is mandatory to understand but cannot be kept as an extension",
          label);
    }
    p.skipChildren();
  }
}
