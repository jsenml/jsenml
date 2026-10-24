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

package io.github.jsenml.internal;

import io.github.jsenml.SenmlCodecProvider;
import io.github.jsenml.SenmlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Lazily discovered, priority-ordered registry of codec providers of a single SPI type, caching one
 * codec per provider. Shared by entry points of SenML representations; not intended for
 * applications.
 *
 * <p>Providers are loaded through a function supplied by the entry point, so that {@link
 * ServiceLoader} is called from the module declaring {@code uses} of the provider type.
 *
 * @param <P> provider SPI type
 * @param <C> codec type created by providers
 */
public final class CodecRegistry<P extends SenmlCodecProvider<C>, C> {

  private final Class<P> providerType;
  private final Function<ClassLoader, ServiceLoader<P>> serviceLoader;
  private final ClassLoader classLoader;
  private final String backendHint;

  private volatile @Nullable Map<String, P> providers;

  private final ConcurrentMap<String, C> codecs = new ConcurrentHashMap<>();

  /**
   * Creates a registry, discovering providers lazily on first use.
   *
   * @param providerType provider SPI type
   * @param serviceLoader creates a {@link ServiceLoader} of providers for given class loader
   * @param classLoader class loader to discover providers with
   * @param backendHint backend modules to suggest when no provider is found, e.g. {@code
   *     "jsenml-cbor-jackson"}
   */
  public CodecRegistry(
      Class<P> providerType,
      Function<ClassLoader, ServiceLoader<P>> serviceLoader,
      ClassLoader classLoader,
      String backendHint) {
    this.providerType = providerType;
    this.serviceLoader = serviceLoader;
    this.classLoader = classLoader;
    this.backendHint = backendHint;
  }

  /**
   * Returns the codec of the available provider with highest priority.
   *
   * @return default codec
   * @throws SenmlException if no provider is available
   */
  public C codec() {
    Map<String, P> available = providers();
    if (available.isEmpty()) {
      throw new SenmlException(
          "No "
              + providerType.getName()
              + " found, add a backend module to the project, e.g. "
              + backendHint);
    }
    return codec(available.keySet().iterator().next());
  }

  /**
   * Returns the codec of the provider with given name, created once and cached.
   *
   * @param name provider name
   * @return codec of given provider
   * @throws SenmlException if no provider with given name is available
   */
  public C codec(String name) {
    P provider = providers().get(name);
    if (provider == null) {
      throw new SenmlException(
          "No "
              + providerType.getName()
              + " named '"
              + name
              + "' found, available: "
              + providers().keySet());
    }
    return codecs.computeIfAbsent(name, key -> provider.create());
  }

  /**
   * Returns available providers, ordered from highest to lowest priority.
   *
   * @return unmodifiable list of providers
   * @throws SenmlException if provider discovery fails
   */
  public List<P> getProviders() {
    return List.copyOf(providers().values());
  }

  /**
   * Discovers providers visible to given class loader, bypassing the cache. Providers with
   * duplicate names are dropped in favour of the one with higher priority.
   *
   * @param classLoader class loader to discover providers with
   * @return unmodifiable list of providers, ordered from highest to lowest priority
   * @throws SenmlException if provider discovery fails
   */
  public List<P> loadProviders(ClassLoader classLoader) {
    return List.copyOf(load(classLoader).values());
  }

  private Map<String, P> providers() {
    Map<String, P> result = providers;
    if (result == null) {
      synchronized (this) {
        result = providers;
        if (result == null) {
          result = load(classLoader);
          providers = result;
        }
      }
    }
    return result;
  }

  private Map<String, P> load(ClassLoader classLoader) {
    List<P> found = new ArrayList<>();
    try {
      for (P provider : serviceLoader.apply(classLoader)) {
        found.add(provider);
      }
    } catch (ServiceConfigurationError e) {
      throw new SenmlException("Failed to load " + providerType.getName(), e);
    }
    found.sort(
        Comparator.comparingInt((P provider) -> provider.getPriority())
            .reversed()
            .thenComparing(SenmlCodecProvider::name));

    Map<String, P> byName = new LinkedHashMap<>();
    for (P provider : found) {
      byName.putIfAbsent(provider.name(), provider);
    }
    return Collections.unmodifiableMap(byName);
  }
}
