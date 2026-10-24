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

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlPackBuilder;
import io.github.jsenml.SenmlRecord;
import java.io.IOException;

final class SenmlPackTypeAdapter extends TypeAdapter<SenmlPack> {

  private final TypeAdapter<SenmlRecord> recordAdapter;

  SenmlPackTypeAdapter(TypeAdapter<SenmlRecord> recordAdapter) {
    this.recordAdapter = recordAdapter;
  }

  /**
   * Writes given pack as a SenML JSON array of records.
   *
   * @param out JSON writer
   * @param pack pack to write
   * @throws IOException if writing fails
   */
  @Override
  public void write(JsonWriter out, SenmlPack pack) throws IOException {
    out.beginArray();
    for (SenmlRecord record : pack) {
      recordAdapter.write(out, record);
    }
    out.endArray();
  }

  /**
   * Reads a SenML JSON array of records as a pack.
   *
   * @param in JSON reader
   * @return pack read
   * @throws IOException if reading fails or input is not a SenML JSON array
   */
  @Override
  public SenmlPack read(JsonReader in) throws IOException {
    if (in.peek() != JsonToken.BEGIN_ARRAY) {
      throw new JsonParseException(
          "SenML pack must be a JSON array, got " + in.peek() + " at " + in.getPath());
    }
    SenmlPackBuilder builder = SenmlPack.builder();
    in.beginArray();
    while (in.hasNext()) {
      builder.add(recordAdapter.read(in));
    }
    in.endArray();
    return builder.build();
  }
}
