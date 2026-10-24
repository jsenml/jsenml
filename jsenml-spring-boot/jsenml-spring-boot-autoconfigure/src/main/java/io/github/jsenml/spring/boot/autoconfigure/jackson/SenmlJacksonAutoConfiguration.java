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
import io.github.jsenml.json.jackson.SenmlJsonJacksonModule;
import io.github.jsenml.spring.boot.autoconfigure.SenmlProperties;
import io.github.jsenml.xml.jackson.SenmlXmlJacksonModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.CborMapperBuilderCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.jackson.autoconfigure.XmlMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.cbor.CBORMapper;
import tools.jackson.dataformat.xml.XmlMapper;

/**
 * Spring Boot autoconfiguration registering SenML support in Spring Boot's Jackson mappers. Active
 * when Spring Boot's Jackson support is on the classpath, unless {@code jsenml.enabled} is {@code
 * false}. Each representation is registered in its mapper when its backend is on the classpath:
 *
 * <ul>
 *   <li>SenML JSON in {@code JsonMapper} with {@code jsenml-json-jackson},
 *   <li>SenML CBOR in {@code CBORMapper} with {@code jsenml-cbor-jackson},
 *   <li>SenML XML in {@code XmlMapper} with {@code jsenml-xml-jackson}.
 * </ul>
 *
 * <p>Starters bring {@code jsenml-json-jackson} only, so CBOR and XML are opt-in.
 */
@AutoConfiguration
@EnableConfigurationProperties(SenmlProperties.class)
@ConditionalOnBooleanProperty(name = SenmlProperties.ENABLED_PROPERTY, matchIfMissing = true)
@ConditionalOnClass(JsonMapper.class)
public final class SenmlJacksonAutoConfiguration {

  /** Creates a new instance of this autoconfiguration. */
  public SenmlJacksonAutoConfiguration() {}

  @Configuration
  @ConditionalOnClass({JsonMapperBuilderCustomizer.class, SenmlJsonJacksonModule.class})
  static class JsonConfiguration {

    /**
     * Creates a {@link SenmlJsonMapperBuilderCustomizer} registering the {@link
     * SenmlJsonJacksonModule} in the JSON mapper.
     *
     * @return a new {@code SenmlJsonMapperBuilderCustomizer} bean
     */
    @Bean
    @ConditionalOnMissingBean(SenmlJsonMapperBuilderCustomizer.class)
    SenmlJsonMapperBuilderCustomizer senmlJsonMapperBuilderCustomizer() {
      return jsonMapperBuilder -> jsonMapperBuilder.addModule(new SenmlJsonJacksonModule());
    }
  }

  @Configuration
  @ConditionalOnClass({
    CborMapperBuilderCustomizer.class,
    CBORMapper.class,
    SenmlCborJacksonModule.class
  })
  static class CborConfiguration {

    /**
     * Creates a {@link SenmlCborMapperBuilderCustomizer} registering the {@link
     * SenmlCborJacksonModule} in the CBOR mapper.
     *
     * @return a new {@code SenmlCborMapperBuilderCustomizer} bean
     */
    @Bean
    @ConditionalOnMissingBean(SenmlCborMapperBuilderCustomizer.class)
    SenmlCborMapperBuilderCustomizer senmlCborMapperBuilderCustomizer() {
      return cborMapperBuilder -> cborMapperBuilder.addModule(new SenmlCborJacksonModule());
    }
  }

  @Configuration
  @ConditionalOnClass({
    XmlMapperBuilderCustomizer.class,
    XmlMapper.class,
    SenmlXmlJacksonModule.class
  })
  static class XmlConfiguration {

    /**
     * Creates a {@link SenmlXmlMapperBuilderCustomizer} registering the {@link
     * SenmlXmlJacksonModule} in the XML mapper.
     *
     * @return a new {@code SenmlXmlMapperBuilderCustomizer} bean
     */
    @Bean
    @ConditionalOnMissingBean(SenmlXmlMapperBuilderCustomizer.class)
    SenmlXmlMapperBuilderCustomizer senmlXmlMapperBuilderCustomizer() {
      return xmlMapperBuilder -> xmlMapperBuilder.addModule(new SenmlXmlJacksonModule());
    }
  }
}
