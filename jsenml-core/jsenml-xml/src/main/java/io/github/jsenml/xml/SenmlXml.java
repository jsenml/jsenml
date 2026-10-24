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

import io.github.jsenml.SenmlException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.internal.CodecRegistry;
import io.github.jsenml.xml.spi.SenmlXmlCodecProvider;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Entry point of SenML XML representation, giving access to {@link SenmlXmlCodec}s discovered via
 * {@link ServiceLoader} from {@link SenmlXmlCodecProvider} services.
 *
 * <p>Add a backend module (e.g. {@code jsenml-xml-jackson}) to the class path or module path. If
 * multiple backends are available, the one with highest priority is used by default; a specific one
 * can be selected by name.
 *
 * <p>Providers are discovered once, lazily, using the class loader of this class. Codecs are
 * created once per provider and cached.
 */
public final class SenmlXml {

  private static final CodecRegistry<SenmlXmlCodecProvider, SenmlXmlCodec> XML =
      new CodecRegistry<>(
          SenmlXmlCodecProvider.class,
          classLoader -> ServiceLoader.load(SenmlXmlCodecProvider.class, classLoader),
          SenmlXml.class.getClassLoader(),
          "jsenml-xml-jackson");

  /**
   * Returns the default XML codec, backed by the available provider with highest priority.
   *
   * @return default XML codec
   * @throws SenmlException if no XML provider is available
   */
  public static SenmlXmlCodec xmlCodec() {
    return XML.codec();
  }

  /**
   * Returns the XML codec backed by provider with given name.
   *
   * @param name provider name, e.g. {@code "jackson"}
   * @return XML codec of given provider
   * @throws SenmlException if no XML provider with given name is available
   */
  public static SenmlXmlCodec xmlCodec(String name) {
    return XML.codec(name);
  }

  /**
   * Encodes a pack to SenML XML using the default XML codec.
   *
   * @param pack pack to encode
   * @return SenML XML
   * @throws SenmlException if no XML provider is available or encoding fails
   * @see #xmlCodec()
   */
  public static String toXml(SenmlPack pack) {
    return xmlCodec().encode(pack);
  }

  /**
   * Encodes a pack to UTF-8 encoded SenML XML using the default XML codec.
   *
   * @param pack pack to encode
   * @return SenML XML as bytes
   * @throws SenmlException if no XML provider is available or encoding fails
   * @see #xmlCodec()
   */
  public static byte[] toXmlBytes(SenmlPack pack) {
    return xmlCodec().encodeToBytes(pack);
  }

  /**
   * Decodes a pack from SenML XML using the default XML codec.
   *
   * @param xml SenML XML
   * @return decoded pack
   * @throws SenmlException if no XML provider is available or decoding fails
   * @see #xmlCodec()
   */
  public static SenmlPack fromXml(String xml) {
    return xmlCodec().decode(xml);
  }

  /**
   * Decodes a pack from UTF-8 encoded SenML XML using the default XML codec.
   *
   * @param xml SenML XML as bytes
   * @return decoded pack
   * @throws SenmlException if no XML provider is available or decoding fails
   * @see #xmlCodec()
   */
  public static SenmlPack fromXml(byte[] xml) {
    return xmlCodec().decode(xml);
  }

  /**
   * Returns available XML codec providers, ordered from highest to lowest priority.
   *
   * @return unmodifiable list of providers
   * @throws SenmlException if provider discovery fails
   */
  static List<SenmlXmlCodecProvider> getXmlCodecProviders() {
    return XML.getProviders();
  }

  private SenmlXml() {}
}
