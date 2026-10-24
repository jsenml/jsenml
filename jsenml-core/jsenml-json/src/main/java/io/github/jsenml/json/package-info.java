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
 * SenML JSON representation ({@code application/senml+json}): the {@code SenmlJsonCodec}
 * abstraction and the {@code SenmlJson} entry point.
 *
 * <p>Codec implementations are provided by backend modules (e.g. {@code jsenml-json-jackson} or
 * {@code jsenml-json-gson}) through {@code io.github.jsenml.json.spi.SenmlJsonCodecProvider}.
 */
@NullMarked
package io.github.jsenml.json;

import org.jspecify.annotations.NullMarked;
