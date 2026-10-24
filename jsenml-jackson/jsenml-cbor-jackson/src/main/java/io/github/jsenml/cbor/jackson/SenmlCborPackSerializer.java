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

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

final class SenmlCborPackSerializer extends StdSerializer<SenmlPack> {

  private final SenmlCborRecordSerializer recordSerializer;

  SenmlCborPackSerializer(SenmlCborRecordSerializer recordSerializer) {
    super(SenmlPack.class);
    this.recordSerializer = recordSerializer;
  }

  /**
   * Writes given pack as a SenML CBOR array of records.
   *
   * @param pack pack to write
   * @param gen CBOR generator
   * @param ctxt serialization context
   */
  @Override
  public void serialize(SenmlPack pack, JsonGenerator gen, SerializationContext ctxt) {
    gen.writeStartArray(pack, pack.size());
    for (SenmlRecord record : pack) {
      recordSerializer.serialize(record, gen, ctxt);
    }
    gen.writeEndArray();
  }
}
