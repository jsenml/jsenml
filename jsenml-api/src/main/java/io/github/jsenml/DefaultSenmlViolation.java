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
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Default implementation of {@link SenmlViolation}. */
final class DefaultSenmlViolation implements SenmlViolation, Serializable {

  @Serial //
  private static final long serialVersionUID = 1L;

  private final int recordIndex;
  private final String reason;

  DefaultSenmlViolation(int recordIndex, String reason) {
    this.recordIndex = recordIndex;
    this.reason = Objects.requireNonNull(reason, "reason");
  }

  @Override
  public int getRecordIndex() {
    return recordIndex;
  }

  @Override
  public String getReason() {
    return reason;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SenmlViolation other)) {
      return false;
    }
    return recordIndex == other.getRecordIndex() && reason.equals(other.getReason());
  }

  @Override
  public int hashCode() {
    return Objects.hash(recordIndex, reason);
  }

  @Override
  public String toString() {
    return "SenmlViolation[recordIndex=" + recordIndex + ", reason=" + reason + "]";
  }
}
