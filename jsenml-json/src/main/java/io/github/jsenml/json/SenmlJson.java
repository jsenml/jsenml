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

import io.github.jsenml.SenmlException;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.internal.CodecRegistry;
import io.github.jsenml.json.spi.SenmlJsonCodecProvider;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Entry point of SenML JSON representation, giving access to {@link SenmlJsonCodec}s discovered via
 * {@link ServiceLoader} from {@link SenmlJsonCodecProvider} services.
 *
 * <p>Add a backend module (e.g. {@code jsenml-json-jackson} or {@code jsenml-json-gson}) to the
 * class path or module path. If multiple backends are available, the one with highest priority is
 * used by default; a specific one can be selected by name.
 *
 * <p>Providers are discovered once, lazily, using the class loader of this class. Codecs are
 * created once per provider and cached.
 *
 * <pre>{@code
 * SenmlPack pack =
 *     SenmlPack.of(
 *         SenmlRecord.builder()
 *             .name("urn:dev:ow:10e2073a01080063")
 *             .unit("Cel")
 *             .value(23.1)
 *             .build());
 *
 * String json = SenmlJson.toJson(pack);
 * }</pre>
 */
public final class SenmlJson {

  private static final CodecRegistry<SenmlJsonCodecProvider, SenmlJsonCodec> JSON =
      new CodecRegistry<>(
          SenmlJsonCodecProvider.class,
          classLoader -> ServiceLoader.load(SenmlJsonCodecProvider.class, classLoader),
          SenmlJson.class.getClassLoader(),
          "jsenml-json-jackson or jsenml-json-gson");

  /**
   * Returns the default JSON codec, backed by the available provider with highest priority.
   *
   * @return default JSON codec
   * @throws SenmlException if no JSON provider is available
   */
  public static SenmlJsonCodec jsonCodec() {
    return JSON.codec();
  }

  /**
   * Returns the JSON codec backed by provider with given name.
   *
   * @param name provider name, e.g. {@code "jackson"} or {@code "gson"}
   * @return JSON codec of given provider
   * @throws SenmlException if no JSON provider with given name is available
   */
  public static SenmlJsonCodec jsonCodec(String name) {
    return JSON.codec(name);
  }

  /**
   * Encodes a pack to SenML JSON using the default JSON codec.
   *
   * @param pack pack to encode
   * @return SenML JSON
   * @throws SenmlException if no JSON provider is available or encoding fails
   * @see #jsonCodec()
   */
  public static String toJson(SenmlPack pack) {
    return jsonCodec().encode(pack);
  }

  /**
   * Encodes a pack to SenML JSON using the default JSON codec.
   *
   * @param pack pack to encode
   * @return SenML JSON as bytes
   * @throws SenmlException if no JSON provider is available or encoding fails
   * @see #jsonCodec()
   */
  public static byte[] toJsonBytes(SenmlPack pack) {
    return jsonCodec().encodeToBytes(pack);
  }

  /**
   * Decodes a pack from SenML JSON using the default JSON codec.
   *
   * @param json SenML JSON
   * @return decoded pack
   * @throws SenmlException if no JSON provider is available or decoding fails
   * @see #jsonCodec()
   */
  public static SenmlPack fromJson(String json) {
    return jsonCodec().decode(json);
  }

  /**
   * Decodes a pack from SenML JSON using the default JSON codec.
   *
   * @param json SenML JSON as bytes
   * @return decoded pack
   * @throws SenmlException if no JSON provider is available or decoding fails
   * @see #jsonCodec()
   */
  public static SenmlPack fromJson(byte[] json) {
    return jsonCodec().decode(json);
  }

  /**
   * Returns available JSON codec providers, ordered from highest to lowest priority.
   *
   * @return unmodifiable list of providers
   * @throws SenmlException if provider discovery fails
   */
  static List<SenmlJsonCodecProvider> getJsonCodecProviders() {
    return JSON.getProviders();
  }

  /**
   * Discovers JSON codec providers visible to given class loader, bypassing the cache used by
   * {@link #jsonCodec()}. Providers with duplicate names are dropped in favour of the one with
   * higher priority.
   *
   * @param classLoader class loader to discover providers with
   * @return unmodifiable list of providers, ordered from highest to lowest priority
   * @throws SenmlException if provider discovery fails
   */
  static List<SenmlJsonCodecProvider> loadJsonCodecProviders(ClassLoader classLoader) {
    return JSON.loadProviders(classLoader);
  }

  private SenmlJson() {}
}
