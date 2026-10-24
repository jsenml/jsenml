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

package io.github.jsenml;

/**
 * Base of service provider interfaces for SenML codec backends, discovered via {@link
 * java.util.ServiceLoader}. Each SenML representation has its own provider SPI extending this one
 * (e.g. {@code SenmlJsonCodecProvider}), so a backend never has to guess the format it handles.
 *
 * <p>Implementations must have a public no-arg constructor.
 *
 * @param <C> codec type created by this provider
 */
public interface SenmlCodecProvider<C> {

  /**
   * Returns unique, stable name of this backend (e.g. {@code "jackson"}), used to select it
   * explicitly.
   *
   * @return backend name
   */
  String name();

  /**
   * Returns priority of this backend. When multiple backends are available, the one with highest
   * priority becomes the default. Ties are broken by {@link #name()}.
   *
   * @return backend priority
   */
  default int getPriority() {
    return 0;
  }

  /**
   * Creates a new codec instance backed by this provider.
   *
   * @return new codec
   */
  C create();
}
