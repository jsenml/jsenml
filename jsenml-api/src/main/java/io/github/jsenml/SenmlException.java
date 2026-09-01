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
import org.jspecify.annotations.Nullable;

/** Base class of all exceptions thrown during SenML operations. */
public class SenmlException extends RuntimeException {

  @Serial //
  private static final long serialVersionUID = 1L;

  /** Creates a new exception with no detail message and no cause. */
  public SenmlException() {
    super();
  }

  /**
   * Creates a new exception with the specified detail message.
   *
   * @param message human-readable explanation of the failure (may be {@code null})
   */
  public SenmlException(@Nullable String message) {
    super(message);
  }

  /**
   * Creates a new exception wrapping the given cause.
   *
   * @param cause underlying cause (may be {@code null})
   */
  public SenmlException(@Nullable Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new exception with the specified detail message and cause.
   *
   * @param message human-readable explanation of the failure (may be {@code null})
   * @param cause underlying cause (may be {@code null})
   */
  public SenmlException(@Nullable String message, @Nullable Throwable cause) {
    super(message, cause);
  }

  /**
   * Advanced constructor allowing full control over suppression and stack trace writability.
   * Typically used only internally or in tests.
   *
   * @param message detail message (may be {@code null})
   * @param cause underlying cause (may be {@code null})
   * @param enableSuppression whether suppression is enabled or disabled
   * @param writableStackTrace whether the stack trace should be writable
   */
  protected SenmlException(
      @Nullable String message,
      @Nullable Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
