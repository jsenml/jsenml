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

package io.github.jsenml.xml.jackson;

import io.github.jsenml.xml.SenmlXmlCodec;
import io.github.jsenml.xml.spi.SenmlXmlCodecProvider;

/** {@link SenmlXmlCodecProvider} registering {@link JacksonSenmlXmlCodec}. */
public final class JacksonSenmlXmlCodecProvider implements SenmlXmlCodecProvider {

  /** Name of this provider. */
  public static final String NAME = "jackson";

  /** Priority of this provider. */
  public static final int PRIORITY = 5000;

  /**
   * Returns {@value #NAME}, the name to select this provider with.
   *
   * @return provider name
   */
  @Override
  public String name() {
    return NAME;
  }

  /**
   * Returns {@value #PRIORITY}.
   *
   * @return provider priority
   */
  @Override
  public int getPriority() {
    return PRIORITY;
  }

  /**
   * Creates a new {@link JacksonSenmlXmlCodec} with default Jackson configuration.
   *
   * @return new codec
   */
  @Override
  public SenmlXmlCodec create() {
    return new JacksonSenmlXmlCodec();
  }
}
