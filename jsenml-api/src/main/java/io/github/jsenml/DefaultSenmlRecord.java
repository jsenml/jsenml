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
import java.util.Objects;
import java.util.StringJoiner;
import org.jspecify.annotations.Nullable;

/** Default implementation of {@link SenmlRecord}. */
final class DefaultSenmlRecord implements SenmlRecord, Serializable {

  @Serial //
  private static final long serialVersionUID = 1L;

  private final @Nullable String baseName;
  private final @Nullable Double baseTime;
  private final @Nullable String baseUnit;
  private final @Nullable Double baseValue;
  private final @Nullable Double baseSum;
  private final @Nullable Integer baseVersion;
  private final @Nullable String name;
  private final @Nullable String unit;
  private final @Nullable Double value;
  private final @Nullable String stringValue;
  private final @Nullable Boolean booleanValue;
  private final byte @Nullable [] dataValue;
  private final @Nullable Double sum;
  private final @Nullable Double time;
  private final @Nullable Double updateTime;
  private final SenmlExtensions extensions;

  DefaultSenmlRecord(DefaultSenmlRecordBuilder builder) {
    this.baseName = builder.baseName;
    this.baseTime = builder.baseTime;
    this.baseUnit = builder.baseUnit;
    this.baseValue = builder.baseValue;
    this.baseSum = builder.baseSum;
    this.baseVersion = builder.baseVersion;
    this.name = builder.name;
    this.unit = builder.unit;
    this.value = builder.value;
    this.stringValue = builder.stringValue;
    this.booleanValue = builder.booleanValue;
    this.dataValue = builder.dataValue;
    this.sum = builder.sum;
    this.time = builder.time;
    this.updateTime = builder.updateTime;
    this.extensions = builder.extensions.build();
  }

  @Override
  public SenmlRecordBuilder toBuilder() {
    return new DefaultSenmlRecordBuilder(this);
  }

  @Override
  public @Nullable String getBaseName() {
    return baseName;
  }

  @Override
  public @Nullable Double getBaseTime() {
    return baseTime;
  }

  @Override
  public @Nullable String getBaseUnit() {
    return baseUnit;
  }

  @Override
  public @Nullable Double getBaseValue() {
    return baseValue;
  }

  @Override
  public @Nullable Double getBaseSum() {
    return baseSum;
  }

  @Override
  public @Nullable Integer getBaseVersion() {
    return baseVersion;
  }

  @Override
  public @Nullable String getName() {
    return name;
  }

  @Override
  public @Nullable String getUnit() {
    return unit;
  }

  @Override
  public @Nullable Double getValue() {
    return value;
  }

  @Override
  public @Nullable String getStringValue() {
    return stringValue;
  }

  @Override
  public @Nullable Boolean getBooleanValue() {
    return booleanValue;
  }

  @Override
  public byte @Nullable [] getDataValue() {
    return dataValue != null ? dataValue.clone() : null;
  }

  @Override
  public @Nullable Double getSum() {
    return sum;
  }

  @Override
  public @Nullable Double getTime() {
    return time;
  }

  @Override
  public @Nullable Double getUpdateTime() {
    return updateTime;
  }

  @Override
  public boolean hasBaseFields() {
    return baseName != null
        || baseTime != null
        || baseUnit != null
        || baseValue != null
        || baseSum != null
        || baseVersion != null;
  }

  @Override
  public SenmlExtensions getExtensions() {
    return extensions;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SenmlRecord other)) {
      return false;
    }
    return Objects.equals(baseName, other.getBaseName())
        && Objects.equals(baseTime, other.getBaseTime())
        && Objects.equals(baseUnit, other.getBaseUnit())
        && Objects.equals(baseValue, other.getBaseValue())
        && Objects.equals(baseSum, other.getBaseSum())
        && Objects.equals(baseVersion, other.getBaseVersion())
        && Objects.equals(name, other.getName())
        && Objects.equals(unit, other.getUnit())
        && Objects.equals(value, other.getValue())
        && Objects.equals(stringValue, other.getStringValue())
        && Objects.equals(booleanValue, other.getBooleanValue())
        && Arrays.equals(dataValue, other.getDataValue())
        && Objects.equals(sum, other.getSum())
        && Objects.equals(time, other.getTime())
        && Objects.equals(updateTime, other.getUpdateTime())
        && extensions.equals(other.getExtensions());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        baseName,
        baseTime,
        baseUnit,
        baseValue,
        baseSum,
        baseVersion,
        name,
        unit,
        value,
        stringValue,
        booleanValue,
        Arrays.hashCode(dataValue),
        sum,
        time,
        updateTime,
        extensions);
  }

  @Override
  public String toString() {
    return toString(SenmlRecord.class.getSimpleName());
  }

  String toString(String typeName) {
    StringJoiner joiner = new StringJoiner(", ", typeName + "[", "]");
    append(joiner, SenmlTextFormat.BASE_NAME, baseName);
    append(joiner, SenmlTextFormat.BASE_TIME, baseTime);
    append(joiner, SenmlTextFormat.BASE_UNIT, baseUnit);
    append(joiner, SenmlTextFormat.BASE_VALUE, baseValue);
    append(joiner, SenmlTextFormat.BASE_SUM, baseSum);
    append(joiner, SenmlTextFormat.BASE_VERSION, baseVersion);
    append(joiner, SenmlTextFormat.NAME, name);
    append(joiner, SenmlTextFormat.UNIT, unit);
    append(joiner, SenmlTextFormat.TIME, time);
    append(joiner, SenmlTextFormat.UPDATE_TIME, updateTime);
    append(joiner, SenmlTextFormat.VALUE, value);
    append(joiner, SenmlTextFormat.STRING_VALUE, stringValue);
    append(joiner, SenmlTextFormat.BOOLEAN_VALUE, booleanValue);
    if (dataValue != null) {
      append(joiner, SenmlTextFormat.DATA_VALUE, SenmlTextFormat.formatBytes(dataValue));
    }
    append(joiner, SenmlTextFormat.SUM, sum);
    for (String extension : extensions.names()) {
      Object extensionValue = extensions.get(extension);
      if (extensionValue != null) {
        append(joiner, extension, DefaultSenmlExtensions.format(extensionValue));
      }
    }
    return joiner.toString();
  }

  private void append(StringJoiner joiner, String label, @Nullable Object value) {
    if (value != null) {
      joiner.add(label + "=" + value);
    }
  }
}
