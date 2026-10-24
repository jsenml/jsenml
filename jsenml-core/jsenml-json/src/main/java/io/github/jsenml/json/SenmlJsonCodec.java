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

package io.github.jsenml.json;

import io.github.jsenml.SenmlCodecException;
import io.github.jsenml.SenmlPack;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Encodes and decodes {@link SenmlPack}s to and from SenML JSON representation ({@value
 * #MEDIA_TYPE}), as defined in <a href="https://www.rfc-editor.org/rfc/rfc8428#section-5">RFC 8428,
 * Section 5</a>.
 *
 * <p>This codec handles JSON only. Other SenML representations (e.g. CBOR, XML, EXI) are handled by
 * separate codec types with their own provider SPI.
 *
 * <p>Implementations are provided by backend modules (e.g. {@code jsenml-json-jackson}, {@code
 * jsenml-json-gson}) and discovered through {@link
 * io.github.jsenml.json.spi.SenmlJsonCodecProvider}. Obtain one via {@link SenmlJson#jsonCodec()}.
 * Implementations must be thread-safe.
 *
 * <p>Labels not defined by RFC 8428 with a string, number or boolean value are decoded as record
 * extensions (see {@link io.github.jsenml.SenmlExtensions}), including those that are mandatory to
 * understand (ending with {@code _}); whether those are acceptable is checked by {@link
 * SenmlPack#resolve()}. Other unknown labels are ignored, unless they are mandatory to understand,
 * which causes the whole pack to be rejected, as required by RFC 8428, Section 4.4.
 */
public interface SenmlJsonCodec {

  /** Media type of SenML JSON representation, as registered by RFC 8428. */
  String MEDIA_TYPE = "application/senml+json";

  /**
   * Encodes a pack to SenML JSON.
   *
   * @param pack pack to encode
   * @return SenML JSON
   * @throws SenmlCodecException if encoding fails
   */
  String encode(SenmlPack pack);

  /**
   * Encodes a pack to SenML JSON, writing it to given writer. The writer is not closed.
   *
   * @param pack pack to encode
   * @param writer target writer
   * @throws SenmlCodecException if encoding or writing fails
   */
  void encode(SenmlPack pack, Writer writer);

  /**
   * Encodes a pack to UTF-8 encoded SenML JSON.
   *
   * @param pack pack to encode
   * @return binary representation of an encoded SenML JSON
   * @throws SenmlCodecException if encoding fails
   */
  default byte[] encodeToBytes(SenmlPack pack) {
    return encode(pack).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Decodes a pack from SenML JSON.
   *
   * @param json SenML JSON
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML JSON pack
   */
  SenmlPack decode(String json);

  /**
   * Decodes a pack from SenML JSON read from given reader. The reader is not closed.
   *
   * @param reader source reader
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML JSON pack or reading fails
   */
  SenmlPack decode(Reader reader);

  /**
   * Decodes a pack from UTF-8 encoded SenML JSON.
   *
   * @param json UTF-8 encoded SenML JSON
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML JSON pack
   */
  default SenmlPack decode(byte[] json) {
    return decode(new String(json, StandardCharsets.UTF_8));
  }
}
