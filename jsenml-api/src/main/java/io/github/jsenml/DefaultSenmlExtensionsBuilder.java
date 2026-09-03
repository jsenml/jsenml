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

package io.github.jsenml;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Default implementation of {@link SenmlExtensionsBuilder}. */
final class DefaultSenmlExtensionsBuilder implements SenmlExtensionsBuilder, Serializable {

  @Serial //
  private static final long serialVersionUID = 1L;

  private final Map<String, Object> values;

  DefaultSenmlExtensionsBuilder() {
    this.values = new LinkedHashMap<>();
  }

  DefaultSenmlExtensionsBuilder(Map<String, Object> values) {
    this.values = new LinkedHashMap<>(values);
  }

  @Override
  public SenmlExtensionsBuilder put(String name, String value) {
    return set(name, Objects.requireNonNull(value, "value"));
  }

  @Override
  public SenmlExtensionsBuilder put(String name, Number value) {
    double number = DefaultSenmlRecordBuilder.toDouble(Objects.requireNonNull(value, "value"));
    if (!Double.isFinite(number)) {
      throw new IllegalArgumentException("Extension '" + name + "' must be finite, got " + value);
    }
    return set(name, number);
  }

  @Override
  public SenmlExtensionsBuilder put(String name, boolean value) {
    return set(name, value);
  }

  @Override
  public SenmlExtensionsBuilder put(String name, byte[] value) {
    return set(name, Objects.requireNonNull(value, "value").clone());
  }

  @Override
  public SenmlExtensionsBuilder remove(String name) {
    values.remove(name);
    return this;
  }

  @Override
  public SenmlExtensions build() {
    return values.isEmpty()
        ? SenmlExtensions.empty()
        : new DefaultSenmlExtensions(Collections.unmodifiableMap(new LinkedHashMap<>(values)));
  }

  @Override
  public String toString() {
    return ((DefaultSenmlExtensions) build())
        .toString(SenmlExtensionsBuilder.class.getSimpleName());
  }

  private SenmlExtensionsBuilder set(String name, Object value) {
    if (!SenmlExtensions.isValidName(name)) {
      throw new IllegalArgumentException("'" + name + "' is not a valid SenML extension name");
    }
    values.put(name, value);
    return this;
  }
}
