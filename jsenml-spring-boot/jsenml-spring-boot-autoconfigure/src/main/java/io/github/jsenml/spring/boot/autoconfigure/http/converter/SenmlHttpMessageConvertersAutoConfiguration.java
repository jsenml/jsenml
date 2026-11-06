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

package io.github.jsenml.spring.boot.autoconfigure.http.converter;

import io.github.jsenml.cbor.SenmlCborCodec;
import io.github.jsenml.cbor.jackson.SenmlCborJacksonModule;
import io.github.jsenml.spring.boot.autoconfigure.SenmlProperties;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.boot.http.converter.autoconfigure.ServerHttpMessageConvertersCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.cbor.JacksonCborHttpMessageConverter;
import tools.jackson.dataformat.cbor.CBORMapper;

/**
 * Spring Boot autoconfiguration of HTTP message converters (Spring MVC, {@code RestClient}) for
 * SenML media types that Spring Boot's converters do not support. Active when Spring Boot's HTTP
 * message converter support is on the classpath, unless {@code jsenml.enabled} is {@code false}.
 *
 * <p>{@code application/senml+json} needs no converter of its own, as Spring Boot's JSON converter
 * supports {@code application/*+json}. An {@code application/senml+cbor} converter using Spring
 * Boot's {@code CBORMapper} is added when {@code jsenml-cbor-jackson} is on the classpath, unless
 * {@code jsenml.converter.cbor.enabled} is {@code false}. Converters are added last, so they
 * neither change other converters nor the default media type of responses.
 */
@AutoConfiguration(
    afterName = "org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration")
@EnableConfigurationProperties(SenmlProperties.class)
@ConditionalOnBooleanProperty(name = SenmlProperties.ENABLED_PROPERTY, matchIfMissing = true)
@ConditionalOnClass({
  ClientHttpMessageConvertersCustomizer.class,
  ServerHttpMessageConvertersCustomizer.class
})
public final class SenmlHttpMessageConvertersAutoConfiguration {

  /** Creates a new instance of this autoconfiguration. */
  public SenmlHttpMessageConvertersAutoConfiguration() {}

  @Configuration
  @ConditionalOnBooleanProperty(
      name = SenmlProperties.CONVERTER_CBOR_ENABLED_PROPERTY,
      matchIfMissing = true)
  @ConditionalOnClass({
    JacksonCborHttpMessageConverter.class,
    CBORMapper.class,
    SenmlCborJacksonModule.class
  })
  @ConditionalOnBean(CBORMapper.class)
  static class CborConvertersConfiguration {

    @Bean
    @ConditionalOnMissingBean(SenmlCborServerHttpMessageConvertersCustomizer.class)
    SenmlCborServerHttpMessageConvertersCustomizer senmlCborServerHttpMessageConvertersCustomizer(
        CBORMapper cborMapper) {
      return builder ->
          builder.configureMessageConvertersList(
              converters -> converters.add(createConverter(cborMapper)));
    }

    @Bean
    @ConditionalOnMissingBean(SenmlCborClientHttpMessageConvertersCustomizer.class)
    SenmlCborClientHttpMessageConvertersCustomizer senmlCborClientHttpMessageConvertersCustomizer(
        CBORMapper cborMapper) {
      return builder ->
          builder.configureMessageConvertersList(
              converters -> converters.add(createConverter(cborMapper)));
    }

    private static JacksonCborHttpMessageConverter createConverter(CBORMapper cborMapper) {
      JacksonCborHttpMessageConverter converter = new JacksonCborHttpMessageConverter(cborMapper);
      converter.setSupportedMediaTypes(
          List.of(MediaType.parseMediaType(SenmlCborCodec.MEDIA_TYPE)));
      return converter;
    }
  }
}
