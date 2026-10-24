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

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties of JSenML Spring Boot integration, bound under {@value #PREFIX}. */
@ConfigurationProperties(prefix = SenmlProperties.PREFIX)
public final class SenmlProperties {

  /** Prefix of all JSenML properties. */
  public static final String PREFIX = "jsenml";

  /** Name of the property enabling JSenML autoconfiguration. */
  public static final String ENABLED_PROPERTY = PREFIX + ".enabled";

  /** Name of the property enabling the {@code application/senml+cbor} HTTP message converter. */
  public static final String CONVERTER_CBOR_ENABLED_PROPERTY = PREFIX + ".converter.cbor.enabled";

  /** Whether to register SenML support (JSON, CBOR, XML, OpenAPI schemas) in Spring Boot. */
  private boolean enabled = true;

  /** HTTP message converters. */
  private final Converter converter = new Converter();

  /** Creates properties with default values. */
  public SenmlProperties() {}

  /**
   * Returns whether JSenML autoconfiguration is enabled.
   *
   * @return {@code true} if enabled, which is the default
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets whether JSenML autoconfiguration is enabled.
   *
   * @param enabled {@code false} to disable it
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns HTTP message converter properties.
   *
   * @return converter properties
   */
  public Converter getConverter() {
    return converter;
  }

  /** HTTP message converter properties, bound under {@code jsenml.converter}. */
  public static final class Converter {

    /** {@code application/senml+cbor} converter. */
    private final Cbor cbor = new Cbor();

    /** Creates properties with default values. */
    public Converter() {}

    /**
     * Returns {@code application/senml+cbor} converter properties.
     *
     * @return CBOR converter properties
     */
    public Cbor getCbor() {
      return cbor;
    }

    /**
     * {@code application/senml+cbor} converter properties, bound under {@code
     * jsenml.converter.cbor}.
     */
    public static final class Cbor {

      /**
       * Whether to add an {@code application/senml+cbor} HTTP message converter (Spring MVC, {@code
       * RestClient}) when {@code jsenml-cbor-jackson} is on the classpath.
       */
      private boolean enabled = true;

      /** Creates properties with default values. */
      public Cbor() {}

      /**
       * Returns whether the {@code application/senml+cbor} converter is enabled.
       *
       * @return {@code true} if enabled, which is the default
       */
      public boolean isEnabled() {
        return enabled;
      }

      /**
       * Sets whether the {@code application/senml+cbor} converter is enabled.
       *
       * @param enabled {@code false} to disable it
       */
      public void setEnabled(boolean enabled) {
        this.enabled = enabled;
      }
    }
  }
}
