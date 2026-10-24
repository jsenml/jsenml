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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.github.jsenml.SenmlCodecException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.json.SenmlJsonCodec;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * {@link SenmlJsonCodec} backed by Gson.
 *
 * <p>Usually obtained via {@link io.github.jsenml.json.SenmlJson#jsonCodec(String)} with name
 * {@value GsonSenmlJsonCodecProvider#NAME}, but can be created directly to reuse a customized
 * {@link Gson}.
 */
public final class GsonSenmlJsonCodec implements SenmlJsonCodec {

  private final Gson gson;

  /** Creates a codec using default {@link Gson} configuration. */
  public GsonSenmlJsonCodec() {
    this(new Gson());
  }

  /**
   * Creates a codec based on given {@link Gson}. A {@link SenmlTypeAdapterFactory} is registered
   * and HTML escaping is disabled on a derived instance, the given one is not modified.
   *
   * @param gson base Gson instance
   */
  public GsonSenmlJsonCodec(Gson gson) {
    this.gson =
        gson.newBuilder()
            .registerTypeAdapterFactory(new SenmlTypeAdapterFactory())
            .disableHtmlEscaping()
            .create();
  }

  /**
   * Encodes a pack to SenML JSON using Gson.
   *
   * @param pack pack to encode
   * @return SenML JSON
   * @throws SenmlCodecException if encoding fails
   */
  @Override
  public String encode(SenmlPack pack) {
    try {
      return gson.toJson(pack, SenmlPack.class);
    } catch (JsonParseException e) {
      throw new SenmlCodecException("Failed to encode SenML pack", e);
    }
  }

  /**
   * Encodes a pack to SenML JSON using Gson, writing it to given writer. The writer is not closed.
   *
   * @param pack pack to encode
   * @param writer target writer
   * @throws SenmlCodecException if encoding or writing fails
   */
  @Override
  public void encode(SenmlPack pack, Writer writer) {
    try {
      gson.toJson(pack, SenmlPack.class, writer);
    } catch (JsonParseException e) {
      throw new SenmlCodecException("Failed to encode SenML pack", e);
    }
  }

  /**
   * Decodes a pack from SenML JSON using Gson.
   *
   * @param json SenML JSON
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML JSON pack
   */
  @Override
  public SenmlPack decode(String json) {
    return decode(new StringReader(json));
  }

  /**
   * Decodes a pack from SenML JSON read from given reader using Gson. The reader is not closed.
   *
   * @param reader source reader
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML JSON pack or reading fails
   */
  @Override
  public SenmlPack decode(Reader reader) {
    SenmlPack pack;
    try {
      pack = gson.fromJson(reader, SenmlPack.class);
    } catch (JsonParseException | IllegalArgumentException e) {
      throw new SenmlCodecException("Failed to decode SenML pack: " + e.getMessage(), e);
    }
    if (pack == null) {
      throw new SenmlCodecException("Failed to decode SenML pack: input is empty or null");
    }
    return pack;
  }
}
