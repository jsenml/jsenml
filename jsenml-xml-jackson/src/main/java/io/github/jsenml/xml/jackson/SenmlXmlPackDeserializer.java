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

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlPackBuilder;
import io.github.jsenml.xml.SenmlXmlCodec;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

final class SenmlXmlPackDeserializer extends StdDeserializer<SenmlPack> {

  private final SenmlXmlRecordDeserializer recordDeserializer;

  SenmlXmlPackDeserializer(SenmlXmlRecordDeserializer recordDeserializer) {
    super(SenmlPack.class);
    this.recordDeserializer = recordDeserializer;
  }

  /**
   * Reads a SenML XML element as a pack, with a record per {@code senml} child element. Other child
   * elements and text are skipped.
   *
   * @param p XML parser, positioned at the element
   * @param ctxt deserialization context
   * @return pack read
   */
  @Override
  public SenmlPack deserialize(JsonParser p, DeserializationContext ctxt) {
    SenmlPackBuilder builder = SenmlPack.builder();
    if (p.currentToken() != JsonToken.START_OBJECT) {
      p.skipChildren();
      return builder.build();
    }
    for (String name = p.nextName(); name != null; name = p.nextName()) {
      p.nextToken();
      if (SenmlXmlCodec.RECORD_ELEMENT.equals(name)) {
        builder.add(recordDeserializer.deserialize(p, ctxt));
      } else {
        p.skipChildren();
      }
    }
    return builder.build();
  }

  /**
   * Returns an empty pack, read from an empty element.
   *
   * @param ctxt deserialization context
   * @return empty pack
   */
  @Override
  public SenmlPack getEmptyValue(DeserializationContext ctxt) {
    return SenmlPack.empty();
  }
}
