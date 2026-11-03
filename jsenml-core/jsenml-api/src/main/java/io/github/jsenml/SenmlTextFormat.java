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

import java.math.BigDecimal;
import java.util.Base64;

/**
 * Text format of SenML fields, shared by the text representations: labels, as defined in <a
 * href="https://www.rfc-editor.org/rfc/rfc8428#section-4.1">RFC 8428, Section 4.1</a>, used as
 * object keys by the JSON representation and as attribute names by the XML representation, and
 * number formatting, so that every backend produces the same textual output for the same pack. The
 * CBOR representation uses integer labels instead ({@code SenmlCborLabels} in {@code jsenml-cbor}).
 */
public final class SenmlTextFormat {

  /** Base Name. */
  public static final String BASE_NAME = "bn";

  /** Base Time. */
  public static final String BASE_TIME = "bt";

  /** Base Unit. */
  public static final String BASE_UNIT = "bu";

  /** Base Value. */
  public static final String BASE_VALUE = "bv";

  /** Base Sum. */
  public static final String BASE_SUM = "bs";

  /** Base Version. */
  public static final String BASE_VERSION = "bver";

  /** Name. */
  public static final String NAME = "n";

  /** Unit. */
  public static final String UNIT = "u";

  /** Value. */
  public static final String VALUE = "v";

  /** String Value. */
  public static final String STRING_VALUE = "vs";

  /** Boolean Value. */
  public static final String BOOLEAN_VALUE = "vb";

  /** Data Value. */
  public static final String DATA_VALUE = "vd";

  /** Sum. */
  public static final String SUM = "s";

  /** Time. */
  public static final String TIME = "t";

  /** Update Time. */
  public static final String UPDATE_TIME = "ut";

  /** Longest number literal written in plain notation, see {@link #formatNumber(double)}. */
  private static final int MAX_PLAIN_LENGTH = 18;

  /**
   * Returns whether given label is mandatory to understand. Per RFC 8428, Section 4.4, labels
   * ending with {@code _} must be understood by the recipient, otherwise the whole pack must be
   * rejected.
   *
   * @param label label to check
   * @return {@code true} if the label ends with {@code _}
   */
  public static boolean isMandatoryToUnderstand(String label) {
    return label.endsWith("_");
  }

  /**
   * Formats a finite {@code double} as a number literal without trailing zeros, in plain notation
   * if it takes at most 18 characters (sign aside), otherwise in exponent notation with a lower
   * case {@code e}, as recommended by <a
   * href="https://www.rfc-editor.org/rfc/rfc8428#section-5">RFC 8428, Section 5</a> (mantissa less
   * than 19 characters). E.g. {@code 1.27602E9} becomes {@code 1276020000}, {@code 21.0} becomes
   * {@code 21}, {@code 1.0E300} becomes {@code 1e300} and {@code 1.5E-20} becomes {@code 1.5e-20}.
   *
   * @param number finite number
   * @return number literal
   * @throws IllegalArgumentException if number is not finite
   */
  public static String formatNumber(double number) {
    if (!Double.isFinite(number)) {
      throw new IllegalArgumentException("SenML number must be finite, got " + number);
    }
    if (number == 0) {
      return "0";
    }
    BigDecimal decimal = BigDecimal.valueOf(number).stripTrailingZeros();
    String plain = decimal.toPlainString();
    int sign = decimal.signum() < 0 ? 1 : 0;
    if (plain.length() - sign <= MAX_PLAIN_LENGTH) {
      return plain;
    }
    String digits = decimal.unscaledValue().abs().toString();
    StringBuilder literal = new StringBuilder(digits.length() + 8);
    if (sign == 1) {
      literal.append('-');
    }
    literal.append(digits.charAt(0));
    if (digits.length() > 1) {
      literal.append('.').append(digits, 1, digits.length());
    }
    return literal.append('e').append(digits.length() - 1 - decimal.scale()).toString();
  }

  /**
   * Formats octets as unpadded base64url, the representation of the Data Value and of octet
   * extensions in text representations (RFC 8428, Sections 5 and 7).
   *
   * @param bytes octets to format
   * @return base64url text without padding
   */
  public static String formatBytes(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private SenmlTextFormat() {}
}
