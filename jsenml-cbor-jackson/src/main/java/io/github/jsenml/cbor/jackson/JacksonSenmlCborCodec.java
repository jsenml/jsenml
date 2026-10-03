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

import io.github.jsenml.SenmlCodecException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.cbor.SenmlCborCodec;
import java.io.InputStream;
import java.io.OutputStream;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.dataformat.cbor.CBORMapper;

/**
 * {@link SenmlCborCodec} backed by Jackson 3.
 *
 * <p>Usually obtained via {@link io.github.jsenml.cbor.SenmlCbor#cborCodec(String)} with name
 * {@value JacksonSenmlCborCodecProvider#NAME}, but can be created directly to reuse a customized
 * {@link CBORMapper}.
 */
public final class JacksonSenmlCborCodec implements SenmlCborCodec {

  private final ObjectReader reader;
  private final ObjectWriter writer;

  /** Creates a codec using default {@link CBORMapper} configuration. */
  public JacksonSenmlCborCodec() {
    this(CBORMapper.shared());
  }

  /**
   * Creates a codec based on given {@link CBORMapper}. A {@link SenmlCborJacksonModule} is
   * registered on a derived instance, the given one is not modified.
   *
   * @param mapper base mapper
   */
  public JacksonSenmlCborCodec(CBORMapper mapper) {
    CBORMapper senmlMapper = mapper.rebuild().addModule(new SenmlCborJacksonModule()).build();
    this.reader =
        senmlMapper.readerFor(SenmlPack.class).without(StreamReadFeature.AUTO_CLOSE_SOURCE);
    this.writer =
        senmlMapper.writerFor(SenmlPack.class).without(StreamWriteFeature.AUTO_CLOSE_TARGET);
  }

  /**
   * Encodes a pack to SenML CBOR using Jackson.
   *
   * @param pack pack to encode
   * @return SenML CBOR
   * @throws SenmlCodecException if encoding fails
   */
  @Override
  public byte[] encode(SenmlPack pack) {
    try {
      return writer.writeValueAsBytes(pack);
    } catch (JacksonException | IllegalArgumentException e) {
      throw new SenmlCodecException("Failed to encode SenML pack", e);
    }
  }

  /**
   * Encodes a pack to SenML CBOR using Jackson, writing it to given stream. The stream is not
   * closed.
   *
   * @param pack pack to encode
   * @param output target stream
   * @throws SenmlCodecException if encoding or writing fails
   */
  @Override
  public void encode(SenmlPack pack, OutputStream output) {
    try {
      writer.writeValue(output, pack);
    } catch (JacksonException | IllegalArgumentException e) {
      throw new SenmlCodecException("Failed to encode SenML pack", e);
    }
  }

  /**
   * Decodes a pack from SenML CBOR using Jackson.
   *
   * @param cbor SenML CBOR
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML CBOR pack
   */
  @Override
  public SenmlPack decode(byte[] cbor) {
    try {
      SenmlPack pack = reader.readValue(cbor);
      return requireNonNull(pack);
    } catch (JacksonException | IllegalArgumentException e) {
      throw new SenmlCodecException("Failed to decode SenML pack: " + e.getMessage(), e);
    }
  }

  /**
   * Decodes a pack from SenML CBOR read from given stream using Jackson. The stream is not closed.
   *
   * @param input source stream
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML CBOR pack or reading fails
   */
  @Override
  public SenmlPack decode(InputStream input) {
    try {
      SenmlPack pack = reader.readValue(input);
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
