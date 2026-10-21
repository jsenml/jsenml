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

package io.github.jsenml.kotlin

import io.github.jsenml.SenmlExtensions
import io.github.jsenml.SenmlExtensionsBuilder

/**
 * Mutable DSL receiver used by [SenmlRecordDsl.extensions] and [copy]. Exposes all configuration
 * methods of [SenmlExtensionsBuilder] except `build()`, which is invoked once the DSL block
 * completes.
 *
 * Example:
 * ```
 * val updated = extensions.copy {
 *     put("accuracy", 0.1)
 *     remove("calibrated")
 * }
 * ```
 */
@SenmlDsl
public sealed interface SenmlExtensionsDsl {

  /**
   * Sets a string extension, replacing any previous value of given name.
   *
   * @param name extension name, see [SenmlExtensions.isValidName]
   * @param value value
   * @throws IllegalArgumentException if [name] is not a valid extension name
   */
  public fun put(name: String, value: String)

  /**
   * Sets a numeric extension, replacing any previous value of given name.
   *
   * @param name extension name, see [SenmlExtensions.isValidName]
   * @param value finite value
   * @throws IllegalArgumentException if [name] is not a valid extension name or [value] is not
   *   finite
   */
  public fun put(name: String, value: Number)

  /**
   * Sets a boolean extension, replacing any previous value of given name.
   *
   * @param name extension name, see [SenmlExtensions.isValidName]
   * @param value value
   * @throws IllegalArgumentException if [name] is not a valid extension name
   */
  public fun put(name: String, value: Boolean)

  /**
   * Sets an octets extension, replacing any previous value of given name. The array is copied.
   *
   * @param name extension name, see [SenmlExtensions.isValidName]
   * @param value value
   * @throws IllegalArgumentException if [name] is not a valid extension name
   */
  public fun put(name: String, value: ByteArray)

  /**
   * Removes extension of given name, if present.
   *
   * @param name extension name
   */
  public fun remove(name: String)
}
