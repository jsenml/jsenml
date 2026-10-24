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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.jsenml.SenmlCodecException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.xml.SenmlXmlCodec;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.jspecify.annotations.Nullable;

/**
 * {@link SenmlXmlCodec} backed by Jackson 2.
 *
 * <p>Usually obtained via {@link io.github.jsenml.xml.SenmlXml#xmlCodec(String)} with name {@value
 * JacksonSenmlXmlCodecProvider#NAME}, but can be created directly to reuse a customized {@link
 * XmlMapper}.
 */
public final class JacksonSenmlXmlCodec implements SenmlXmlCodec {

  private final ObjectReader reader;
  private final ObjectWriter writer;

  /** Creates a codec using default {@link XmlMapper} configuration. */
  public JacksonSenmlXmlCodec() {
    this(new XmlMapper());
  }

  /**
   * Creates a codec based on given {@link XmlMapper}. A {@link SenmlXmlJacksonModule} is registered
   * on a derived instance, the given one is not modified.
   *
   * @param mapper base mapper
   */
  public JacksonSenmlXmlCodec(XmlMapper mapper) {
    XmlMapper senmlMapper = mapper.copy();
    senmlMapper.registerModule(new SenmlXmlJacksonModule());
    this.reader =
        senmlMapper.readerFor(SenmlPack.class).without(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    this.writer =
        senmlMapper.writerFor(SenmlPack.class).without(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
  }

  /**
   * Encodes a pack to SenML XML using Jackson.
   *
   * @param pack pack to encode
   * @return SenML XML
   * @throws SenmlCodecException if encoding fails
   */
  @Override
  public String encode(SenmlPack pack) {
    try {
      return writer.writeValueAsString(pack);
    } catch (IOException e) {
      throw new SenmlCodecException("Failed to encode SenML pack", e);
    }
  }

  /**
   * Encodes a pack to SenML XML using Jackson, writing it to given writer. The writer is not
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
    } catch (IOException e) {
      throw new SenmlCodecException("Failed to encode SenML pack", e);
    }
  }

  /**
   * Encodes a pack to UTF-8 encoded SenML XML using Jackson, writing bytes directly without an
   * intermediate {@code String}.
   *
   * @param pack pack to encode
   * @return UTF-8 encoded SenML XML
   * @throws SenmlCodecException if encoding fails
   */
  @Override
  public byte[] encodeToBytes(SenmlPack pack) {
    try {
      return writer.writeValueAsBytes(pack);
    } catch (IOException e) {
      throw new SenmlCodecException("Failed to encode SenML pack", e);
    }
  }

  /**
   * Decodes a pack from SenML XML using Jackson.
   *
   * @param xml SenML XML
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML XML pack
   */
  @Override
  public SenmlPack decode(String xml) {
    try {
      SenmlPack pack = reader.readValue(xml);
      return requireNonNull(pack);
    } catch (IOException | IllegalArgumentException e) {
      throw new SenmlCodecException("Failed to decode SenML pack: " + e.getMessage(), e);
    }
  }

  /**
   * Decodes a pack from SenML XML read from given reader using Jackson. The reader is not closed.
   *
   * @param source source reader
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML XML pack or reading fails
   */
  @Override
  public SenmlPack decode(Reader source) {
    try {
      SenmlPack pack = reader.readValue(source);
      return requireNonNull(pack);
    } catch (IOException | IllegalArgumentException e) {
      throw new SenmlCodecException("Failed to decode SenML pack: " + e.getMessage(), e);
    }
  }

  /**
   * Decodes a pack from UTF-8 encoded SenML XML using Jackson, parsing bytes directly without an
   * intermediate {@code String}.
   *
   * @param xml UTF-8 encoded SenML XML
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML XML pack
   */
  @Override
  public SenmlPack decode(byte[] xml) {
    try {
      SenmlPack pack = reader.readValue(xml);
      return requireNonNull(pack);
    } catch (IOException | IllegalArgumentException e) {
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
