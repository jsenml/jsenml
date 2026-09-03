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
import java.util.Base64;
import org.jspecify.annotations.Nullable;

/** Default implementation of {@link SenmlRecordBuilder}. */
final class DefaultSenmlRecordBuilder implements SenmlRecordBuilder, Serializable {

  @Serial //
  private static final long serialVersionUID = 1L;

  @Nullable String baseName;
  @Nullable Double baseTime;
  @Nullable String baseUnit;
  @Nullable Double baseValue;
  @Nullable Double baseSum;
  @Nullable Integer baseVersion;
  @Nullable String name;
  @Nullable String unit;
  @Nullable Double value;
  @Nullable String stringValue;
  @Nullable Boolean booleanValue;
  byte @Nullable [] dataValue;
  @Nullable Double sum;
  @Nullable Double time;
  @Nullable Double updateTime;
  SenmlExtensionsBuilder extensions = SenmlExtensions.builder();

  DefaultSenmlRecordBuilder() {}

  DefaultSenmlRecordBuilder(SenmlRecord record) {
    this.baseName = record.getBaseName();
    this.baseTime = record.getBaseTime();
    this.baseUnit = record.getBaseUnit();
    this.baseValue = record.getBaseValue();
    this.baseSum = record.getBaseSum();
    this.baseVersion = record.getBaseVersion();
    this.name = record.getName();
    this.unit = record.getUnit();
    this.value = record.getValue();
    this.stringValue = record.getStringValue();
    this.booleanValue = record.getBooleanValue();
    this.dataValue = record.getDataValue();
    this.sum = record.getSum();
    this.time = record.getTime();
    this.updateTime = record.getUpdateTime();
    this.extensions = record.getExtensions().toBuilder();
  }

  @Override
  public SenmlRecordBuilder baseName(@Nullable String baseName) {
    this.baseName = baseName;
    return this;
  }

  @Override
  public SenmlRecordBuilder baseTime(@Nullable Number baseTime) {
    this.baseTime = toFiniteDouble(SenmlTextFormat.BASE_TIME, baseTime);
    return this;
  }

  @Override
  public SenmlRecordBuilder baseUnit(@Nullable String baseUnit) {
    this.baseUnit = baseUnit;
    return this;
  }

  @Override
  public SenmlRecordBuilder baseValue(@Nullable Number baseValue) {
    this.baseValue = toFiniteDouble(SenmlTextFormat.BASE_VALUE, baseValue);
    return this;
  }

  @Override
  public SenmlRecordBuilder baseSum(@Nullable Number baseSum) {
    this.baseSum = toFiniteDouble(SenmlTextFormat.BASE_SUM, baseSum);
    return this;
  }

  @Override
  public SenmlRecordBuilder baseVersion(@Nullable Integer baseVersion) {
    this.baseVersion = baseVersion;
    return this;
  }

  @Override
  public SenmlRecordBuilder name(@Nullable String name) {
    this.name = name;
    return this;
  }

  @Override
  public SenmlRecordBuilder unit(@Nullable String unit) {
    this.unit = unit;
    return this;
  }

  @Override
  public SenmlRecordBuilder value(@Nullable Number value) {
    this.value = toFiniteDouble(SenmlTextFormat.VALUE, value);
    return this;
  }

  @Override
  public SenmlRecordBuilder stringValue(@Nullable String stringValue) {
    this.stringValue = stringValue;
    return this;
  }

  @Override
  public SenmlRecordBuilder booleanValue(@Nullable Boolean booleanValue) {
    this.booleanValue = booleanValue;
    return this;
  }

  @Override
  public SenmlRecordBuilder dataValue(@Nullable String dataValue) {
    if (dataValue == null) {
      this.dataValue = null;
      return this;
    }
    try {
      this.dataValue = Base64.getUrlDecoder().decode(dataValue);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "SenML field '"
              + SenmlTextFormat.DATA_VALUE
              + "' must be base64url, got '"
              + dataValue
              + "'",
          e);
    }
    return this;
  }

  @Override
  public SenmlRecordBuilder dataValue(byte @Nullable [] dataValue) {
    this.dataValue = dataValue != null ? dataValue.clone() : null;
    return this;
  }

  @Override
  public SenmlRecordBuilder sum(@Nullable Number sum) {
    this.sum = toFiniteDouble(SenmlTextFormat.SUM, sum);
    return this;
  }

  @Override
  public SenmlRecordBuilder time(@Nullable Number time) {
    this.time = toFiniteDouble(SenmlTextFormat.TIME, time);
    return this;
  }

  @Override
  public SenmlRecordBuilder updateTime(@Nullable Number updateTime) {
    this.updateTime = toFiniteDouble(SenmlTextFormat.UPDATE_TIME, updateTime);
    return this;
  }

  @Override
  public SenmlRecordBuilder extension(String name, String value) {
    extensions.put(name, value);
    return this;
  }

  @Override
  public SenmlRecordBuilder extension(String name, Number value) {
    extensions.put(name, value);
    return this;
  }

  @Override
  public SenmlRecordBuilder extension(String name, boolean value) {
    extensions.put(name, value);
    return this;
  }

  @Override
  public SenmlRecordBuilder extension(String name, byte[] value) {
    extensions.put(name, value);
    return this;
  }

  @Override
  public SenmlRecordBuilder removeExtension(String name) {
    extensions.remove(name);
    return this;
  }

  @Override
  public SenmlRecordBuilder extensions(SenmlExtensions extensions) {
    this.extensions = extensions.toBuilder();
    return this;
  }

  @Override
  public SenmlRecord build() {
    return new DefaultSenmlRecord(this);
  }

  @Override
  public String toString() {
    return new DefaultSenmlRecord(this).toString(SenmlRecordBuilder.class.getSimpleName());
  }

  /**
   * Converts a number to {@code double}. A {@link Float} is converted through its decimal
   * representation, so that e.g. {@code 23.1f} becomes {@code 23.1} rather than {@code
   * 23.100000381469727}, as plain widening would give. Other types are converted with {@link
   * Number#doubleValue()}. Non-finite values are kept as-is.
   *
   * @param value value to convert
   * @return converted value
   */
  static double toDouble(Number value) {
    if (value instanceof Float) {
      return Double.parseDouble(value.toString());
    }
    return value.doubleValue();
  }

  private static @Nullable Double toFiniteDouble(String label, @Nullable Number number) {
    if (number == null) {
      return null;
    }
    double result = toDouble(number);
    if (!Double.isFinite(result)) {
      throw new IllegalArgumentException(
          "SenML field '" + label + "' must be finite, got " + number);
    }
    return result;
  }
}
