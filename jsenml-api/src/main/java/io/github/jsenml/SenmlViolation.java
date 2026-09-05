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

/**
 * Immutable description of a single SenML rule broken by a record within a {@code SenmlPack}.
 *
 * @see SenmlViolationException#getViolations()
 */
public sealed interface SenmlViolation permits DefaultSenmlViolation {

  /**
   * Creates a violation.
   *
   * @param recordIndex zero-based index of the offending record within the pack
   * @param reason human-readable description of the rule the record breaks
   * @return new violation
   */
  static SenmlViolation from(int recordIndex, String reason) {
    return new DefaultSenmlViolation(recordIndex, reason);
  }

  /**
   * Returns zero-based index of the offending record within the pack.
   *
   * @return record index
   */
  int getRecordIndex();

  /**
   * Returns human-readable description of the rule the record breaks, e.g. {@code "neither value
   * nor sum"}.
   *
   * @return violation reason
   */
  String getReason();
}
