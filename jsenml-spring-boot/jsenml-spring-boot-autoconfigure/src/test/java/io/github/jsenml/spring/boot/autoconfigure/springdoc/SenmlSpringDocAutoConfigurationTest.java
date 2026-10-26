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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.spring.boot.autoconfigure.SenmlProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class SenmlSpringDocAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(SenmlSpringDocAutoConfiguration.class));

  @Test
  void givenSpringdocOnClasspath_whenContextStarts_thenRegistersConverter() {
    contextRunner.run(context -> assertThat(context).hasSingleBean(SenmlModelConverter.class));
  }

  @Test
  void givenUserDefinedConverter_whenContextStarts_thenBacksOff() {
    contextRunner
        .withUserConfiguration(CustomConverterConfiguration.class)
        .run(
            context -> {
              assertThat(context).hasSingleBean(SenmlModelConverter.class);
              assertThat(context.getBean(SenmlModelConverter.class))
                  .isSameAs(context.getBean("customSenmlModelConverter"));
            });
  }

  @Test
  void givenSpringdocNotOnClasspath_whenContextStarts_thenDoesNotRegisterConverter() {
    contextRunner
        .withClassLoader(new FilteredClassLoader(SpringDocConfiguration.class))
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).doesNotHaveBean(SenmlModelConverter.class);
            });
  }

  @Test
  void givenJsenmlDisabled_whenContextStarts_thenDoesNotRegisterConverter() {
    contextRunner
        .withPropertyValues(SenmlProperties.ENABLED_PROPERTY + "=false")
        .run(context -> assertThat(context).doesNotHaveBean(SenmlModelConverter.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"openapi_3_0", "openapi_3_1"})
  void givenSenmlTypesInController_whenGettingApiDocs_thenUsesSenmlJsonSchemas(String version) {
    new WebApplicationContextRunner()
        .withUserConfiguration(SpringdocApplication.class)
        .withPropertyValues("springdoc.api-docs.version=" + version)
        .run(
            context -> {
              MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
              String json =
                  mockMvc
                      .perform(get("/v3/api-docs"))
                      .andReturn()
                      .getResponse()
                      .getContentAsString();
              JsonNode docs = JsonMapper.shared().readTree(json);

              assertThat(
                      docs.at(
                              "/paths/~1packs/post/requestBody/content/application~1json/schema/$ref")
                          .asString())
                  .isEqualTo("#/components/schemas/SenmlPack");
              assertThat(
                      docs.at("/paths/~1records/get/responses/200/content/*~1*/schema/items/$ref")
                          .asString())
                  .isEqualTo("#/components/schemas/SenmlRecord");
              assertThat(docs.at("/components/schemas/SenmlPack/type").toString())
                  .contains("array");
              assertThat(docs.at("/components/schemas/SenmlRecord/properties/bn/type").toString())
                  .contains("string");
              assertThat(docs.at("/components/schemas/SenmlRecord/properties/v/type").toString())
                  .contains("number");
              assertThat(docs.at("/components/schemas/SenmlRecord/properties").has("baseName"))
                  .isFalse();
            });
  }

  @Configuration
  static class CustomConverterConfiguration {

    @Bean
    SenmlModelConverter customSenmlModelConverter() {
      return new DefaultSenmlModelConverter();
    }
  }

  @Configuration
  @EnableAutoConfiguration
  @Import(SenmlController.class)
  static class SpringdocApplication {}

  @RestController
  static class SenmlController {

    @PostMapping("/packs")
    SenmlPack echo(@RequestBody SenmlPack pack) {
      return pack;
    }

    @GetMapping("/records")
    List<SenmlRecord> records() {
      return List.of();
    }
  }
}
