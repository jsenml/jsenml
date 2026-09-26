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

package io.github.jsenml.xml;

import io.github.jsenml.SenmlCodecException;
import io.github.jsenml.SenmlPack;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Encodes and decodes {@link SenmlPack}s to and from SenML XML representation ({@value
 * #MEDIA_TYPE}), as defined in <a href="https://www.rfc-editor.org/rfc/rfc8428#section-7">RFC 8428,
 * Section 7</a>.
 *
 * <p>A pack is a {@value #PACK_ELEMENT} element in the {@value #NAMESPACE} namespace, containing a
 * {@value #RECORD_ELEMENT} element per record, with fields as attributes named by SenML labels (see
 * {@link io.github.jsenml.SenmlTextFormat}). Only the UTF-8 form of XML is allowed.
 *
 * <p>Implementations are provided by backend modules (e.g. {@code jsenml-xml-jackson}) and
 * discovered through {@link io.github.jsenml.xml.spi.SenmlXmlCodecProvider}. Obtain one via {@link
 * SenmlXml#xmlCodec()}. Implementations must be thread-safe and must not resolve external entities.
 *
 * <p>Attributes not defined by RFC 8428 with a valid extension name are decoded as string
 * extensions (see {@link io.github.jsenml.SenmlExtensions}), as XML attributes carry no type,
 * including those that are mandatory to understand (ending with {@code _}); whether those are
 * acceptable is checked by {@link SenmlPack#resolve()}. Other unknown attributes and child elements
 * are ignored. Extensions are encoded as attributes, octets as base64url strings.
 */
public interface SenmlXmlCodec {

  /** Media type of SenML XML representation, as registered by RFC 8428. */
  String MEDIA_TYPE = "application/senml+xml";

  /** XML namespace of SenML elements. */
  String NAMESPACE = "urn:ietf:params:xml:ns:senml";

  /** Name of the root element, representing a pack. */
  String PACK_ELEMENT = "sensml";

  /** Name of the element representing a record. */
  String RECORD_ELEMENT = "senml";

  /**
   * Encodes a pack to SenML XML.
   *
   * @param pack pack to encode
   * @return SenML XML
   * @throws SenmlCodecException if encoding fails
   */
  String encode(SenmlPack pack);

  /**
   * Encodes a pack to SenML XML, writing it to given writer. The writer is not closed.
   *
   * @param pack pack to encode
   * @param writer target writer
   * @throws SenmlCodecException if encoding or writing fails
   */
  void encode(SenmlPack pack, Writer writer);

  /**
   * Encodes a pack to UTF-8 encoded SenML XML.
   *
   * @param pack pack to encode
   * @return UTF-8 encoded SenML XML
   * @throws SenmlCodecException if encoding fails
   */
  default byte[] encodeToBytes(SenmlPack pack) {
    return encode(pack).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Decodes a pack from SenML XML.
   *
   * @param xml SenML XML
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML XML pack
   */
  SenmlPack decode(String xml);

  /**
   * Decodes a pack from SenML XML read from given reader. The reader is not closed.
   *
   * @param reader source reader
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML XML pack or reading fails
   */
  SenmlPack decode(Reader reader);

  /**
   * Decodes a pack from UTF-8 encoded SenML XML.
   *
   * @param xml UTF-8 encoded SenML XML
   * @return decoded pack
   * @throws SenmlCodecException if input is not a valid SenML XML pack
   */
  default SenmlPack decode(byte[] xml) {
    return decode(new String(xml, StandardCharsets.UTF_8));
  }
}
