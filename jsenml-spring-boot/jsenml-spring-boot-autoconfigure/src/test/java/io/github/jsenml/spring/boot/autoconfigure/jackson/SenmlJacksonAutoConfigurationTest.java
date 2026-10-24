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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.cbor.jackson.SenmlCborJacksonModule;
import io.github.jsenml.json.jackson.SenmlJsonJacksonModule;
import io.github.jsenml.spring.boot.autoconfigure.SenmlProperties;
import io.github.jsenml.xml.jackson.SenmlXmlJacksonModule;
import java.util.HexFormat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.cbor.CBORMapper;
import tools.jackson.dataformat.xml.XmlMapper;

class SenmlJacksonAutoConfigurationTest {

  private static final SenmlPack PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("Cel")
              .value(23.1)
              .build());

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  JacksonAutoConfiguration.class, SenmlJacksonAutoConfiguration.class));

  @Nested
  class Json {

    private static final String JSON =
        "[{\"n\":\"urn:dev:ow:10e2073a01080063\",\"u\":\"Cel\",\"v\":23.1}]";

    @Test
    void givenJacksonOnClasspath_whenContextStarts_thenRegistersCustomizer() {
      contextRunner.run(
          context -> assertThat(context).hasSingleBean(SenmlJsonMapperBuilderCustomizer.class));
    }

    @Test
    void givenJacksonOnClasspath_whenContextStarts_thenJsonMapperUsesSenmlJson() {
      contextRunner.run(
          context -> {
            JsonMapper mapper = context.getBean(JsonMapper.class);

            assertThat(mapper.writeValueAsString(PACK)).isEqualTo(JSON);
            assertThat(mapper.readValue(JSON, SenmlPack.class)).isEqualTo(PACK);
          });
    }

    @Test
    void givenUserDefinedCustomizer_whenContextStarts_thenBacksOff() {
      contextRunner
          .withUserConfiguration(CustomCustomizerConfiguration.class)
          .run(
              context -> {
                assertThat(context).hasSingleBean(SenmlJsonMapperBuilderCustomizer.class);
                assertThat(context.getBean(SenmlJsonMapperBuilderCustomizer.class))
                    .isSameAs(context.getBean("senmlJsonMapperBuilderCustomizerExt"));
              });
    }

    @Test
    void givenJsenmlJacksonNotOnClasspath_whenContextStarts_thenDoesNotRegisterCustomizer() {
      contextRunner
          .withClassLoader(new FilteredClassLoader(SenmlJsonJacksonModule.class))
          .run(
              context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(SenmlJsonMapperBuilderCustomizer.class);
                assertThat(context).hasSingleBean(JsonMapper.class);
              });
    }

    @Test
    void givenJacksonNotOnClasspath_whenContextStarts_thenDoesNotRegisterCustomizer() {
      contextRunner
          .withClassLoader(new FilteredClassLoader(JsonMapper.class))
          .run(
              context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(SenmlJsonMapperBuilderCustomizer.class);
              });
    }

    @Test
    void givenJsenmlDisabled_whenContextStarts_thenDoesNotRegisterCustomizer() {
      contextRunner
          .withPropertyValues(SenmlProperties.ENABLED_PROPERTY + "=false")
          .run(
              context ->
                  assertThat(context).doesNotHaveBean(SenmlJsonMapperBuilderCustomizer.class));
    }

    @Configuration
    static class CustomCustomizerConfiguration {

      @Bean
      SenmlJsonMapperBuilderCustomizer senmlJsonMapperBuilderCustomizerExt() {
        return new SenmlJsonMapperBuilderCustomizerExt();
      }
    }
  }

  @Nested
  class Cbor {

    private static final String CBOR =
        "81a300781b75726e3a6465763a6f773a3130653230373361303130383030"
            + "3633016343656c02fb403719999999999a";

    @Test
    void givenJacksonCborOnClasspath_whenContextStarts_thenRegistersCustomizer() {
      contextRunner.run(
          context -> assertThat(context).hasSingleBean(SenmlCborMapperBuilderCustomizer.class));
    }

    @Test
    void givenJacksonCborOnClasspath_whenContextStarts_thenCborMapperUsesSenmlCbor() {
      contextRunner.run(
          context -> {
            CBORMapper mapper = context.getBean(CBORMapper.class);

            byte[] cbor = mapper.writeValueAsBytes(PACK);

            assertThat(HexFormat.of().formatHex(cbor)).isEqualTo(CBOR);
            assertThat(mapper.readValue(cbor, SenmlPack.class)).isEqualTo(PACK);
          });
    }

    @Test
    void givenUserDefinedCustomizer_whenContextStarts_thenBacksOff() {
      contextRunner
          .withUserConfiguration(CustomCustomizerConfiguration.class)
          .run(
              context -> {
                assertThat(context).hasSingleBean(SenmlCborMapperBuilderCustomizer.class);
                assertThat(context.getBean(SenmlCborMapperBuilderCustomizer.class))
                    .isSameAs(context.getBean("senmlCborMapperBuilderCustomizerExt"));
              });
    }

    @Test
    void givenJsenmlCborJacksonNotOnClasspath_whenContextStarts_thenDoesNotRegisterCustomizer() {
      contextRunner
          .withClassLoader(new FilteredClassLoader(SenmlCborJacksonModule.class))
          .run(
              context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(SenmlCborMapperBuilderCustomizer.class);
                assertThat(context).hasSingleBean(CBORMapper.class);
              });
    }

    @Test
    void givenJacksonCborNotOnClasspath_whenContextStarts_thenDoesNotRegisterCustomizer() {
      contextRunner
          .withClassLoader(new FilteredClassLoader(CBORMapper.class))
          .run(
              context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(SenmlCborMapperBuilderCustomizer.class);
              });
    }

    @Test
    void givenJsenmlDisabled_whenContextStarts_thenDoesNotRegisterCustomizer() {
      contextRunner
          .withPropertyValues(SenmlProperties.ENABLED_PROPERTY + "=false")
          .run(
              context ->
                  assertThat(context).doesNotHaveBean(SenmlCborMapperBuilderCustomizer.class));
    }

    @Configuration
    static class CustomCustomizerConfiguration {

      @Bean
      SenmlCborMapperBuilderCustomizer senmlCborMapperBuilderCustomizerExt() {
        return new SenmlCborMapperBuilderCustomizerExt();
      }
    }
  }

  @Nested
  class Xml {

    private static final String XML =
        "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\">"
            + "<senml n=\"urn:dev:ow:10e2073a01080063\" u=\"Cel\" v=\"23.1\"/>"
            + "</sensml>";

    @Test
    void givenJacksonXmlOnClasspath_whenContextStarts_thenRegistersCustomizer() {
      contextRunner.run(
          context -> assertThat(context).hasSingleBean(SenmlXmlMapperBuilderCustomizer.class));
    }

    @Test
    void givenJacksonXmlOnClasspath_whenContextStarts_thenXmlMapperUsesSenmlXml() {
      contextRunner.run(
          context -> {
            XmlMapper mapper = context.getBean(XmlMapper.class);

            assertThat(mapper.writeValueAsString(PACK)).isEqualTo(XML);
            assertThat(mapper.readValue(XML, SenmlPack.class)).isEqualTo(PACK);
          });
    }

    @Test
    void givenUserDefinedCustomizer_whenContextStarts_thenBacksOff() {
      contextRunner
          .withUserConfiguration(CustomCustomizerConfiguration.class)
          .run(
              context -> {
                assertThat(context).hasSingleBean(SenmlXmlMapperBuilderCustomizer.class);
                assertThat(context.getBean(SenmlXmlMapperBuilderCustomizer.class))
                    .isSameAs(context.getBean("senmlXmlMapperBuilderCustomizerExt"));
              });
    }

    @Test
    void givenJsenmlXmlJacksonNotOnClasspath_whenContextStarts_thenDoesNotRegisterCustomizer() {
      contextRunner
          .withClassLoader(new FilteredClassLoader(SenmlXmlJacksonModule.class))
          .run(
              context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(SenmlXmlMapperBuilderCustomizer.class);
                assertThat(context).hasSingleBean(XmlMapper.class);
              });
    }

    @Test
    void givenJacksonXmlNotOnClasspath_whenContextStarts_thenDoesNotRegisterCustomizer() {
      contextRunner
          .withClassLoader(new FilteredClassLoader(XmlMapper.class))
          .run(
              context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(SenmlXmlMapperBuilderCustomizer.class);
              });
    }

    @Test
    void givenJsenmlDisabled_whenContextStarts_thenDoesNotRegisterCustomizer() {
      contextRunner
          .withPropertyValues(SenmlProperties.ENABLED_PROPERTY + "=false")
          .run(
              context ->
                  assertThat(context).doesNotHaveBean(SenmlXmlMapperBuilderCustomizer.class));
    }

    @Configuration
    static class CustomCustomizerConfiguration {

      @Bean
      SenmlXmlMapperBuilderCustomizer senmlXmlMapperBuilderCustomizerExt() {
        return new SenmlXmlMapperBuilderCustomizerExt();
      }
    }
  }
}
