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
 * Immutable SenML Record, as defined in <a href="https://www.rfc-editor.org/rfc/rfc8428#section-4">
 * RFC 8428, Section 4</a>.
 *
 * <p>A record holds base fields (applying to this and all following records within a {@link
 * SenmlPack}) and regular fields (applying to this record only). Every field is optional. Numeric
 * fields must be finite.
 *
 * <p>Records are not validated against cross-field SenML rules (e.g. a single value field per
 * record) on construction, so that any wire-level pack can be represented as-is. Use {@link
 * SenmlPack#resolve()} to obtain validated, resolved records.
 *
 * <p>Use {@link #builder()} to create instances and {@link #toBuilder()} to derive modified copies.
 */
public sealed interface SenmlRecord permits DefaultSenmlRecord {

  /**
   * Creates a new, empty builder.
   *
   * @return new builder
   */
  static SenmlRecordBuilder builder() {
    return new DefaultSenmlRecordBuilder();
  }

  /**
   * Creates a builder initialized with all fields of this record.
   *
   * @return new builder
   */
  SenmlRecordBuilder toBuilder();

  /**
   * Returns the Base Name ({@code bn}).
   *
   * @return base name or {@code null} if absent
   */
  @Nullable String getBaseName();

  /**
   * Returns the Base Time ({@code bt}).
   *
   * @return base time or {@code null} if absent
   */
  @Nullable Double getBaseTime();

  /**
   * Returns the Base Unit ({@code bu}).
   *
   * @return base unit or {@code null} if absent
   */
  @Nullable String getBaseUnit();

  /**
   * Returns the Base Value ({@code bv}).
   *
   * @return base value or {@code null} if absent
   */
  @Nullable Double getBaseValue();

  /**
   * Returns the Base Sum ({@code bs}).
   *
   * @return base sum or {@code null} if absent
   */
  @Nullable Double getBaseSum();

  /**
   * Returns the Base Version ({@code bver}).
   *
   * @return base version or {@code null} if absent
   */
  @Nullable Integer getBaseVersion();

  /**
   * Returns the Name ({@code n}).
   *
   * @return name or {@code null} if absent
   */
  @Nullable String getName();

  /**
   * Returns the Unit ({@code u}).
   *
   * @return unit or {@code null} if absent
   */
  @Nullable String getUnit();

  /**
   * Returns the numeric Value ({@code v}).
   *
   * @return value or {@code null} if absent
   */
  @Nullable Double getValue();

  /**
   * Returns the String Value ({@code vs}).
   *
   * @return string value or {@code null} if absent
   */
  @Nullable String getStringValue();

  /**
   * Returns the Boolean Value ({@code vb}).
   *
   * @return boolean value or {@code null} if absent
   */
  @Nullable Boolean getBooleanValue();

  /**
   * Returns the Data Value ({@code vd}): octets, carried as base64url text in JSON and XML and as a
   * byte string in CBOR.
   *
   * @return copy of the data value or {@code null} if absent
   */
  byte @Nullable [] getDataValue();

  /**
   * Returns the Sum ({@code s}).
   *
   * @return sum or {@code null} if absent
   */
  @Nullable Double getSum();

  /**
   * Returns the Time ({@code t}).
   *
   * @return time or {@code null} if absent
   */
  @Nullable Double getTime();

  /**
   * Returns the Update Time ({@code ut}).
   *
   * @return update time or {@code null} if absent
   */
  @Nullable Double getUpdateTime();

  /**
   * Returns whether this record carries any base field.
   *
   * @return {@code true} if any of {@code bn}, {@code bt}, {@code bu}, {@code bv}, {@code bs} or
   *     {@code bver} is present
   */
  boolean hasBaseFields();

  /**
   * Returns extension fields of this record, i.e. fields not defined by RFC 8428.
   *
   * @return extensions, empty if there are none
   */
  SenmlExtensions getExtensions();
}
