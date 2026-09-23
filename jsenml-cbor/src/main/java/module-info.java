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

import org.jspecify.annotations.NullMarked;

/**
 * SenML CBOR representation (application/senml+cbor) of JSenML, SenML (RFC 8428) library for Java:
 * codec abstraction, provider SPI and the entry point discovering backends.
 *
 * @see io.github.jsenml.cbor.SenmlCbor
 */
@NullMarked
module io.github.jsenml.cbor {
  requires static transitive org.jspecify;
  requires transitive io.github.jsenml;

  exports io.github.jsenml.cbor;
  exports io.github.jsenml.cbor.spi;

  uses io.github.jsenml.cbor.spi.SenmlCborCodecProvider;
}
