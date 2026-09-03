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

import org.jspecify.annotations.Nullable;

/**
 * Mutable builder of {@link SenmlRecord} instances. Not thread-safe.
 *
 * <p>Numeric fields and extensions accept any {@link Number}, converted to {@code double}: {@link
 * Float} through its decimal representation (e.g. {@code 23.1f} becomes {@code 23.1}), other types
 * with {@link Number#doubleValue()}, so e.g. {@code long} values beyond &plusmn;2<sup>53</sup> or
 * {@link java.math.BigDecimal} values may lose precision.
 */
public sealed interface SenmlRecordBuilder permits DefaultSenmlRecordBuilder {

  /**
   * Sets the Base Name ({@code bn}).
   *
   * @param baseName base name or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder baseName(@Nullable String baseName);

  /**
   * Sets the Base Time ({@code bt}).
   *
   * @param baseTime finite base time or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder baseTime(@Nullable Number baseTime);

  /**
   * Sets the Base Unit ({@code bu}).
   *
   * @param baseUnit base unit or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder baseUnit(@Nullable String baseUnit);

  /**
   * Sets the Base Value ({@code bv}).
   *
   * @param baseValue finite base value or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder baseValue(@Nullable Number baseValue);

  /**
   * Sets the Base Sum ({@code bs}).
   *
   * @param baseSum finite base sum or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder baseSum(@Nullable Number baseSum);

  /**
   * Sets the Base Version ({@code bver}).
   *
   * @param baseVersion base version or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder baseVersion(@Nullable Integer baseVersion);

  /**
   * Sets the Name ({@code n}).
   *
   * @param name name or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder name(@Nullable String name);

  /**
   * Sets the Unit ({@code u}).
   *
   * @param unit unit or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder unit(@Nullable String unit);

  /**
   * Sets the numeric Value ({@code v}).
   *
   * @param value finite value or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder value(@Nullable Number value);

  /**
   * Sets the String Value ({@code vs}).
   *
   * @param stringValue string value or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder stringValue(@Nullable String stringValue);

  /**
   * Sets the Boolean Value ({@code vb}).
   *
   * @param booleanValue boolean value or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder booleanValue(@Nullable Boolean booleanValue);

  /**
   * Sets the Data Value ({@code vd}) from base64url text, as carried in JSON and XML. Padding is
   * optional.
   *
   * @param dataValue base64url-encoded data value or {@code null} to clear
   * @return this builder
   * @throws IllegalArgumentException if the text is not valid base64url
   */
  SenmlRecordBuilder dataValue(@Nullable String dataValue);

  /**
   * Sets the Data Value ({@code vd}). The array is copied.
   *
   * @param dataValue data value or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder dataValue(byte @Nullable [] dataValue);

  /**
   * Sets the Sum ({@code s}).
   *
   * @param sum finite sum or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder sum(@Nullable Number sum);

  /**
   * Sets the Time ({@code t}).
   *
   * @param time finite time or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder time(@Nullable Number time);

  /**
   * Sets the Update Time ({@code ut}).
   *
   * @param updateTime finite update time or {@code null} to clear
   * @return this builder
   */
  SenmlRecordBuilder updateTime(@Nullable Number updateTime);

  /**
   * Sets a string extension field, replacing any previous extension of given name.
   *
   * @param name extension name, see {@link SenmlExtensions#isValidName(String)}
   * @param value value
   * @return this builder
   * @throws IllegalArgumentException if the name is not a valid extension name
   */
  SenmlRecordBuilder extension(String name, String value);

  /**
   * Sets a numeric extension field, replacing any previous extension of given name.
   *
   * @param name extension name, see {@link SenmlExtensions#isValidName(String)}
   * @param value finite value
   * @return this builder
   * @throws IllegalArgumentException if the name is not a valid extension name or the value is not
   *     finite
   */
  SenmlRecordBuilder extension(String name, Number value);

  /**
   * Sets a boolean extension field, replacing any previous extension of given name.
   *
   * @param name extension name, see {@link SenmlExtensions#isValidName(String)}
   * @param value value
   * @return this builder
   * @throws IllegalArgumentException if the name is not a valid extension name
   */
  SenmlRecordBuilder extension(String name, boolean value);

  /**
   * Sets an octets extension field, replacing any previous extension of given name. The array is
   * copied.
   *
   * @param name extension name, see {@link SenmlExtensions#isValidName(String)}
   * @param value value
   * @return this builder
   * @throws IllegalArgumentException if the name is not a valid extension name
   */
  SenmlRecordBuilder extension(String name, byte[] value);

  /**
   * Removes extension field of given name, if present.
   *
   * @param name extension name
   * @return this builder
   */
  SenmlRecordBuilder removeExtension(String name);

  /**
   * Replaces all extension fields with given ones.
   *
   * @param extensions new extensions
   * @return this builder
   */
  SenmlRecordBuilder extensions(SenmlExtensions extensions);

  /**
   * Creates an immutable {@link SenmlRecord} from the current state of this builder.
   *
   * @return new record
   */
  SenmlRecord build();
}
