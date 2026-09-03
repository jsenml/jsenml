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

/**
 * Mutable builder of {@link SenmlExtensions} instances. Not thread-safe.
 *
 * <p>Numeric values accept any {@link Number}, converted to {@code double} as described in {@link
 * SenmlRecordBuilder}.
 */
public sealed interface SenmlExtensionsBuilder permits DefaultSenmlExtensionsBuilder {

  /**
   * Sets a string extension, replacing any previous value of given name.
   *
   * @param name extension name, see {@link SenmlExtensions#isValidName(String)}
   * @param value value
   * @return this builder
   * @throws IllegalArgumentException if the name is not a valid extension name
   */
  SenmlExtensionsBuilder put(String name, String value);

  /**
   * Sets a numeric extension, replacing any previous value of given name.
   *
   * @param name extension name, see {@link SenmlExtensions#isValidName(String)}
   * @param value finite value
   * @return this builder
   * @throws IllegalArgumentException if the name is not a valid extension name or the value is not
   *     finite
   */
  SenmlExtensionsBuilder put(String name, Number value);

  /**
   * Sets a boolean extension, replacing any previous value of given name.
   *
   * @param name extension name, see {@link SenmlExtensions#isValidName(String)}
   * @param value value
   * @return this builder
   * @throws IllegalArgumentException if the name is not a valid extension name
   */
  SenmlExtensionsBuilder put(String name, boolean value);

  /**
   * Sets an octets extension, replacing any previous value of given name. The array is copied.
   *
   * @param name extension name, see {@link SenmlExtensions#isValidName(String)}
   * @param value value
   * @return this builder
   * @throws IllegalArgumentException if the name is not a valid extension name
   */
  SenmlExtensionsBuilder put(String name, byte[] value);

  /**
   * Removes extension of given name, if present.
   *
   * @param name extension name
   * @return this builder
   */
  SenmlExtensionsBuilder remove(String name);

  /**
   * Creates an immutable {@link SenmlExtensions} from the current state of this builder.
   *
   * @return new extensions
   */
  SenmlExtensions build();
}
