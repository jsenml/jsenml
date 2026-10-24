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
import io.github.jsenml.SenmlPackBuilder;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

final class SenmlCborPackDeserializer extends StdDeserializer<SenmlPack> {

  private final SenmlCborRecordDeserializer recordDeserializer;

  SenmlCborPackDeserializer(SenmlCborRecordDeserializer recordDeserializer) {
    super(SenmlPack.class);
    this.recordDeserializer = recordDeserializer;
  }

  /**
   * Reads a SenML CBOR array of records as a pack. Input that is not a SenML CBOR array is reported
   * as an input mismatch.
   *
   * @param p CBOR parser, positioned at the start of the array
   * @param ctxt deserialization context
   * @return pack read
   */
  @Override
  public SenmlPack deserialize(JsonParser p, DeserializationContext ctxt) {
    if (p.currentToken() != JsonToken.START_ARRAY) {
      return ctxt.reportInputMismatch(
          this, "SenML pack must be a CBOR array, got %s", p.currentToken());
    }
    SenmlPackBuilder builder = SenmlPack.builder();
    while (p.nextToken() != JsonToken.END_ARRAY) {
      builder.add(recordDeserializer.deserialize(p, ctxt));
    }
    return builder.build();
  }
}
