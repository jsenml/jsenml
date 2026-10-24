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

package io.github.jsenml.spring.boot.autoconfigure.springdoc;

import io.github.jsenml.spring.boot.autoconfigure.SenmlProperties;
import io.swagger.v3.core.converter.ModelConverter;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot autoconfiguration describing SenML packs and records in springdoc-openapi documents
 * in their SenML JSON representation. Active when springdoc-openapi is on the classpath, unless
 * {@code jsenml.enabled} is {@code false}.
 */
@AutoConfiguration
@EnableConfigurationProperties(SenmlProperties.class)
@ConditionalOnBooleanProperty(name = SenmlProperties.ENABLED_PROPERTY, matchIfMissing = true)
@ConditionalOnClass({ModelConverter.class, SpringDocConfiguration.class})
public final class SenmlSpringDocAutoConfiguration {

  /** Creates a new instance of this autoconfiguration. */
  public SenmlSpringDocAutoConfiguration() {}

  /**
   * Creates the default {@link SenmlModelConverter}, picked up by springdoc-openapi as a {@link
   * ModelConverter} bean.
   *
   * @return a new {@code SenmlModelConverter} bean
   */
  @Bean
  @ConditionalOnMissingBean(SenmlModelConverter.class)
  SenmlModelConverter senmlModelConverter() {
    return new DefaultSenmlModelConverter();
  }
}
