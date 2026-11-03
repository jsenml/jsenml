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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SenmlTextFormatTest {

  @ParameterizedTest
  @CsvSource({
    "0.0, 0",
    "-0.0, 0",
    "21.0, 21",
    "100.0, 100",
    "23.1, 23.1",
    "-1.25, -1.25",
    "1.320067464E9, 1320067464",
    "1.320067464123E9, 1320067464.123",
    "1.0E-7, 0.0000001",
    "1.0E17, 100000000000000000",
    "-1.0E17, -100000000000000000",
    "1.0E-16, 0.0000000000000001",
    "1.2345678901234567E8, 123456789.01234567"
  })
  void givenNumberWithShortPlainForm_whenFormatting_thenWritesPlainNumber(
      double number, String expected) {
    assertThat(SenmlTextFormat.formatNumber(number)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    "1.0E18, 1e18",
    "-1.0E18, -1e18",
    "1.5E20, 1.5e20",
    "1.0E300, 1e300",
    "-1.7976931348623157E308, -1.7976931348623157e308",
    "1.0E-17, 1e-17",
    "1.5E-20, 1.5e-20",
    "4.9E-324, 4.9e-324",
    "1.2345678901234567E20, 1.2345678901234567e20"
  })
  void givenNumberWithLongPlainForm_whenFormatting_thenWritesExponentNotation(
      double number, String expected) {
    assertThat(SenmlTextFormat.formatNumber(number)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      doubles = {1.0E18, 1.5E-20, -1.7976931348623157E308, 4.9E-324, 1.2345678901234567E20})
  void givenNumberInExponentNotation_whenParsing_thenGivesSameDouble(double number) {
    assertThat(Double.parseDouble(SenmlTextFormat.formatNumber(number))).isEqualTo(number);
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void givenNonFiniteNumber_whenFormatting_thenThrows(double number) {
    assertThatThrownBy(() -> SenmlTextFormat.formatNumber(number))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
