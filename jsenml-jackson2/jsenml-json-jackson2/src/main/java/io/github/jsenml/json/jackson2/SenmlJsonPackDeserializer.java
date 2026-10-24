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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlPackBuilder;
import java.io.IOException;
import java.io.Serial;

final class SenmlJsonPackDeserializer extends StdDeserializer<SenmlPack> {

  @Serial //
  private static final long serialVersionUID = 1L;

  private final SenmlJsonRecordDeserializer recordDeserializer;

  SenmlJsonPackDeserializer(SenmlJsonRecordDeserializer recordDeserializer) {
    super(SenmlPack.class);
    this.recordDeserializer = recordDeserializer;
  }

  /**
   * Reads a SenML JSON array of records as a pack. Input that is not a SenML JSON array is reported
   * as an input mismatch.
   *
   * @param p JSON parser, positioned at the start of the array
   * @param ctxt deserialization context
   * @return pack read
   */
  @Override
  public SenmlPack deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (p.currentToken() != JsonToken.START_ARRAY) {
      return ctxt.reportInputMismatch(
          this, "SenML pack must be a JSON array, got %s", p.currentToken());
    }
    SenmlPackBuilder builder = SenmlPack.builder();
    while (p.nextToken() != JsonToken.END_ARRAY) {
      builder.add(recordDeserializer.deserialize(p, ctxt));
    }
    return builder.build();
  }
}
