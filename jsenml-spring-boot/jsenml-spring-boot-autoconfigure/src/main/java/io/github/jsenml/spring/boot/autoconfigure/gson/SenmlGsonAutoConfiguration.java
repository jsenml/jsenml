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

import com.google.gson.Gson;
import io.github.jsenml.json.gson.SenmlTypeAdapterFactory;
import io.github.jsenml.spring.boot.autoconfigure.SenmlProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.gson.autoconfigure.GsonBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot autoconfiguration registering SenML JSON support in Spring Boot's {@code Gson}.
 * Active when Spring Boot's Gson support and {@code jsenml-json-gson} are on the classpath, unless
 * {@code jsenml.enabled} is {@code false}.
 */
@AutoConfiguration
@EnableConfigurationProperties(SenmlProperties.class)
@ConditionalOnBooleanProperty(name = SenmlProperties.ENABLED_PROPERTY, matchIfMissing = true)
@ConditionalOnClass({GsonBuilderCustomizer.class, Gson.class, SenmlTypeAdapterFactory.class})
public final class SenmlGsonAutoConfiguration {

  /** Creates a new instance of this autoconfiguration. */
  public SenmlGsonAutoConfiguration() {}

  /**
   * Creates a {@link SenmlGsonBuilderCustomizer} registering the {@link SenmlTypeAdapterFactory} in
   * the Gson builder.
   *
   * @return a new {@code SenmlGsonBuilderCustomizer} bean
   */
  @Bean
  @ConditionalOnMissingBean(SenmlGsonBuilderCustomizer.class)
  SenmlGsonBuilderCustomizer senmlGsonBuilderCustomizer() {
    return gsonBuilder -> gsonBuilder.registerTypeAdapterFactory(new SenmlTypeAdapterFactory());
  }
}
