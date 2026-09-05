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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * One pass of resolving records of a pack, as defined in RFC 8428, Section 4.6. Base fields seen so
 * far are its state, so records are resolved in order, one at a time, and violations of all of them
 * are collected. Not thread-safe; create one per pass.
 */
final class SenmlResolution {

  /** SenML version defined by RFC 8428, omitted from resolved records. */
  static final int VERSION = 10;

  /** Times below 2^28 are relative to "now", see RFC 8428, Section 4.5.3. */
  private static final double RELATIVE_TIME_THRESHOLD = 268_435_456;

  /** See RFC 8428, Section 4.5.1. */
  private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9\\-:./_]*");

  private final double nowSeconds;
  private final Set<String> extensions;
  private final List<SenmlViolation> violations = new ArrayList<>();
  private int index;

  private String baseName = "";
  private double baseTime;
  private @Nullable String baseUnit;
  private double baseValue;
  private @Nullable Double baseSum;
  private @Nullable Integer baseVersion;

  /**
   * Starts a pass.
   *
   * @param now instant that relative times are relative to
   * @param extensions names of extensions that are mandatory to understand the caller understands
   * @throws IllegalArgumentException if any accepted name is not a mandatory extension name
   */
  SenmlResolution(Instant now, Set<String> extensions) {
    for (String accepted : extensions) {
      if (!SenmlTextFormat.isMandatoryToUnderstand(accepted)
          || !SenmlExtensions.isValidName(accepted)) {
        throw new IllegalArgumentException(
            "'" + accepted + "' is not a mandatory-to-understand SenML extension name");
      }
    }
    this.nowSeconds =
        BigDecimal.valueOf(now.getEpochSecond())
            .add(BigDecimal.valueOf(now.getNano(), 9))
            .doubleValue();
    this.extensions = extensions;
  }

  /**
   * Returns whether given records are in resolved form: each of them is (see {@link
   * #isResolved(SenmlRecord)}) and the Base Version, if any, is the same in every record.
   *
   * @param records records to check
   * @return {@code true} if records are in resolved form, including no records at all
   */
  static boolean isResolved(List<SenmlRecord> records) {
    Integer version = records.isEmpty() ? null : records.get(0).getBaseVersion();
    for (SenmlRecord record : records) {
      if (!isResolved(record) || !Objects.equals(record.getBaseVersion(), version)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns whether given record is in resolved form: no base fields other than a Base Version
   * different from {@value #VERSION}, no base extensions and an absolute time.
   *
   * @param record record to check
   * @return {@code true} if the record is in resolved form
   */
  static boolean isResolved(SenmlRecord record) {
    Integer version = record.getBaseVersion();
    Double time = record.getTime();
    return record.getBaseName() == null
        && record.getBaseTime() == null
        && record.getBaseUnit() == null
        && record.getBaseValue() == null
        && record.getBaseSum() == null
        && (version == null || version != VERSION)
        && time != null
        && time >= RELATIVE_TIME_THRESHOLD
        && record.getExtensions().names().stream().noneMatch(SenmlResolution::isBaseExtension);
  }

  /**
   * Resolves the next record, applying base fields of it and of preceding records, and validates
   * the result.
   *
   * @param record next record of the pack
   * @return resolved record
   */
  SenmlRecord resolve(SenmlRecord record) {
    if (record.getBaseName() != null) {
      baseName = record.getBaseName();
    }
    if (record.getBaseTime() != null) {
      baseTime = record.getBaseTime();
    }
    if (record.getBaseUnit() != null) {
      baseUnit = record.getBaseUnit();
    }
    if (record.getBaseValue() != null) {
      baseValue = record.getBaseValue();
    }
    if (record.getBaseSum() != null) {
      baseSum = record.getBaseSum();
    }
    if (record.getBaseVersion() != null) {
      baseVersion = record.getBaseVersion();
    }

    String name = baseName + (record.getName() != null ? record.getName() : "");
    Double value = record.getValue() != null ? addDecimal(baseValue, record.getValue()) : null;
    Double sum = resolveSum(record.getSum());
    double time = addDecimal(baseTime, record.getTime() != null ? record.getTime() : 0);
    if (time < RELATIVE_TIME_THRESHOLD) {
      time = addDecimal(nowSeconds, time);
    }

    // The builder rejects non-finite numbers, so overflows are reported here and replaced by zero,
    // which keeps the field present for the remaining checks. Any violation rejects the whole pack.
    SenmlRecord resolvedRecord =
        SenmlRecord.builder()
            .baseVersion(baseVersion != null && baseVersion != VERSION ? baseVersion : null)
            .name(name)
            .unit(record.getUnit() != null ? record.getUnit() : baseUnit)
            .value(requireFinite(value, "resolved value is not finite"))
            .stringValue(record.getStringValue())
            .booleanValue(record.getBooleanValue())
            .dataValue(record.getDataValue())
            .sum(requireFinite(sum, "resolved sum is not finite"))
            .time(requireFinite(time, "resolved time is not finite"))
            .updateTime(record.getUpdateTime())
            .extensions(
                DefaultSenmlExtensions.filter(
                    record.getExtensions(), extension -> !isBaseExtension(extension)))
            .build();

    validate(record, resolvedRecord);
    return resolvedRecord;
  }

  /**
   * Validates the next record, already in resolved form, without resolving it.
   *
   * @param resolvedRecord next record of a pack in resolved form
   */
  void validate(SenmlRecord resolvedRecord) {
    validate(resolvedRecord, resolvedRecord);
  }

  /**
   * Ends the pass, reporting violations of all records found so far.
   *
   * @throws SenmlViolationException if any record violates SenML rules
   */
  void throwIfViolated() {
    if (!violations.isEmpty()) {
      throw new SenmlViolationException(violations);
    }
  }

  /**
   * Checks mandatory extensions of the original record and the resolved record against RFC 8428,
   * Sections 4.2, 4.4 and 4.5.1, then moves on to the next record.
   */
  private void validate(SenmlRecord record, SenmlRecord resolvedRecord) {
    for (String extension : record.getExtensions().names()) {
      if (SenmlTextFormat.isMandatoryToUnderstand(extension) && !extensions.contains(extension)) {
        violations.add(
            SenmlViolation.from(index, "mandatory extension '" + extension + "' is not supported"));
      }
    }
    String name = resolvedRecord.getName();
    if (name == null || !NAME_PATTERN.matcher(name).matches()) {
      violations.add(
          SenmlViolation.from(index, "resolved name is empty or contains invalid characters"));
    }
    int valueCount = 0;
    if (resolvedRecord.getValue() != null) {
      valueCount++;
    }
    if (resolvedRecord.getStringValue() != null) {
      valueCount++;
    }
    if (resolvedRecord.getBooleanValue() != null) {
      valueCount++;
    }
    if (resolvedRecord.getDataValue() != null) {
      valueCount++;
    }
    if (valueCount > 1) {
      violations.add(SenmlViolation.from(index, "more than one value field"));
    }
    if (valueCount == 0 && resolvedRecord.getSum() == null) {
      violations.add(SenmlViolation.from(index, "neither value nor sum"));
    }
    index++;
  }

  /** Labels starting with {@code b} have base semantics, see RFC 8428, Section 12.2. */
  private static boolean isBaseExtension(String name) {
    return name.startsWith("b");
  }

  private @Nullable Double resolveSum(@Nullable Double sum) {
    if (baseSum == null && sum == null) {
      return null;
    }
    return addDecimal(baseSum != null ? baseSum : 0, sum != null ? sum : 0);
  }

  /**
   * Adds two numbers in decimal and rounds the result to {@code double} once, so that e.g. {@code
   * 0.1 + 0.2} resolves to {@code 0.3} rather than {@code 0.30000000000000004}. A sum too large for
   * {@code double} becomes infinite and is reported as an overflow.
   */
  private static double addDecimal(double a, double b) {
    if (!Double.isFinite(a) || !Double.isFinite(b)) {
      return a + b;
    }
    return BigDecimal.valueOf(a).add(BigDecimal.valueOf(b)).doubleValue();
  }

  private @Nullable Double requireFinite(@Nullable Double number, String reason) {
    if (number != null && !Double.isFinite(number)) {
      violations.add(SenmlViolation.from(index, reason));
      return 0.0;
    }
    return number;
  }
}
