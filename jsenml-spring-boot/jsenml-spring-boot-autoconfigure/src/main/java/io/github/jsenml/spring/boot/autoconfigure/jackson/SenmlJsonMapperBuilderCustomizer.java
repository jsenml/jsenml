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

import io.github.jsenml.json.jackson.SenmlJsonJacksonModule;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Customizes Spring Boot's Jackson {@code JsonMapper} by registering a {@link
 * SenmlJsonJacksonModule}, so that SenML packs and records are serialized and deserialized in SenML
 * JSON representation.
 */
public interface SenmlJsonMapperBuilderCustomizer extends JsonMapperBuilderCustomizer {

  /**
   * Customize the JsonMapper.Builder.
   *
   * @param jsonMapperBuilder the builder to customize
   */
  @Override
  void customize(JsonMapper.Builder jsonMapperBuilder);
}
