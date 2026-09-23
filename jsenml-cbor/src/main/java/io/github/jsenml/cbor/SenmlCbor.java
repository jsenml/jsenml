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

import io.github.jsenml.SenmlException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.cbor.spi.SenmlCborCodecProvider;
import io.github.jsenml.internal.CodecRegistry;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Entry point of SenML CBOR representation, giving access to {@link SenmlCborCodec}s discovered via
 * {@link ServiceLoader} from {@link SenmlCborCodecProvider} services.
 *
 * <p>Add a backend module (e.g. {@code jsenml-cbor-jackson}) to the class path or module path. If
 * multiple backends are available, the one with highest priority is used by default; a specific one
 * can be selected by name.
 *
 * <p>Providers are discovered once, lazily, using the class loader of this class. Codecs are
 * created once per provider and cached.
 */
public final class SenmlCbor {

  private static final CodecRegistry<SenmlCborCodecProvider, SenmlCborCodec> CBOR =
      new CodecRegistry<>(
          SenmlCborCodecProvider.class,
          classLoader -> ServiceLoader.load(SenmlCborCodecProvider.class, classLoader),
          SenmlCbor.class.getClassLoader(),
          "jsenml-cbor-jackson");

  /**
   * Returns the default CBOR codec, backed by the available provider with highest priority.
   *
   * @return default CBOR codec
   * @throws SenmlException if no CBOR provider is available
   */
  public static SenmlCborCodec cborCodec() {
    return CBOR.codec();
  }

  /**
   * Returns the CBOR codec backed by provider with given name.
   *
   * @param name provider name, e.g. {@code "jackson"}
   * @return CBOR codec of given provider
   * @throws SenmlException if no CBOR provider with given name is available
   */
  public static SenmlCborCodec cborCodec(String name) {
    return CBOR.codec(name);
  }

  /**
   * Encodes a pack to SenML CBOR using the default CBOR codec.
   *
   * @param pack pack to encode
   * @return SenML CBOR
   * @throws SenmlException if no CBOR provider is available or encoding fails
   * @see #cborCodec()
   */
  public static byte[] toCbor(SenmlPack pack) {
    return cborCodec().encode(pack);
  }

  /**
   * Decodes a pack from SenML CBOR using the default CBOR codec.
   *
   * @param cbor SenML CBOR
   * @return decoded pack
   * @throws SenmlException if no CBOR provider is available or decoding fails
   * @see #cborCodec()
   */
  public static SenmlPack fromCbor(byte[] cbor) {
    return cborCodec().decode(cbor);
  }

  /**
   * Returns available CBOR codec providers, ordered from highest to lowest priority.
   *
   * @return unmodifiable list of providers
   * @throws SenmlException if provider discovery fails
   */
  static List<SenmlCborCodecProvider> getCborCodecProviders() {
    return CBOR.getProviders();
  }

  private SenmlCbor() {}
}
