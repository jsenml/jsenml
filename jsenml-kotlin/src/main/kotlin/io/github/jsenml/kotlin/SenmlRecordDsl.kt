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
import io.github.jsenml.SenmlRecordBuilder

/**
 * Mutable DSL receiver used by [SenmlPackDsl.record] and [copy]. Exposes all configuration methods
 * of [SenmlRecordBuilder] except `build()`, which is invoked once the DSL block completes. Each
 * call in the block is applied, so methods do not need to be chained.
 *
 * Numeric fields and extensions accept any [Number], converted to [Double] as described in
 * [SenmlRecordBuilder].
 *
 * Example:
 * ```
 * val pack = buildSenmlPack {
 *     record {
 *         name("urn:dev:ow:10e2073a01080063")
 *         unit("Cel")
 *         value(23.1)
 *         extension("accuracy", 0.1)
 *     }
 * }
 * ```
 */
@SenmlDsl
public sealed interface SenmlRecordDsl {

  /**
   * Sets the Base Name (`bn`).
   *
   * @param baseName base name, or `null` to unset it
   */
  public fun baseName(baseName: String?)

  /**
   * Sets the Base Time (`bt`).
   *
   * @param baseTime finite base time, or `null` to unset it
   * @throws IllegalArgumentException if [baseTime] is not finite
   */
  public fun baseTime(baseTime: Number?)

  /**
   * Sets the Base Unit (`bu`).
   *
   * @param baseUnit base unit, or `null` to unset it
   */
  public fun baseUnit(baseUnit: String?)

  /**
   * Sets the Base Value (`bv`).
   *
   * @param baseValue finite base value, or `null` to unset it
   * @throws IllegalArgumentException if [baseValue] is not finite
   */
  public fun baseValue(baseValue: Number?)

  /**
   * Sets the Base Sum (`bs`).
   *
   * @param baseSum finite base sum, or `null` to unset it
   * @throws IllegalArgumentException if [baseSum] is not finite
   */
  public fun baseSum(baseSum: Number?)

  /**
   * Sets the Base Version (`bver`).
   *
   * @param baseVersion base version, or `null` to unset it
   */
  public fun baseVersion(baseVersion: Int?)

  /**
   * Sets the Name (`n`).
   *
   * @param name name, or `null` to unset it
   */
  public fun name(name: String?)

  /**
   * Sets the Unit (`u`).
   *
   * @param unit unit, or `null` to unset it
   */
  public fun unit(unit: String?)

  /**
   * Sets the numeric Value (`v`).
   *
   * @param value finite value, or `null` to unset it
   * @throws IllegalArgumentException if [value] is not finite
   */
  public fun value(value: Number?)

  /**
   * Sets the String Value (`vs`).
   *
   * @param stringValue string value, or `null` to unset it
   */
  public fun stringValue(stringValue: String?)

  /**
   * Sets the Boolean Value (`vb`).
   *
   * @param booleanValue boolean value, or `null` to unset it
   */
  public fun booleanValue(booleanValue: Boolean?)

  /**
   * Sets the Data Value (`vd`) from base64url text, as carried in JSON and XML. Padding is
   * optional.
   *
   * @param dataValue base64url-encoded data value, or `null` to unset it
   * @throws IllegalArgumentException if [dataValue] is not valid base64url
   */
  public fun dataValue(dataValue: String?)

  /**
   * Sets the Data Value (`vd`). The array is copied.
   *
   * @param dataValue data value, or `null` to unset it
   */
  public fun dataValue(dataValue: ByteArray?)

  /**
   * Sets the Sum (`s`).
   *
   * @param sum finite sum, or `null` to unset it
   * @throws IllegalArgumentException if [sum] is not finite
   */
  public fun sum(sum: Number?)

  /**
   * Sets the Time (`t`).
   *
   * @param time finite time, or `null` to unset it
   * @throws IllegalArgumentException if [time] is not finite
   */
  public fun time(time: Number?)

  /**
   * Sets the Update Time (`ut`).
   *
   * @param updateTime finite update time, or `null` to unset it
   * @throws IllegalArgumentException if [updateTime] is not finite
   */
  public fun updateTime(updateTime: Number?)

  /**
   * Sets a string extension field, replacing any previous extension of given name.
   *
   * @param name extension name, see [SenmlExtensions.isValidName]
   * @param value value
   * @throws IllegalArgumentException if [name] is not a valid extension name
   */
  public fun extension(name: String, value: String)

  /**
   * Sets a numeric extension field, replacing any previous extension of given name.
   *
   * @param name extension name, see [SenmlExtensions.isValidName]
   * @param value finite value
   * @throws IllegalArgumentException if [name] is not a valid extension name or [value] is not
   *   finite
   */
  public fun extension(name: String, value: Number)

  /**
   * Sets a boolean extension field, replacing any previous extension of given name.
   *
   * @param name extension name, see [SenmlExtensions.isValidName]
   * @param value value
   * @throws IllegalArgumentException if [name] is not a valid extension name
   */
  public fun extension(name: String, value: Boolean)

  /**
   * Sets an octets extension field, replacing any previous extension of given name. The array is
   * copied.
   *
   * @param name extension name, see [SenmlExtensions.isValidName]
   * @param value value
   * @throws IllegalArgumentException if [name] is not a valid extension name
   */
  public fun extension(name: String, value: ByteArray)

  /**
   * Removes extension field of given name, if present.
   *
   * @param name extension name
   */
  public fun removeExtension(name: String)

  /**
   * Replaces all extension fields with given ones.
   *
   * @param extensions new extensions
   */
  public fun extensions(extensions: SenmlExtensions)

  /**
   * Replaces all extension fields with ones configured by a [SenmlExtensionsDsl] block.
   *
   * Example:
   * ```
   * record {
   *     name("temp")
   *     extensions {
   *         put("accuracy", 0.1)
   *         put("calibrated", true)
   *     }
   * }
   * ```
   *
   * @param block configures the extensions
   */
  public fun extensions(block: SenmlExtensionsDsl.() -> Unit)
}
