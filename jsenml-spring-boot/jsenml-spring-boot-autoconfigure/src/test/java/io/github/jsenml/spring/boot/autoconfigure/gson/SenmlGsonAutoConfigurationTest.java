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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.json.gson.SenmlTypeAdapterFactory;
import io.github.jsenml.spring.boot.autoconfigure.SenmlProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.gson.autoconfigure.GsonAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class SenmlGsonAutoConfigurationTest {

  private static final SenmlPack PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("Cel")
              .value(23.1)
              .build());

  private static final String JSON =
      "[{\"n\":\"urn:dev:ow:10e2073a01080063\",\"u\":\"Cel\",\"v\":23.1}]";

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(GsonAutoConfiguration.class, SenmlGsonAutoConfiguration.class));

  @Test
  void givenGsonOnClasspath_whenContextStarts_thenRegistersCustomizer() {
    contextRunner.run(
        context -> assertThat(context).hasSingleBean(SenmlGsonBuilderCustomizer.class));
  }

  @Test
  void givenGsonOnClasspath_whenContextStarts_thenGsonUsesSenmlJson() {
    contextRunner.run(
        context -> {
          Gson gson = context.getBean(Gson.class);

          assertThat(gson.toJson(PACK, SenmlPack.class)).isEqualTo(JSON);
          assertThat(gson.fromJson(JSON, SenmlPack.class)).isEqualTo(PACK);
        });
  }

  @Test
  void givenUserDefinedCustomizer_whenContextStarts_thenBacksOff() {
    contextRunner
        .withUserConfiguration(CustomCustomizerConfiguration.class)
        .run(
            context -> {
              assertThat(context).hasSingleBean(SenmlGsonBuilderCustomizer.class);
              assertThat(context.getBean(SenmlGsonBuilderCustomizer.class))
                  .isSameAs(context.getBean("senmlGsonBuilderCustomizerExt"));
            });
  }

  @Test
  void givenJsenmlGsonNotOnClasspath_whenContextStarts_thenDoesNotRegisterCustomizer() {
    contextRunner
        .withClassLoader(new FilteredClassLoader(SenmlTypeAdapterFactory.class))
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).doesNotHaveBean(SenmlGsonBuilderCustomizer.class);
              assertThat(context).hasSingleBean(Gson.class);
            });
  }

  @Test
  void givenGsonNotOnClasspath_whenContextStarts_thenDoesNotRegisterCustomizer() {
    contextRunner
        .withClassLoader(new FilteredClassLoader(Gson.class))
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).doesNotHaveBean(SenmlGsonBuilderCustomizer.class);
            });
  }

  @Test
  void givenJsenmlDisabled_whenContextStarts_thenDoesNotRegisterCustomizer() {
    contextRunner
        .withPropertyValues(SenmlProperties.ENABLED_PROPERTY + "=false")
        .run(context -> assertThat(context).doesNotHaveBean(SenmlGsonBuilderCustomizer.class));
  }

  @Configuration
  static class CustomCustomizerConfiguration {

    @Bean
    SenmlGsonBuilderCustomizer senmlGsonBuilderCustomizerExt() {
      return new SenmlGsonBuilderCustomizerExt();
    }
  }
}
