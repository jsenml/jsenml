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

package io.github.jsenml.cbor.spi;

import io.github.jsenml.SenmlCodecProvider;
import io.github.jsenml.cbor.SenmlCborCodec;

/**
 * Service provider interface for SenML CBOR codec backends, discovered via {@link
 * java.util.ServiceLoader}. Each SenML representation has its own provider SPI; this one provides
 * {@link SenmlCborCodec}s only.
 *
 * <p>Backends register implementations either in {@code
 * META-INF/services/io.github.jsenml.cbor.spi.SenmlCborCodecProvider} (class path) or with a {@code
 * provides} clause in {@code module-info.java} (module path). Implementations must have a public
 * no-arg constructor.
 *
 * @see io.github.jsenml.cbor.SenmlCbor
 */
public interface SenmlCborCodecProvider extends SenmlCodecProvider<SenmlCborCodec> {

  /**
   * Returns unique, stable name of this backend (e.g. {@code "jackson"}), used to select it
   * explicitly via {@link io.github.jsenml.cbor.SenmlCbor#cborCodec(String)}.
   *
   * @return backend name
   */
  @Override
  String name();

  /**
   * Creates a new CBOR codec instance backed by this provider.
   *
   * @return new codec
   */
  @Override
  SenmlCborCodec create();
}
