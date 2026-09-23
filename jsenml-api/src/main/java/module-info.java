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
 * Framework-agnostic SenML (RFC 8428) API.
 *
 * <p>Exports the SenML model ({@code SenmlPack}, {@code SenmlRecord}), SenML labels and the base
 * codec provider interface. Each representation lives in its own module with its own codec, SPI and
 * entry point ({@code io.github.jsenml.json}, {@code io.github.jsenml.cbor}, {@code
 * io.github.jsenml.xml}).
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8428">RFC 8428: Sensor Measurement Lists
 *     (SenML)</a>
 */
@NullMarked
module io.github.jsenml {
  requires static transitive org.jspecify;

  exports io.github.jsenml;
  exports io.github.jsenml.internal to
      io.github.jsenml.json,
      io.github.jsenml.cbor;
}
