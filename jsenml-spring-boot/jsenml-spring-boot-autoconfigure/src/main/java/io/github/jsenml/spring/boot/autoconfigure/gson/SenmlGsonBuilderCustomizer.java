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

package io.github.jsenml.spring.boot.autoconfigure.gson;

import com.google.gson.GsonBuilder;
import io.github.jsenml.json.gson.SenmlTypeAdapterFactory;
import org.springframework.boot.gson.autoconfigure.GsonBuilderCustomizer;

/**
 * Customizes Spring Boot's {@code Gson} instance by registering a {@link SenmlTypeAdapterFactory},
 * so that SenML packs and records are serialized and deserialized in SenML JSON representation.
 */
public interface SenmlGsonBuilderCustomizer extends GsonBuilderCustomizer {

  /**
   * Customize the GsonBuilder.
   *
   * @param gsonBuilder the GsonBuilder to customize
   */
  @Override
  void customize(GsonBuilder gsonBuilder);
}
