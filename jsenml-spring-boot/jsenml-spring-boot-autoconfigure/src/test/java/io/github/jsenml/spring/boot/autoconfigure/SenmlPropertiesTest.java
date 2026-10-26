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

package io.github.jsenml.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jsenml.spring.boot.autoconfigure.gson.SenmlGsonAutoConfiguration;
import io.github.jsenml.spring.boot.autoconfigure.jackson.SenmlJacksonAutoConfiguration;
import io.github.jsenml.spring.boot.autoconfigure.springdoc.SenmlSpringDocAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SenmlPropertiesTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  SenmlGsonAutoConfiguration.class,
                  SenmlJacksonAutoConfiguration.class,
                  SenmlSpringDocAutoConfiguration.class));

  @Test
  void givenNoProperties_whenContextStarts_thenRegistersEnabledProperties() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(SenmlProperties.class);
          assertThat(context.getBean(SenmlProperties.class).isEnabled()).isTrue();
          assertThat(context.getBean(SenmlProperties.class).getConverter().getCbor().isEnabled())
              .isTrue();
        });
  }

  @Test
  void givenEnabledExplicitly_whenContextStarts_thenBindsIt() {
    contextRunner
        .withPropertyValues(SenmlProperties.ENABLED_PROPERTY + "=true")
        .run(context -> assertThat(context.getBean(SenmlProperties.class).isEnabled()).isTrue());
  }

  @Test
  void givenCborConverterDisabled_whenContextStarts_thenBindsIt() {
    contextRunner
        .withPropertyValues(SenmlProperties.CONVERTER_CBOR_ENABLED_PROPERTY + "=false")
        .run(
            context ->
                assertThat(
                        context.getBean(SenmlProperties.class).getConverter().getCbor().isEnabled())
                    .isFalse());
  }

  @Test
  void givenDisabled_whenContextStarts_thenRegistersNoJsenmlBeans() {
    contextRunner
        .withPropertyValues(SenmlProperties.ENABLED_PROPERTY + "=false")
        .run(context -> assertThat(context).doesNotHaveBean(SenmlProperties.class));
  }
}
