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

import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Immutable, ordered set of extension fields of a {@link SenmlRecord}, i.e. fields not defined by
 * RFC 8428 itself, as allowed by <a href="https://www.rfc-editor.org/rfc/rfc8428#section-4.4">RFC
 * 8428, Section 4.4</a>.
 *
 * <p>Values are scalars only: {@code String}, finite number ({@code Double}), {@code Boolean} or
 * octets ({@code byte[]}). Names follow the SenML label syntax and must not collide with registered
 * labels (see {@link #isValidName(String)}). Names ending with {@code _} denote extensions that are
 * mandatory to understand; names starting with {@code b} denote base extensions.
 *
 * <p>Use {@link SenmlRecordBuilder#extension(String, String)} and its overloads, or {@link
 * #builder()}, to create instances.
 */
public sealed interface SenmlExtensions permits DefaultSenmlExtensions {

  /**
   * Returns an empty set of extensions.
   *
   * @return empty extensions
   */
  static SenmlExtensions empty() {
    return DefaultSenmlExtensions.EMPTY;
  }

  /**
   * Creates a new, empty builder.
   *
   * @return new builder
   */
  static SenmlExtensionsBuilder builder() {
    return new DefaultSenmlExtensionsBuilder();
  }

  /**
   * Returns whether given name can be used for an extension field: it must follow the SenML label
   * syntax ({@code [A-Za-z0-9][-_:.A-Za-z0-9]*}) and must not be a label registered by RFC 8428.
   *
   * @param name name to check
   * @return {@code true} if the name is a valid extension name
   */
  static boolean isValidName(String name) {
    return DefaultSenmlExtensions.NAME_PATTERN.matcher(name).matches()
        && !DefaultSenmlExtensions.REGISTERED_LABELS.contains(name);
  }

  /**
   * Creates a builder initialized with all extensions of this instance.
   *
   * @return new builder
   */
  SenmlExtensionsBuilder toBuilder();

  /**
   * Returns names of all extensions, in insertion order.
   *
   * @return unmodifiable set of names
   */
  Set<String> names();

  /**
   * Returns whether an extension with given name is present.
   *
   * @param name extension name
   * @return {@code true} if present
   */
  boolean contains(String name);

  /**
   * Returns number of extensions.
   *
   * @return number of extensions
   */
  int size();

  /**
   * Returns whether there are no extensions.
   *
   * @return {@code true} if there are no extensions
   */
  boolean isEmpty();

  /**
   * Returns value of given extension, as {@code String}, {@code Double}, {@code Boolean} or a copy
   * of the {@code byte[]}.
   *
   * @param name extension name
   * @return value or {@code null} if absent
   */
  @Nullable Object get(String name);

  /**
   * Returns value of given string extension.
   *
   * @param name extension name
   * @return value or {@code null} if absent
   * @throws IllegalStateException if the extension is present but not a string
   */
  @Nullable String getString(String name);

  /**
   * Returns value of given numeric extension.
   *
   * @param name extension name
   * @return value or {@code null} if absent
   * @throws IllegalStateException if the extension is present but not a number
   */
  @Nullable Double getNumber(String name);

  /**
   * Returns value of given boolean extension.
   *
   * @param name extension name
   * @return value or {@code null} if absent
   * @throws IllegalStateException if the extension is present but not a boolean
   */
  @Nullable Boolean getBoolean(String name);

  /**
   * Returns a copy of value of given octets extension.
   *
   * @param name extension name
   * @return value or {@code null} if absent
   * @throws IllegalStateException if the extension is present but not octets
   */
  byte @Nullable [] getBytes(String name);
}
