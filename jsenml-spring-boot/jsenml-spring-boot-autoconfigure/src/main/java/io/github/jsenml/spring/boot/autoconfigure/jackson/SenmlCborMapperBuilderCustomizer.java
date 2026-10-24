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

package io.github.jsenml.spring.boot.autoconfigure.jackson;

import io.github.jsenml.cbor.jackson.SenmlCborJacksonModule;
import org.springframework.boot.jackson.autoconfigure.CborMapperBuilderCustomizer;
import tools.jackson.dataformat.cbor.CBORMapper;

/**
 * Customizes Spring Boot's Jackson {@code CBORMapper} by registering a {@link
 * SenmlCborJacksonModule}, so that SenML packs and records are serialized and deserialized in SenML
 * CBOR representation.
 */
public interface SenmlCborMapperBuilderCustomizer extends CborMapperBuilderCustomizer {

  /**
   * Customize the CBORMapper.Builder.
   *
   * @param cborMapperBuilder the builder to customize
   */
  @Override
  void customize(CBORMapper.Builder cborMapperBuilder);
}
