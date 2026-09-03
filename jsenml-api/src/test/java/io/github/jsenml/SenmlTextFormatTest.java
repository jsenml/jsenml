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
    "1.0E-7, 0.0000001"
  })
  void givenFiniteNumber_whenFormatting_thenWritesPlainNumber(double number, String expected) {
    assertThat(SenmlTextFormat.formatNumber(number)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void givenNonFiniteNumber_whenFormatting_thenThrows(double number) {
    assertThatThrownBy(() -> SenmlTextFormat.formatNumber(number))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
