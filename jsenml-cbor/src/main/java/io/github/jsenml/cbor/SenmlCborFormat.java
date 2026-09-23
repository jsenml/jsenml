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

package io.github.jsenml.cbor;

import io.github.jsenml.SenmlTextFormat;

/**
 * Integer map keys of SenML CBOR representation, as defined in <a
 * href="https://www.rfc-editor.org/rfc/rfc8428#section-6">RFC 8428, Section 6</a>.
 *
 * <p>The set of integer keys is closed; extension labels are encoded as text-string map keys, using
 * the same text labels (see {@link SenmlTextFormat}).
 */
public final class SenmlCborFormat {

  /** Base Version. */
  public static final int BASE_VERSION = -1;

  /** Base Name. */
  public static final int BASE_NAME = -2;

  /** Base Time. */
  public static final int BASE_TIME = -3;

  /** Base Unit. */
  public static final int BASE_UNIT = -4;

  /** Base Value. */
  public static final int BASE_VALUE = -5;

  /** Base Sum. */
  public static final int BASE_SUM = -6;

  /** Name. */
  public static final int NAME = 0;

  /** Unit. */
  public static final int UNIT = 1;

  /** Value. */
  public static final int VALUE = 2;

  /** String Value. */
  public static final int STRING_VALUE = 3;

  /** Boolean Value. */
  public static final int BOOLEAN_VALUE = 4;

  /** Sum. */
  public static final int SUM = 5;

  /** Time. */
  public static final int TIME = 6;

  /** Update Time. */
  public static final int UPDATE_TIME = 7;

  /** Data Value. */
  public static final int DATA_VALUE = 8;

  /**
   * Returns whether given text-string extension label is mandatory to understand. Per RFC 8428,
   * Section 4.4, labels ending with {@code _} must be understood by the recipient, otherwise the
   * whole pack must be rejected.
   *
   * @param label text-string label to check
   * @return {@code true} if the label ends with {@code _}
   */
  public static boolean isMandatoryToUnderstand(String label) {
    return label.endsWith("_");
  }

  private SenmlCborFormat() {}
}
