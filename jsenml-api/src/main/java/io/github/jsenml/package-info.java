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

/**
 * Framework-agnostic SenML (<a href="https://www.rfc-editor.org/rfc/rfc8428">RFC 8428</a>) model:
 * immutable {@code SenmlPack} and {@code SenmlRecord} with their builders, extensions, resolution
 * and SenML labels.
 *
 * <p>This package has no serialization dependencies. Each SenML representation has its own module
 * with codec, provider SPI and entry point ({@code jsenml-json} with {@code SenmlJson}, {@code
 * jsenml-cbor} with {@code SenmlCbor}, {@code jsenml-xml} with {@code SenmlXml}), implemented by
 * backend modules (e.g. {@code jsenml-json-jackson}).
 */
@NullMarked
package io.github.jsenml;

import org.jspecify.annotations.NullMarked;
