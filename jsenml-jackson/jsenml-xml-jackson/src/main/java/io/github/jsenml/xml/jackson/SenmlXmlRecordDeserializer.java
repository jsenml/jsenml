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

package io.github.jsenml.xml.jackson;

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
import io.github.jsenml.SenmlRecordBuilder;
import java.util.regex.Pattern;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

final class SenmlXmlRecordDeserializer extends StdDeserializer<SenmlRecord> {

  /** Lexical space of {@code xsd:double}, without the special values, which are not finite. */
  private static final Pattern DOUBLE =
      Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?");

  SenmlXmlRecordDeserializer() {
    super(SenmlRecord.class);
  }

  /**
   * Reads a SenML XML element as a record, with fields taken from its attributes. Unknown
   * attributes with a valid extension name are kept as string extensions, including those that are
   * mandatory to understand (ending with {@code _}); whether those are acceptable is checked by
   * {@code SenmlPack.resolve}. Other unknown attributes, child elements and text are skipped.
   * Attribute values not matching their XML type are reported as an input mismatch.
   *
   * <p>Values the record builder rejects (e.g. non-finite numbers) are reported as an input
   * mismatch.
   *
   * @param p XML parser, positioned at the element
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
    SenmlRecordBuilder builder = SenmlRecord.builder();
    if (p.currentToken() != JsonToken.START_OBJECT) {
      p.skipChildren();
      return builder.build();
    }
    for (String label = p.nextName(); label != null; label = p.nextName()) {
      if (p.nextToken() != JsonToken.VALUE_STRING) {
        p.skipChildren();
        continue;
      }
      String value = p.getString();
      switch (label) {
        case BASE_NAME -> builder.baseName(value);
        case BASE_TIME -> builder.baseTime(readDouble(ctxt, label, value));
        case BASE_UNIT -> builder.baseUnit(value);
        case BASE_VALUE -> builder.baseValue(readDouble(ctxt, label, value));
        case BASE_SUM -> builder.baseSum(readDouble(ctxt, label, value));
        case BASE_VERSION -> builder.baseVersion(readInt(ctxt, label, value));
        case NAME -> builder.name(value);
        case UNIT -> builder.unit(value);
        case VALUE -> builder.value(readDouble(ctxt, label, value));
        case STRING_VALUE -> builder.stringValue(value);
        case BOOLEAN_VALUE -> builder.booleanValue(readBoolean(ctxt, label, value));
        case DATA_VALUE -> builder.dataValue(value);
        case SUM -> builder.sum(readDouble(ctxt, label, value));
        case TIME -> builder.time(readDouble(ctxt, label, value));
        case UPDATE_TIME -> builder.updateTime(readDouble(ctxt, label, value));
        default -> {
          if (SenmlExtensions.isValidName(label)) {
            builder.extension(label, value);
          }
        }
      }
    }
    return builder.build();
  }

  private double readDouble(DeserializationContext ctxt, String label, String value) {
    String trimmed = value.strip();
    if (!DOUBLE.matcher(trimmed).matches()) {
      return ctxt.reportInputMismatch(
          this, "SenML attribute '%s' must be a finite xsd:double, got '%s'", label, value);
    }
    return Double.parseDouble(trimmed);
  }

  private int readInt(DeserializationContext ctxt, String label, String value) {
    try {
      return Integer.parseInt(value.strip());
    } catch (NumberFormatException e) {
      return ctxt.reportInputMismatch(
          this, "SenML attribute '%s' must be an xsd:int, got '%s'", label, value);
    }
  }

  private boolean readBoolean(DeserializationContext ctxt, String label, String value) {
    return switch (value.strip()) {
      case "true", "1" -> true;
      case "false", "0" -> false;
      default ->
          ctxt.reportInputMismatch(
              this, "SenML attribute '%s' must be an xsd:boolean, got '%s'", label, value);
    };
  }
}
