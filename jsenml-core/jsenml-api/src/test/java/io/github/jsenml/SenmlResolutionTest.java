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

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SenmlResolutionTest {

  private static final String INVALID_NAME =
      "resolved name is empty or contains invalid characters";

  private static final String MULTIPLE_VALUE_FIELDS = "more than one value field";

  private static final String MISSING_VALUE_AND_SUM = "neither value nor sum";

  private static final SenmlRecord VALID = SenmlRecord.builder().name("a").value(1.0).build();

  @Test
  void givenValidResolvedRecord_whenValidatingResolved_thenReturnsNoViolations() {
    assertThat(violationsAt(0, SenmlRecord.builder().name("dev:temp").value(1.0).build()))
        .isEmpty();
    assertThat(violationsAt(0, SenmlRecord.builder().name("dev:energy").sum(1.0).build()))
        .isEmpty();
    assertThat(violationsAt(0, SenmlRecord.builder().name("dev:power").value(1.0).sum(1.0).build()))
        .isEmpty();
  }

  @Test
  void givenMissingOrInvalidName_whenValidatingResolved_thenReportsInvalidName() {
    assertThat(violationsAt(3, SenmlRecord.builder().value(1.0).build()))
        .containsExactly(SenmlViolation.from(3, INVALID_NAME));
    assertThat(violationsAt(3, SenmlRecord.builder().name("").value(1.0).build()))
        .containsExactly(SenmlViolation.from(3, INVALID_NAME));
    assertThat(violationsAt(3, SenmlRecord.builder().name("-temp").value(1.0).build()))
        .containsExactly(SenmlViolation.from(3, INVALID_NAME));
  }

  @Test
  void givenMultipleValueFields_whenValidatingResolved_thenReportsMultipleValueFields() {
    SenmlRecord record =
        SenmlRecord.builder().name("a").value(1.0).stringValue("x").dataValue("AQID").build();

    assertThat(violationsAt(0, record))
        .containsExactly(SenmlViolation.from(0, MULTIPLE_VALUE_FIELDS));
  }

  @Test
  void givenNeitherValueNorSum_whenValidatingResolved_thenReportsMissingValueAndSum() {
    assertThat(violationsAt(0, SenmlRecord.builder().name("a").build()))
        .containsExactly(SenmlViolation.from(0, MISSING_VALUE_AND_SUM));
  }

  @Test
  void givenSeveralProblems_whenValidatingResolved_thenReportsAllInCheckOrder() {
    SenmlRecord record = SenmlRecord.builder().booleanValue(true).stringValue("x").build();

    assertThat(violationsAt(1, record))
        .containsExactly(
            SenmlViolation.from(1, INVALID_NAME), SenmlViolation.from(1, MULTIPLE_VALUE_FIELDS));
  }

  /** Validates given record preceded by {@code index} valid ones, returning its violations. */
  private static List<SenmlViolation> violationsAt(int index, SenmlRecord record) {
    SenmlResolution resolution = new SenmlResolution(Instant.EPOCH, Set.of());
    for (int i = 0; i < index; i++) {
      resolution.validate(VALID);
    }
    resolution.validate(record);
    try {
      resolution.throwIfViolated();
      return List.of();
    } catch (SenmlViolationException e) {
      return e.getViolations();
    }
  }
}
