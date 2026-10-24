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

import org.junit.jupiter.api.Test;

class SenmlViolationTest {

  private static final String INVALID_NAME =
      "resolved name is empty or contains invalid characters";

  private static final String MISSING_VALUE_AND_SUM = "neither value nor sum";

  @Test
  void givenIndexAndReason_whenCreating_thenExposesThem() {
    SenmlViolation violation = SenmlViolation.from(3, INVALID_NAME);

    assertThat(violation.getRecordIndex()).isEqualTo(3);
    assertThat(violation.getReason()).isEqualTo(INVALID_NAME);
  }

  @Test
  void givenEqualViolations_whenComparing_thenEqualAndSameHashCode() {
    assertThat(SenmlViolation.from(1, INVALID_NAME))
        .isEqualTo(SenmlViolation.from(1, INVALID_NAME))
        .hasSameHashCodeAs(SenmlViolation.from(1, INVALID_NAME))
        .isNotEqualTo(SenmlViolation.from(2, INVALID_NAME))
        .isNotEqualTo(SenmlViolation.from(1, MISSING_VALUE_AND_SUM));
  }

  @Test
  void givenViolation_whenToString_thenUsesBrackets() {
    assertThat(SenmlViolation.from(1, INVALID_NAME))
        .hasToString(
            "SenmlViolation[recordIndex=1, reason=resolved name is empty or contains invalid characters]");
  }

  @SuppressWarnings("NullAway")
  @Test
  void givenNullReason_whenCreating_thenThrows() {
    assertThatThrownBy(() -> SenmlViolation.from(0, null)).isInstanceOf(NullPointerException.class);
  }
}
