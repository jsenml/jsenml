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
 * Jackson 3 XML backend for JSenML.
 *
 * <p>Provides {@code SenmlXmlJacksonModule} for plain Jackson usage and {@code
 * JacksonSenmlXmlCodec}, registered as {@code io.github.jsenml.xml.spi.SenmlXmlCodecProvider}
 * service named {@code "jackson"}.
 */
@NullMarked
package io.github.jsenml.xml.jackson;

import org.jspecify.annotations.NullMarked;
