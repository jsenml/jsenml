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
 * SenML XML representation ({@code application/senml+xml}): the {@code SenmlXmlCodec} abstraction.
 *
 * <p>Codec implementations are provided by backend modules (e.g. {@code jsenml-xml-jackson})
 * through {@code io.github.jsenml.xml.spi.SenmlXmlCodecProvider}.
 */
@NullMarked
package io.github.jsenml.xml;

import org.jspecify.annotations.NullMarked;
