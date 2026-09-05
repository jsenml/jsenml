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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when a {@link SenmlPack} violates SenML rules, for example during {@link
 * SenmlPack#resolve()}.
 *
 * <p>Validation does not stop at the first problem; all violations found are available via {@link
 * #getViolations()}, in pack order. The exception message is derived from them for logging only and
 * is not meant to be parsed.
 */
public class SenmlViolationException extends SenmlException {

  @Serial //
  private static final long serialVersionUID = 1L;

  /** Violations found, in pack order. */
  private final List<SenmlViolation> violations;

  /**
   * Creates a new exception.
   *
   * @param violations violations found, in pack order, must not be empty
   * @throws IllegalArgumentException if violations are empty
   */
  public SenmlViolationException(List<SenmlViolation> violations) {
    super(describe(violations));
    this.violations = List.copyOf(violations);
  }

  /**
   * Returns all violations found, in pack order.
   *
   * @return unmodifiable, non-empty list of violations
   */
  public List<SenmlViolation> getViolations() {
    return violations;
  }

  private static String describe(List<SenmlViolation> violations) {
    if (violations.isEmpty()) {
      throw new IllegalArgumentException("violations must not be empty");
    }
    return violations.size()
        + " SenML violation(s): "
        + violations.stream()
            .map(v -> "record #" + v.getRecordIndex() + " " + v.getReason())
            .collect(Collectors.joining("; "));
  }
}
