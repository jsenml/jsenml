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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/** Default implementation of {@link SenmlExtensions}. */
final class DefaultSenmlExtensions implements SenmlExtensions, Serializable {

  @Serial //
  private static final long serialVersionUID = 1L;

  static final SenmlExtensions EMPTY = new DefaultSenmlExtensions(Collections.emptyMap());

  /** See the CDDL of RFC 8428, Section 11. */
  static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9][-_:.A-Za-z0-9]*");

  static final Set<String> REGISTERED_LABELS =
      Set.of(
          SenmlTextFormat.BASE_NAME,
          SenmlTextFormat.BASE_TIME,
          SenmlTextFormat.BASE_UNIT,
          SenmlTextFormat.BASE_VALUE,
          SenmlTextFormat.BASE_SUM,
          SenmlTextFormat.BASE_VERSION,
          SenmlTextFormat.NAME,
          SenmlTextFormat.UNIT,
          SenmlTextFormat.VALUE,
          SenmlTextFormat.STRING_VALUE,
          SenmlTextFormat.BOOLEAN_VALUE,
          SenmlTextFormat.DATA_VALUE,
          SenmlTextFormat.SUM,
          SenmlTextFormat.TIME,
          SenmlTextFormat.UPDATE_TIME);

  private final Map<String, Object> values;

  DefaultSenmlExtensions(Map<String, Object> values) {
    this.values = values;
  }

  @Override
  public SenmlExtensionsBuilder toBuilder() {
    return new DefaultSenmlExtensionsBuilder(values);
  }

  @Override
  public Set<String> names() {
    return Collections.unmodifiableSet(values.keySet());
  }

  @Override
  public boolean contains(String name) {
    return values.containsKey(name);
  }

  @Override
  public int size() {
    return values.size();
  }

  @Override
  public boolean isEmpty() {
    return values.isEmpty();
  }

  @Override
  public @Nullable Object get(String name) {
    Object value = values.get(name);
    return value instanceof byte[] bytes ? bytes.clone() : value;
  }

  @Override
  public @Nullable String getString(String name) {
    return typed(name, String.class);
  }

  @Override
  public @Nullable Double getNumber(String name) {
    return typed(name, Double.class);
  }

  @Override
  public @Nullable Boolean getBoolean(String name) {
    return typed(name, Boolean.class);
  }

  @Override
  public byte @Nullable [] getBytes(String name) {
    byte[] bytes = typed(name, byte[].class);
    return bytes != null ? bytes.clone() : null;
  }

  /**
   * Returns extensions whose names match given predicate, in the same order.
   *
   * @param filter predicate on extension names
   * @return matching extensions
   */
  static SenmlExtensions filter(SenmlExtensions extensions, Predicate<String> filter) {
    Map<String, Object> matching = new LinkedHashMap<>();
    ((DefaultSenmlExtensions) extensions)
        .values.forEach(
            (name, value) -> {
              if (filter.test(name)) {
                matching.put(name, value);
              }
            });
    return matching.isEmpty()
        ? EMPTY
        : new DefaultSenmlExtensions(Collections.unmodifiableMap(matching));
  }

  private <T> @Nullable T typed(String name, Class<T> type) {
    Object value = values.get(name);
    if (value == null) {
      return null;
    }
    if (!type.isInstance(value)) {
      throw new IllegalStateException(
          "Extension '"
              + name
              + "' is "
              + value.getClass().getSimpleName()
              + ", not "
              + type.getSimpleName());
    }
    return type.cast(value);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SenmlExtensions other) || values.size() != other.size()) {
      return false;
    }
    for (Map.Entry<String, Object> entry : values.entrySet()) {
      Object otherValue = other.get(entry.getKey());
      if (!Objects.deepEquals(entry.getValue(), otherValue)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    for (Map.Entry<String, Object> entry : values.entrySet()) {
      Object value = entry.getValue();
      int valueHash = value instanceof byte[] bytes ? Arrays.hashCode(bytes) : value.hashCode();
      hash += entry.getKey().hashCode() ^ valueHash;
    }
    return hash;
  }

  @Override
  public String toString() {
    return toString(SenmlExtensions.class.getSimpleName());
  }

  String toString(String typeName) {
    StringJoiner joiner = new StringJoiner(", ", typeName + "[", "]");
    values.forEach((name, value) -> joiner.add(name + "=" + format(value)));
    return joiner.toString();
  }

  static String format(Object value) {
    return value instanceof byte[] bytes
        ? SenmlTextFormat.formatBytes(bytes)
        : String.valueOf(value);
  }
}
