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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.cbor.SenmlCbor;
import io.github.jsenml.cbor.SenmlCborCodec;
import io.github.jsenml.cbor.jackson.SenmlCborJacksonModule;
import io.github.jsenml.json.SenmlJson;
import io.github.jsenml.json.SenmlJsonCodec;
import io.github.jsenml.spring.boot.autoconfigure.SenmlPackController;
import io.github.jsenml.spring.boot.autoconfigure.SenmlProperties;
import io.github.jsenml.spring.boot.autoconfigure.gson.SenmlGsonAutoConfiguration;
import io.github.jsenml.spring.boot.autoconfigure.jackson.SenmlJacksonAutoConfiguration;
import io.github.jsenml.xml.SenmlXml;
import io.github.jsenml.xml.SenmlXmlCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.gson.autoconfigure.GsonAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.cbor.JacksonCborHttpMessageConverter;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

class SenmlHttpMessageConvertersAutoConfigurationTest {

  private static final SenmlPack PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("Cel")
              .value(23.1)
              .build());

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  HttpMessageConvertersAutoConfiguration.class,
                  WebMvcAutoConfiguration.class,
                  SenmlHttpMessageConvertersAutoConfiguration.class))
          .withUserConfiguration(SenmlPackController.class);

  private final WebApplicationContextRunner jacksonContextRunner =
      contextRunner.withConfiguration(
          AutoConfigurations.of(
              JacksonAutoConfiguration.class, SenmlJacksonAutoConfiguration.class));

  static Stream<Arguments> jacksonRepresentations() {
    return Stream.of(
        Arguments.of(SenmlJsonCodec.MEDIA_TYPE, SenmlJson.toJsonBytes(PACK)),
        Arguments.of(SenmlCborCodec.MEDIA_TYPE, SenmlCbor.toCbor(PACK)),
        Arguments.of(SenmlXmlCodec.MEDIA_TYPE, SenmlXml.toXmlBytes(PACK)));
  }

  @ParameterizedTest
  @MethodSource("jacksonRepresentations")
  void givenJackson_whenPostingSenmlBody_thenReadsAndWritesSenml(String mediaType, byte[] body) {
    jacksonContextRunner.run(context -> assertEcho(context, mediaType, body));
  }

  @Test
  void givenGson_whenPostingSenmlJsonBody_thenReadsAndWritesSenmlJson() {
    contextRunner
        .withConfiguration(
            AutoConfigurations.of(GsonAutoConfiguration.class, SenmlGsonAutoConfiguration.class))
        .withPropertyValues("spring.http.converters.preferred-json-mapper=gson")
        .run(
            context -> assertEcho(context, SenmlJsonCodec.MEDIA_TYPE, SenmlJson.toJsonBytes(PACK)));
  }

  @Test
  void givenJackson_whenBuildingServerConverters_thenAddsSenmlCborConverterLast() {
    jacksonContextRunner.run(
        context -> {
          HttpMessageConverters.ServerBuilder builder =
              HttpMessageConverters.forServer().registerDefaults();
          context.getBean(SenmlCborServerHttpMessageConvertersCustomizer.class).customize(builder);

          assertSenmlCborConverterLast(builder.build());
        });
  }

  @Test
  void givenJackson_whenBuildingClientConverters_thenAddsSenmlCborConverterLast() {
    jacksonContextRunner.run(
        context -> {
          HttpMessageConverters.ClientBuilder builder =
              HttpMessageConverters.forClient().registerDefaults();
          context.getBean(SenmlCborClientHttpMessageConvertersCustomizer.class).customize(builder);

          assertSenmlCborConverterLast(builder.build());
        });
  }

  @Test
  void givenUserDefinedServerCustomizer_whenContextStarts_thenBacksOffOnlyServerCustomizer() {
    jacksonContextRunner
        .withUserConfiguration(CustomServerCustomizerConfiguration.class)
        .run(
            context -> {
              assertThat(context)
                  .hasSingleBean(SenmlCborServerHttpMessageConvertersCustomizer.class);
              assertThat(context.getBean(SenmlCborServerHttpMessageConvertersCustomizer.class))
                  .isSameAs(context.getBean("senmlCborServerHttpMessageConvertersCustomizerExt"));
              assertThat(context)
                  .hasSingleBean(SenmlCborClientHttpMessageConvertersCustomizer.class);
            });
  }

  @Test
  void givenUserDefinedClientCustomizer_whenContextStarts_thenBacksOffOnlyClientCustomizer() {
    jacksonContextRunner
        .withUserConfiguration(CustomClientCustomizerConfiguration.class)
        .run(
            context -> {
              assertThat(context)
                  .hasSingleBean(SenmlCborClientHttpMessageConvertersCustomizer.class);
              assertThat(context.getBean(SenmlCborClientHttpMessageConvertersCustomizer.class))
                  .isSameAs(context.getBean("senmlCborClientHttpMessageConvertersCustomizerExt"));
              assertThat(context)
                  .hasSingleBean(SenmlCborServerHttpMessageConvertersCustomizer.class);
            });
  }

  @Test
  void givenJsenmlCborJacksonNotOnClasspath_whenPostingSenmlCborBody_thenRespondsUnsupported() {
    jacksonContextRunner
        .withClassLoader(new FilteredClassLoader(SenmlCborJacksonModule.class))
        .run(
            context -> {
              assertNoCustomizers(context);
              client(context)
                  .post()
                  .uri(SenmlPackController.PATH)
                  .contentType(MediaType.parseMediaType(SenmlCborCodec.MEDIA_TYPE))
                  .body(SenmlCbor.toCbor(PACK))
                  .exchange()
                  .expectStatus()
                  .isEqualTo(415);
            });
  }

  @Test
  void givenCborConverterDisabled_whenPostingSenmlCborBody_thenRespondsUnsupported() {
    jacksonContextRunner
        .withPropertyValues(SenmlProperties.CONVERTER_CBOR_ENABLED_PROPERTY + "=false")
        .run(
            context -> {
              assertNoCustomizers(context);
              assertEcho(context, SenmlJsonCodec.MEDIA_TYPE, SenmlJson.toJsonBytes(PACK));
              client(context)
                  .post()
                  .uri(SenmlPackController.PATH)
                  .contentType(MediaType.parseMediaType(SenmlCborCodec.MEDIA_TYPE))
                  .body(SenmlCbor.toCbor(PACK))
                  .exchange()
                  .expectStatus()
                  .isEqualTo(415);
            });
  }

  @Test
  void givenJsenmlDisabled_whenContextStarts_thenDoesNotRegisterCustomizers() {
    jacksonContextRunner
        .withPropertyValues(SenmlProperties.ENABLED_PROPERTY + "=false")
        .run(SenmlHttpMessageConvertersAutoConfigurationTest::assertNoCustomizers);
  }

  private static void assertEcho(WebApplicationContext context, String mediaType, byte[] body) {
    MediaType type = MediaType.parseMediaType(mediaType);

    byte[] response =
        client(context)
            .post()
            .uri(SenmlPackController.PATH)
            .contentType(type)
            .accept(type)
            .body(body)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(type)
            .expectBody(byte[].class)
            .returnResult()
            .getResponseBody();

    assertThat(response).isEqualTo(body);
  }

  private static void assertSenmlCborConverterLast(HttpMessageConverters converters) {
    MediaType senmlCbor = MediaType.parseMediaType(SenmlCborCodec.MEDIA_TYPE);
    List<HttpMessageConverter<?>> list = new ArrayList<>();
    converters.forEach(list::add);

    assertThat(list)
        .filteredOn(converter -> converter.getSupportedMediaTypes().contains(senmlCbor))
        .singleElement()
        .isInstanceOf(JacksonCborHttpMessageConverter.class)
        .isSameAs(list.get(list.size() - 1));
  }

  private static void assertNoCustomizers(AssertableWebApplicationContext context) {
    assertThat(context).doesNotHaveBean(SenmlCborServerHttpMessageConvertersCustomizer.class);
    assertThat(context).doesNotHaveBean(SenmlCborClientHttpMessageConvertersCustomizer.class);
  }

  private static RestTestClient client(WebApplicationContext context) {
    return RestTestClient.bindToApplicationContext(context).build();
  }

  @Configuration
  static class CustomServerCustomizerConfiguration {

    @Bean
    SenmlCborServerHttpMessageConvertersCustomizer
        senmlCborServerHttpMessageConvertersCustomizerExt() {
      return new SenmlCborServerHttpMessageConvertersCustomizerExt();
    }
  }

  @Configuration
  static class CustomClientCustomizerConfiguration {

    @Bean
    SenmlCborClientHttpMessageConvertersCustomizer
        senmlCborClientHttpMessageConvertersCustomizerExt() {
      return new SenmlCborClientHttpMessageConvertersCustomizerExt();
    }
  }
}
