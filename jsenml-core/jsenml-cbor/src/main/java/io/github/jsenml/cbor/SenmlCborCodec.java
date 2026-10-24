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

package io.github.jsenml.cbor;

import io.github.jsenml.SenmlCodecException;
import io.github.jsenml.SenmlPack;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Encodes and decodes {@link SenmlPack}s to and from SenML CBOR representation ({@value
 * #MEDIA_TYPE}), as defined in <a href="https://www.rfc-editor.org/rfc/rfc8428#section-6">RFC 8428,
 * Section 6</a>.
 *
 * <p>This codec handles CBOR only. Registered SenML labels are encoded as integer map keys (see
 * {@link SenmlCborFormat}), the data value ({@code vd}) as a byte string, and numbers in their most
 * compact exact form (integer, single or double precision float).
 *
 * <p>Implementations are provided by backend modules (e.g. {@code jsenml-cbor-jackson}) and
 * discovered through {@link io.github.jsenml.cbor.spi.SenmlCborCodecProvider}. Obtain one via
 * {@link SenmlCbor#cborCodec()}. Implementations must be thread-safe.
 *
 * <p>Text-string labels not defined by RFC 8428 with a text string, number, boolean or byte string
 * value are decoded as record extensions (see {@link io.github.jsenml.SenmlExtensions}), including
 * those that are mandatory to understand (ending with {@code _}); whether those are acceptable is
 * checked by {@link SenmlPack#resolve()}. Other unknown text-string labels are ignored, unless they
 * are mandatory to understand, which causes the whole pack to be rejected, as required by RFC 8428,
 * Section 4.4. Unknown integer labels are rejected, as the set of integer labels is closed (RFC
 * 8428, Section 6). Extensions are encoded as text-string labels.
 */
public interface SenmlCborCodec {

  /** Media type of SenML CBOR representation, as registered by RFC 8428. */
  String MEDIA_TYPE = "application/senml+cbor";

  /**
   * Encodes a pack to SenML CBOR.
   *
   * @param pack pack to encode
   * @return SenML CBOR
   * @throws SenmlCodecException if encoding fails
   */
  byte[] encode(SenmlPack pack);

  /**
   * Encodes a pack to SenML CBOR, writing it to given stream. The stream is not closed.
   *
   * @param pack pack to encode
   * @param output target stream
   * @throws SenmlCodecException if encoding or writing fails
   */
  void encode(SenmlPack pack, OutputStream output);

  /**
   * Decodes a pack from SenML CBOR.
   *
   * @param cbor SenML CBOR
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML CBOR pack
   */
  SenmlPack decode(byte[] cbor);

  /**
   * Decodes a pack from SenML CBOR read from given stream. The stream is not closed.
   *
   * @param input source stream
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML CBOR pack or reading fails
   */
  SenmlPack decode(InputStream input);
}
