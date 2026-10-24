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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SenmlViolationExceptionTest {

  private static final String INVALID_NAME =
      "resolved name is empty or contains invalid characters";

  private static final String MISSING_VALUE_AND_SUM = "neither value nor sum";

  @Test
  void givenViolations_whenCreating_thenExposesThemInOrder() {
    SenmlViolationException e =
        new SenmlViolationException(
            List.of(
                SenmlViolation.from(2, MISSING_VALUE_AND_SUM),
                SenmlViolation.from(0, INVALID_NAME)));

    assertThat(e.getViolations())
        .containsExactly(
            SenmlViolation.from(2, MISSING_VALUE_AND_SUM), SenmlViolation.from(0, INVALID_NAME));
  }

  @Test
  void givenViolations_whenCreating_thenDerivesMessageFromReasonDescriptions() {
    SenmlViolationException e =
        new SenmlViolationException(
            List.of(
                SenmlViolation.from(0, INVALID_NAME),
                SenmlViolation.from(2, MISSING_VALUE_AND_SUM)));

    assertThat(e)
        .hasMessage(
            "2 SenML violation(s): record #0 resolved name is empty or contains invalid characters;"
                + " record #2 neither value nor sum");
  }

  @Test
  void givenMutableList_whenCreating_thenViolationsAreCopiedAndUnmodifiable() {
    List<SenmlViolation> violations =
        new ArrayList<>(List.of(SenmlViolation.from(0, INVALID_NAME)));

    SenmlViolationException e = new SenmlViolationException(violations);
    violations.add(SenmlViolation.from(1, INVALID_NAME));

    assertThat(e.getViolations()).containsExactly(SenmlViolation.from(0, INVALID_NAME));
    assertThatThrownBy(() -> e.getViolations().add(SenmlViolation.from(1, INVALID_NAME)))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void givenNoViolations_whenCreating_thenThrows() {
    assertThatThrownBy(() -> new SenmlViolationException(List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
