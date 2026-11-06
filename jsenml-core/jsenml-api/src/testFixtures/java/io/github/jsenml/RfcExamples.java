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
 * Examples of <a href="https://www.rfc-editor.org/rfc/rfc8428">RFC 8428</a>, copied verbatim from
 * the RFC text, with the packs they represent. Shared by contract tests of all codec backends.
 */
public final class RfcExamples {

  /** JSON example of RFC 8428, Section 5.1.1. */
  public static final String SINGLE_DATA_POINT_JSON =
      """
      [
        {"n":"urn:dev:ow:10e2073a01080063","u":"Cel","v":23.1}
      ]
      """;

  /** Pack of {@link #SINGLE_DATA_POINT_JSON}. */
  public static final SenmlPack SINGLE_DATA_POINT =
      SenmlPack.of(
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("Cel")
              .value(23.1)
              .build());

  /** JSON example of RFC 8428, Section 5.1.2. */
  public static final String MULTIPLE_DATA_POINTS_JSON =
      """
      [
        {"bn":"urn:dev:ow:10e2073a01080063:","n":"voltage","u":"V","v":120.1},
        {"n":"current","u":"A","v":1.2}
      ]
      """;

  /** Pack of {@link #MULTIPLE_DATA_POINTS_JSON}. */
  public static final SenmlPack MULTIPLE_DATA_POINTS =
      SenmlPack.of(
          SenmlRecord.builder()
              .baseName("urn:dev:ow:10e2073a01080063:")
              .name("voltage")
              .unit("V")
              .value(120.1)
              .build(),
          SenmlRecord.builder().name("current").unit("A").value(1.2).build());

  /** JSON example of RFC 8428, Section 5.1.2. */
  public static final String MULTIPLE_DATA_POINTS_WITH_TIME_JSON =
      """
      [
        {"bn":"urn:dev:ow:10e2073a0108006:","bt":1.276020076001e+09,
         "bu":"A","bver":5,
         "n":"voltage","u":"V","v":120.1},
        {"n":"current","t":-5,"v":1.2},
        {"n":"current","t":-4,"v":1.3},
        {"n":"current","t":-3,"v":1.4},
        {"n":"current","t":-2,"v":1.5},
        {"n":"current","t":-1,"v":1.6},
        {"n":"current","v":1.7}
      ]
      """;

  /** Pack of {@link #MULTIPLE_DATA_POINTS_WITH_TIME_JSON}. */
  public static final SenmlPack MULTIPLE_DATA_POINTS_WITH_TIME =
      SenmlPack.of(
          SenmlRecord.builder()
              .baseName("urn:dev:ow:10e2073a0108006:")
              .baseTime(1.276020076001e9)
              .baseUnit("A")
              .baseVersion(5)
              .name("voltage")
              .unit("V")
              .value(120.1)
              .build(),
          SenmlRecord.builder().name("current").time(-5).value(1.2).build(),
          SenmlRecord.builder().name("current").time(-4).value(1.3).build(),
          SenmlRecord.builder().name("current").time(-3).value(1.4).build(),
          SenmlRecord.builder().name("current").time(-2).value(1.5).build(),
          SenmlRecord.builder().name("current").time(-1).value(1.6).build(),
          SenmlRecord.builder().name("current").value(1.7).build());

  /** JSON example of RFC 8428, Section 5.1.3. */
  public static final String MULTIPLE_MEASUREMENTS_JSON =
      """
      [
        {"bn":"urn:dev:ow:10e2073a01080063","bt":1.320067464e+09,
         "bu":"%RH","v":20},
        {"u":"lon","v":24.30621},
        {"u":"lat","v":60.07965},
        {"t":60,"v":20.3},
        {"u":"lon","t":60,"v":24.30622},
        {"u":"lat","t":60,"v":60.07965},
        {"t":120,"v":20.7},
        {"u":"lon","t":120,"v":24.30623},
        {"u":"lat","t":120,"v":60.07966},
        {"u":"%EL","t":150,"v":98},
        {"t":180,"v":21.2},
        {"u":"lon","t":180,"v":24.30628},
        {"u":"lat","t":180,"v":60.07967}
      ]
      """;

  /** Pack of {@link #MULTIPLE_MEASUREMENTS_JSON}. */
  public static final SenmlPack MULTIPLE_MEASUREMENTS =
      SenmlPack.of(
          SenmlRecord.builder()
              .baseName("urn:dev:ow:10e2073a01080063")
              .baseTime(1.320067464e9)
              .baseUnit("%RH")
              .value(20)
              .build(),
          SenmlRecord.builder().unit("lon").value(24.30621).build(),
          SenmlRecord.builder().unit("lat").value(60.07965).build(),
          SenmlRecord.builder().time(60).value(20.3).build(),
          SenmlRecord.builder().unit("lon").time(60).value(24.30622).build(),
          SenmlRecord.builder().unit("lat").time(60).value(60.07965).build(),
          SenmlRecord.builder().time(120).value(20.7).build(),
          SenmlRecord.builder().unit("lon").time(120).value(24.30623).build(),
          SenmlRecord.builder().unit("lat").time(120).value(60.07966).build(),
          SenmlRecord.builder().unit("%EL").time(150).value(98).build(),
          SenmlRecord.builder().time(180).value(21.2).build(),
          SenmlRecord.builder().unit("lon").time(180).value(24.30628).build(),
          SenmlRecord.builder().unit("lat").time(180).value(60.07967).build());

  /** JSON example of RFC 8428, Section 5.1.4. */
  public static final String RESOLVED_DATA_JSON =
      """
      [
        {"n":"urn:dev:ow:10e2073a01080063","u":"%RH","t":1.320067464e+09,
         "v":20},
        {"n":"urn:dev:ow:10e2073a01080063","u":"lon","t":1.320067464e+09,
         "v":24.30621},
        {"n":"urn:dev:ow:10e2073a01080063","u":"lat","t":1.320067464e+09,
         "v":60.07965},
        {"n":"urn:dev:ow:10e2073a01080063","u":"%RH","t":1.320067524e+09,
         "v":20.3},
        {"n":"urn:dev:ow:10e2073a01080063","u":"lon","t":1.320067524e+09,
         "v":24.30622},
        {"n":"urn:dev:ow:10e2073a01080063","u":"lat","t":1.320067524e+09,
         "v":60.07965},
        {"n":"urn:dev:ow:10e2073a01080063","u":"%RH","t":1.320067584e+09,
         "v":20.7},
        {"n":"urn:dev:ow:10e2073a01080063","u":"lon","t":1.320067584e+09,
         "v":24.30623},
        {"n":"urn:dev:ow:10e2073a01080063","u":"lat","t":1.320067584e+09,
         "v":60.07966},
        {"n":"urn:dev:ow:10e2073a01080063","u":"%EL","t":1.320067614e+09,
         "v":98},
        {"n":"urn:dev:ow:10e2073a01080063","u":"%RH","t":1.320067644e+09,
         "v":21.2},
        {"n":"urn:dev:ow:10e2073a01080063","u":"lon","t":1.320067644e+09,
         "v":24.30628},
        {"n":"urn:dev:ow:10e2073a01080063","u":"lat","t":1.320067644e+09,
         "v":60.07967}
      ]
      """;

  /** Pack of {@link #RESOLVED_DATA_JSON}. */
  public static final SenmlPack RESOLVED_DATA =
      SenmlPack.of(
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("%RH")
              .time(1.320067464e9)
              .value(20)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("lon")
              .time(1.320067464e9)
              .value(24.30621)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("lat")
              .time(1.320067464e9)
              .value(60.07965)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("%RH")
              .time(1.320067524e9)
              .value(20.3)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("lon")
              .time(1.320067524e9)
              .value(24.30622)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("lat")
              .time(1.320067524e9)
              .value(60.07965)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("%RH")
              .time(1.320067584e9)
              .value(20.7)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("lon")
              .time(1.320067584e9)
              .value(24.30623)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("lat")
              .time(1.320067584e9)
              .value(60.07966)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("%EL")
              .time(1.320067614e9)
              .value(98)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("%RH")
              .time(1.320067644e9)
              .value(21.2)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("lon")
              .time(1.320067644e9)
              .value(24.30628)
              .build(),
          SenmlRecord.builder()
              .name("urn:dev:ow:10e2073a01080063")
              .unit("lat")
              .time(1.320067644e9)
              .value(60.07967)
              .build());

  /** JSON example of RFC 8428, Section 5.1.5. */
  public static final String MULTIPLE_DATA_TYPES_JSON =
      """
      [
        {"bn":"urn:dev:ow:10e2073a01080063:","n":"temp","u":"Cel","v":23.1},
        {"n":"label","vs":"Machine Room"},
        {"n":"open","vb":false},
        {"n":"nfc-reader","vd":"aGkgCg"}
      ]
      """;

  /** Pack of {@link #MULTIPLE_DATA_TYPES_JSON}. */
  public static final SenmlPack MULTIPLE_DATA_TYPES =
      SenmlPack.of(
          SenmlRecord.builder()
              .baseName("urn:dev:ow:10e2073a01080063:")
              .name("temp")
              .unit("Cel")
              .value(23.1)
              .build(),
          SenmlRecord.builder().name("label").stringValue("Machine Room").build(),
          SenmlRecord.builder().name("open").booleanValue(false).build(),
          SenmlRecord.builder().name("nfc-reader").dataValue("aGkgCg").build());

  /** JSON example of RFC 8428, Section 5.1.6. */
  public static final String COLLECTION_OF_RESOURCES_JSON =
      """
      [
        {"bn":"2001:db8::2/","bt":1.320078429e+09,
         "n":"temperature","u":"Cel","v":25.2},
        {"n":"humidity","u":"%RH","v":30},
        {"bn":"2001:db8::1/","n":"temperature","u":"Cel","v":12.3},
        {"n":"humidity","u":"%RH","v":67}
      ]
      """;

  /** Pack of {@link #COLLECTION_OF_RESOURCES_JSON}. */
  public static final SenmlPack COLLECTION_OF_RESOURCES =
      SenmlPack.of(
          SenmlRecord.builder()
              .baseName("2001:db8::2/")
              .baseTime(1.320078429e9)
              .name("temperature")
              .unit("Cel")
              .value(25.2)
              .build(),
          SenmlRecord.builder().name("humidity").unit("%RH").value(30).build(),
          SenmlRecord.builder()
              .baseName("2001:db8::1/")
              .name("temperature")
              .unit("Cel")
              .value(12.3)
              .build(),
          SenmlRecord.builder().name("humidity").unit("%RH").value(67).build());

  /** JSON example of RFC 8428, Section 5.1.7. */
  public static final String SETTING_AN_ACTUATOR_JSON =
      """
      [
        {"bn":"urn:dev:ow:10e2073a01080063:"},
        {"n":"temp","u":"Cel","v":23.1},
        {"n":"heat","u":"/","v":1},
        {"n":"fan","u":"/","v":0}
      ]
      """;

  /** Pack of {@link #SETTING_AN_ACTUATOR_JSON}. */
  public static final SenmlPack SETTING_AN_ACTUATOR =
      SenmlPack.of(
          SenmlRecord.builder().baseName("urn:dev:ow:10e2073a01080063:").build(),
          SenmlRecord.builder().name("temp").unit("Cel").value(23.1).build(),
          SenmlRecord.builder().name("heat").unit("/").value(1).build(),
          SenmlRecord.builder().name("fan").unit("/").value(0).build());

  /** JSON example of RFC 8428, Section 5.1.7. */
  public static final String TURNING_LIGHTS_ON_JSON =
      """
      [
        {"bt":1.320078429e+09,"bu":"/","n":"2001:db8::3","v":1},
        {"n":"2001:db8::4","v":1}
      ]
      """;

  /** Pack of {@link #TURNING_LIGHTS_ON_JSON}. */
  public static final SenmlPack TURNING_LIGHTS_ON =
      SenmlPack.of(
          SenmlRecord.builder()
              .baseTime(1.320078429e9)
              .baseUnit("/")
              .name("2001:db8::3")
              .value(1)
              .build(),
          SenmlRecord.builder().name("2001:db8::4").value(1).build());

  /** JSON example of RFC 8428, Section 5.1.7. */
  public static final String TURNING_LIGHTS_OFF_JSON =
      """
      [
        {"bt":1.320078429e+09,"bu":"/","n":"2001:db8::3","v":0.5},
        {"n":"2001:db8::4","v":0.5},
        {"n":"2001:db8::3","t":0.1,"v":0},
        {"n":"2001:db8::4","t":0.1,"v":0}
      ]
      """;

  /** Pack of {@link #TURNING_LIGHTS_OFF_JSON}. */
  public static final SenmlPack TURNING_LIGHTS_OFF =
      SenmlPack.of(
          SenmlRecord.builder()
              .baseTime(1.320078429e9)
              .baseUnit("/")
              .name("2001:db8::3")
              .value(0.5)
              .build(),
          SenmlRecord.builder().name("2001:db8::4").value(0.5).build(),
          SenmlRecord.builder().name("2001:db8::3").time(0.1).value(0).build(),
          SenmlRecord.builder().name("2001:db8::4").time(0.1).value(0).build());

  /**
   * Hex dump of the CBOR example of RFC 8428, Section 6, encoding {@link
   * #MULTIPLE_DATA_POINTS_WITH_TIME}, except that the last record has an explicit time of {@code
   * 0}.
   */
  public static final String MULTIPLE_DATA_POINTS_WITH_TIME_CBOR =
      """
      87 a7 21 78 1b 75 72 6e 3a 64 65 76 3a 6f 77 3a
      31 30 65 32 30 37 33 61 30 31 30 38 30 30 36 3a
      22 fb 41 d3 03 a1 5b 00 10 62 23 61 41 20 05 00
      67 76 6f 6c 74 61 67 65 01 61 56 02 fb 40 5e 06
      66 66 66 66 66 a3 00 67 63 75 72 72 65 6e 74 06
      24 02 fb 3f f3 33 33 33 33 33 33 a3 00 67 63 75
      72 72 65 6e 74 06 23 02 fb 3f f4 cc cc cc cc cc
      cd a3 00 67 63 75 72 72 65 6e 74 06 22 02 fb 3f
      f6 66 66 66 66 66 66 a3 00 67 63 75 72 72 65 6e
      74 06 21 02 f9 3e 00 a3 00 67 63 75 72 72 65 6e
      74 06 20 02 fb 3f f9 99 99 99 99 99 9a a3 00 67
      63 75 72 72 65 6e 74 06 00 02 fb 3f fb 33 33 33
      33 33 33
      """;

  /** Pack of {@link #MULTIPLE_DATA_POINTS_WITH_TIME_CBOR}. */
  public static final SenmlPack MULTIPLE_DATA_POINTS_WITH_TIME_CBOR_PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .baseName("urn:dev:ow:10e2073a0108006:")
              .baseTime(1.276020076001e9)
              .baseUnit("A")
              .baseVersion(5)
              .name("voltage")
              .unit("V")
              .value(120.1)
              .build(),
          SenmlRecord.builder().name("current").time(-5).value(1.2).build(),
          SenmlRecord.builder().name("current").time(-4).value(1.3).build(),
          SenmlRecord.builder().name("current").time(-3).value(1.4).build(),
          SenmlRecord.builder().name("current").time(-2).value(1.5).build(),
          SenmlRecord.builder().name("current").time(-1).value(1.6).build(),
          SenmlRecord.builder().name("current").time(0).value(1.7).build());

  private RfcExamples() {}
}
