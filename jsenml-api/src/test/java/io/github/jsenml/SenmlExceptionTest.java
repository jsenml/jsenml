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

import org.junit.jupiter.api.Test;

class SenmlExceptionTest {

  @Test
  void givenNoArguments_whenCreating_thenHasNoMessageAndNoCause() {
    SenmlException e = new SenmlException();

    assertThat(e).hasMessage(null).hasNoCause();
  }

  @Test
  void givenMessage_whenCreating_thenHasMessageAndNoCause() {
    SenmlException e = new SenmlException("failure");

    assertThat(e).hasMessage("failure").hasNoCause();
  }

  @Test
  void givenCause_whenCreating_thenHasCauseAndMessageFromCause() {
    IllegalStateException cause = new IllegalStateException("root");

    SenmlException e = new SenmlException(cause);

    assertThat(e).hasCause(cause).hasMessage(cause.toString());
  }

  @Test
  void givenMessageAndCause_whenCreating_thenHasBoth() {
    IllegalStateException cause = new IllegalStateException("root");

    SenmlException e = new SenmlException("failure", cause);

    assertThat(e).hasMessage("failure").hasCause(cause);
  }

  @Test
  void givenSuppressionAndStackTraceDisabled_whenCreating_thenDropsSuppressedAndStackTrace() {
    SenmlException e = new SenmlException("failure", null, false, false) {};

    e.addSuppressed(new IllegalStateException("suppressed"));

    assertThat(e).hasMessage("failure").hasNoCause();
    assertThat(e.getSuppressed()).isEmpty();
    assertThat(e.getStackTrace()).isEmpty();
  }

  @Test
  void givenSuppressionAndStackTraceEnabled_whenCreating_thenKeepsSuppressedAndStackTrace() {
    SenmlException e = new SenmlException("failure", null, true, true) {};

    e.addSuppressed(new IllegalStateException("suppressed"));

    assertThat(e.getSuppressed()).hasSize(1);
    assertThat(e.getStackTrace()).isNotEmpty();
  }
}
