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

package io.github.jsenml.xml.jackson2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.xml.SenmlXmlCodec;
import java.io.IOException;
import java.io.Serial;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

final class SenmlXmlPackSerializer extends StdSerializer<SenmlPack> {

  @Serial //
  private static final long serialVersionUID = 1L;

  private static final QName PACK_ELEMENT =
      new QName(SenmlXmlCodec.NAMESPACE, SenmlXmlCodec.PACK_ELEMENT);

  static final QName RECORD_ELEMENT =
      new QName(SenmlXmlCodec.NAMESPACE, SenmlXmlCodec.RECORD_ELEMENT);

  private final SenmlXmlRecordSerializer recordSerializer;

  SenmlXmlPackSerializer(SenmlXmlRecordSerializer recordSerializer) {
    super(SenmlPack.class);
    this.recordSerializer = recordSerializer;
  }

  /**
   * Writes given pack as a SenML XML element containing an element per record. As root value, the
   * pack is written as {@code sensml} element in SenML namespace, otherwise the element is named by
   * the enclosing property and, like the record elements, takes the namespace of its surroundings.
   *
   * @param pack pack to write
   * @param gen XML generator
   * @param ctxt serialization context
   */
  @Override
  public void serialize(SenmlPack pack, JsonGenerator gen, SerializerProvider ctxt)
      throws IOException {
    ToXmlGenerator xgen = requireXmlGenerator(gen, ctxt, SenmlPack.class);
    boolean root = xgen.inRoot();
    if (root) {
      setDefaultNamespace(xgen);
      xgen.setNextName(PACK_ELEMENT);
    }
    xgen.writeStartObject(pack);
    for (SenmlRecord record : pack) {
      xgen.writeFieldName(SenmlXmlCodec.RECORD_ELEMENT);
      if (root) {
        xgen.setNextName(RECORD_ELEMENT);
      }
      recordSerializer.serialize(record, xgen, ctxt);
    }
    xgen.writeEndObject();
  }

  /** Binds SenML namespace as default one, so that elements are written without a prefix. */
  static void setDefaultNamespace(ToXmlGenerator xgen) throws IOException {
    try {
      xgen.getStaxWriter().setDefaultNamespace(SenmlXmlCodec.NAMESPACE);
    } catch (XMLStreamException e) {
      throw new IOException("Failed to bind SenML namespace", e);
    }
  }

  static ToXmlGenerator requireXmlGenerator(
      JsonGenerator gen, SerializerProvider ctxt, Class<?> type) throws IOException {
    if (gen instanceof ToXmlGenerator xgen) {
      return xgen;
    }
    return ctxt.reportBadDefinition(
        type, "SenML XML serializer requires an XML generator, got " + gen.getClass().getName());
  }
}
