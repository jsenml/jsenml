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

package io.github.jsenml.json.jackson;

import io.github.jsenml.SenmlCodecException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.json.SenmlJsonCodec;
import java.io.Reader;
import java.io.Writer;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link SenmlJsonCodec} backed by Jackson 3.
 *
 * <p>Usually obtained via {@link io.github.jsenml.json.SenmlJson#jsonCodec(String)} with name
 * {@value JacksonSenmlJsonCodecProvider#NAME}, but can be created directly to reuse a customized
 * {@link JsonMapper}.
 */
public final class JacksonSenmlJsonCodec implements SenmlJsonCodec {

  private final ObjectReader reader;
  private final ObjectWriter writer;

  /** Creates a codec using default {@link JsonMapper} configuration. */
  public JacksonSenmlJsonCodec() {
    this(JsonMapper.shared());
  }

  /**
   * Creates a codec based on given {@link ObjectMapper}. A {@link SenmlJsonJacksonModule} is
   * registered on a derived instance, the given one is not modified.
   *
   * @param mapper base mapper
   */
  public JacksonSenmlJsonCodec(JsonMapper mapper) {
    JsonMapper senmlMapper = mapper.rebuild().addModule(new SenmlJsonJacksonModule()).build();
    this.reader =
        senmlMapper.readerFor(SenmlPack.class).without(StreamReadFeature.AUTO_CLOSE_SOURCE);
    this.writer =
        senmlMapper.writerFor(SenmlPack.class).without(StreamWriteFeature.AUTO_CLOSE_TARGET);
  }

  /**
   * Encodes a pack to SenML JSON using Jackson.
   *
   * @param pack pack to encode
   * @return SenML JSON
   * @throws SenmlCodecException if encoding fails
   */
  @Override
  public String encode(SenmlPack pack) {
    try {
      return writer.writeValueAsString(pack);
    } catch (JacksonException e) {
      throw new SenmlCodecException("Failed to encode SenML pack", e);
    }
  }

  /**
   * Encodes a pack to SenML JSON using Jackson, writing it to given writer. The writer is not
   * closed.
   *
   * @param pack pack to encode
   * @param target target writer
   * @throws SenmlCodecException if encoding or writing fails
   */
  @Override
  public void encode(SenmlPack pack, Writer target) {
    try {
      writer.writeValue(target, pack);
    } catch (JacksonException e) {
      throw new SenmlCodecException("Failed to encode SenML pack", e);
    }
  }

  /**
   * Encodes a pack to UTF-8 encoded SenML JSON using Jackson, writing bytes directly without an
   * intermediate {@code String}.
   *
   * @param pack pack to encode
   * @return UTF-8 encoded SenML JSON
   * @throws SenmlCodecException if encoding fails
   */
  @Override
  public byte[] encodeToBytes(SenmlPack pack) {
    try {
      return writer.writeValueAsBytes(pack);
    } catch (JacksonException e) {
      throw new SenmlCodecException("Failed to encode SenML pack", e);
    }
  }

  /**
   * Decodes a pack from SenML JSON using Jackson.
   *
   * @param json SenML JSON
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML JSON pack
   */
  @Override
  public SenmlPack decode(String json) {
    try {
      SenmlPack pack = reader.readValue(json);
      return requireNonNull(pack);
    } catch (JacksonException | IllegalArgumentException e) {
      throw new SenmlCodecException("Failed to decode SenML pack: " + e.getMessage(), e);
    }
  }

  /**
   * Decodes a pack from SenML JSON read from given reader using Jackson. The reader is not closed.
   *
   * @param source source reader
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML JSON pack or reading fails
   */
  @Override
  public SenmlPack decode(Reader source) {
    try {
      SenmlPack pack = reader.readValue(source);
      return requireNonNull(pack);
    } catch (JacksonException | IllegalArgumentException e) {
      throw new SenmlCodecException("Failed to decode SenML pack: " + e.getMessage(), e);
    }
  }

  /**
   * Decodes a pack from UTF-8 encoded SenML JSON using Jackson, parsing bytes directly without an
   * intermediate {@code String}.
   *
   * @param json UTF-8 encoded SenML JSON
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML JSON pack
   */
  @Override
  public SenmlPack decode(byte[] json) {
    try {
      SenmlPack pack = reader.readValue(json);
      return requireNonNull(pack);
    } catch (JacksonException | IllegalArgumentException e) {
      throw new SenmlCodecException("Failed to decode SenML pack: " + e.getMessage(), e);
    }
  }

  private SenmlPack requireNonNull(@Nullable SenmlPack pack) {
    if (pack == null) {
      throw new SenmlCodecException("Failed to decode SenML pack: input is null");
    }
    return pack;
  }
}
