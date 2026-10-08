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
import io.github.jsenml.SenmlTextFormat;
import javax.xml.namespace.QName;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

final class SenmlXmlRecordSerializer extends StdSerializer<SenmlRecord> {

  SenmlXmlRecordSerializer() {
    super(SenmlRecord.class);
  }

  /**
   * Writes given record as a SenML XML element, with present fields as attributes in the order of
   * RFC 8428 examples followed by extensions, numbers in plain notation and octet extensions as
   * base64url strings. As root value, the record is written as {@code senml} element in SenML
   * namespace, otherwise the element is named by the enclosing property.
   *
   * @param record record to write
   * @param gen XML generator
   * @param ctxt serialization context
   */
  @Override
  public void serialize(SenmlRecord record, JsonGenerator gen, SerializationContext ctxt) {
    ToXmlGenerator xgen = SenmlXmlPackSerializer.requireXmlGenerator(gen, ctxt, SenmlRecord.class);
    if (xgen.inRoot()) {
      SenmlXmlPackSerializer.setDefaultNamespace(xgen);
      xgen.setNextName(SenmlXmlPackSerializer.RECORD_ELEMENT);
    }
    xgen.writeStartObject(record);
    xgen.setNextIsAttribute(true);
    writeString(xgen, BASE_NAME, record.getBaseName());
    writeNumber(xgen, BASE_TIME, record.getBaseTime());
    writeString(xgen, BASE_UNIT, record.getBaseUnit());
    writeNumber(xgen, BASE_VALUE, record.getBaseValue());
    writeNumber(xgen, BASE_SUM, record.getBaseSum());
    if (record.getBaseVersion() != null) {
      writeAttribute(xgen, BASE_VERSION, record.getBaseVersion().toString());
    }
    writeString(xgen, NAME, record.getName());
    writeString(xgen, UNIT, record.getUnit());
    writeNumber(xgen, TIME, record.getTime());
    writeNumber(xgen, UPDATE_TIME, record.getUpdateTime());
    writeNumber(xgen, VALUE, record.getValue());
    writeString(xgen, STRING_VALUE, record.getStringValue());
    if (record.getBooleanValue() != null) {
      writeAttribute(xgen, BOOLEAN_VALUE, record.getBooleanValue().toString());
    }
    byte[] dataValue = record.getDataValue();
    if (dataValue != null) {
      writeAttribute(xgen, DATA_VALUE, SenmlTextFormat.formatBytes(dataValue));
    }
    writeNumber(xgen, SUM, record.getSum());
    SenmlExtensions extensions = record.getExtensions();
    for (String extension : extensions.names()) {
      Object extensionValue = extensions.get(extension);
      if (extensionValue instanceof String string) {
        writeAttribute(xgen, extension, string);
      } else if (extensionValue instanceof Double number) {
        writeAttribute(xgen, extension, SenmlTextFormat.formatNumber(number));
      } else if (extensionValue instanceof Boolean bool) {
        writeAttribute(xgen, extension, bool.toString());
      } else if (extensionValue instanceof byte[] bytes) {
        writeAttribute(xgen, extension, SenmlTextFormat.formatBytes(bytes));
      }
    }
    xgen.setNextIsAttribute(false);
    xgen.writeEndObject();
  }

  /** Writes an attribute, which, unlike SenML elements, has no namespace. */
  private void writeAttribute(ToXmlGenerator gen, String label, String value) {
    gen.writeName(label);
    gen.setNextName(new QName(label));
    gen.writeString(value);
  }

  private void writeString(ToXmlGenerator gen, String label, @Nullable String value) {
    if (value != null) {
      writeAttribute(gen, label, value);
    }
  }

  private void writeNumber(ToXmlGenerator gen, String label, @Nullable Double value) {
    if (value != null) {
      writeAttribute(gen, label, SenmlTextFormat.formatNumber(value));
    }
  }
}
