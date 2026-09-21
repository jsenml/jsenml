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

package io.github.jsenml.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jsenml.RfcExamples;
import io.github.jsenml.SenmlCodecException;
import io.github.jsenml.SenmlExtensions;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Contract tests every {@link SenmlJsonCodec} backend must pass, so that all backends produce
 * identical SenML JSON and agree on what input is rejected.
 */
public abstract class AbstractSenmlJsonCodecTest {

  private static final String FULL_JSON =
      "[{\"bn\":\"urn:dev:ow:10e2073a01080063:\",\"bt\":1320067464,\"bu\":\"V\",\"bv\":1,"
          + "\"bs\":2,\"bver\":10,\"n\":\"voltage\",\"u\":\"A\",\"t\":-1.25,\"ut\":60,"
          + "\"v\":120.1,\"s\":3.5},"
          + "{\"n\":\"label\",\"vs\":\"kitchen <&> 'x'\"},"
          + "{\"n\":\"open\",\"vb\":false},"
          + "{\"n\":\"blob\",\"vd\":\"AQID\"}]";

  private static final SenmlPack FULL_PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .baseName("urn:dev:ow:10e2073a01080063:")
              .baseTime(1.320067464e9)
              .baseUnit("V")
              .baseValue(1.0)
              .baseSum(2.0)
              .baseVersion(10)
              .name("voltage")
              .unit("A")
              .value(120.1)
              .sum(3.5)
              .time(-1.25)
              .updateTime(60.0)
              .build(),
          SenmlRecord.builder().name("label").stringValue("kitchen <&> 'x'").build(),
          SenmlRecord.builder().name("open").booleanValue(false).build(),
          SenmlRecord.builder().name("blob").dataValue(new byte[] {1, 2, 3}).build());

  /**
   * Returns codec under test.
   *
   * @return codec under test
   */
  protected abstract SenmlJsonCodec codec();

  static Stream<Arguments> rfcExamples() {
    return Stream.of(
        rfcExample("5.1.1", RfcExamples.SINGLE_DATA_POINT_JSON, RfcExamples.SINGLE_DATA_POINT),
        rfcExample(
            "5.1.2", RfcExamples.MULTIPLE_DATA_POINTS_JSON, RfcExamples.MULTIPLE_DATA_POINTS),
        rfcExample(
            "5.1.2 with time",
            RfcExamples.MULTIPLE_DATA_POINTS_WITH_TIME_JSON,
            RfcExamples.MULTIPLE_DATA_POINTS_WITH_TIME),
        rfcExample(
            "5.1.3", RfcExamples.MULTIPLE_MEASUREMENTS_JSON, RfcExamples.MULTIPLE_MEASUREMENTS),
        rfcExample("5.1.4", RfcExamples.RESOLVED_DATA_JSON, RfcExamples.RESOLVED_DATA),
        rfcExample("5.1.5", RfcExamples.MULTIPLE_DATA_TYPES_JSON, RfcExamples.MULTIPLE_DATA_TYPES),
        rfcExample(
            "5.1.6", RfcExamples.COLLECTION_OF_RESOURCES_JSON, RfcExamples.COLLECTION_OF_RESOURCES),
        rfcExample("5.1.7", RfcExamples.SETTING_AN_ACTUATOR_JSON, RfcExamples.SETTING_AN_ACTUATOR),
        rfcExample(
            "5.1.7 lights on", RfcExamples.TURNING_LIGHTS_ON_JSON, RfcExamples.TURNING_LIGHTS_ON),
        rfcExample(
            "5.1.7 lights off",
            RfcExamples.TURNING_LIGHTS_OFF_JSON,
            RfcExamples.TURNING_LIGHTS_OFF));
  }

  /** RFC examples without numbers in exponent notation, which are written in plain notation. */
  static Stream<Arguments> rfcExamplesWithPlainNumbers() {
    return Stream.of(
        rfcExample("5.1.1", RfcExamples.SINGLE_DATA_POINT_JSON, RfcExamples.SINGLE_DATA_POINT),
        rfcExample(
            "5.1.2", RfcExamples.MULTIPLE_DATA_POINTS_JSON, RfcExamples.MULTIPLE_DATA_POINTS),
        rfcExample("5.1.5", RfcExamples.MULTIPLE_DATA_TYPES_JSON, RfcExamples.MULTIPLE_DATA_TYPES),
        rfcExample("5.1.7", RfcExamples.SETTING_AN_ACTUATOR_JSON, RfcExamples.SETTING_AN_ACTUATOR));
  }

  @ParameterizedTest
  @MethodSource("rfcExamples")
  void givenRfcExample_whenDecoding_thenReadsPackItRepresents(String json, SenmlPack pack) {
    assertThat(codec().decode(json)).isEqualTo(pack);
  }

  @ParameterizedTest
  @MethodSource("rfcExamplesWithPlainNumbers")
  void givenRfcExamplePack_whenEncoding_thenProducesRfcJsonWithoutWhitespace(
      String json, SenmlPack pack) {
    assertThat(codec().encode(pack)).isEqualTo(json.replaceAll("\\s*\\n\\s*", ""));
  }

  @Test
  void givenRfcMultipleMeasurements_whenDecodingAndResolving_thenReadsRfcResolvedData() {
    SenmlPack resolved = codec().decode(RfcExamples.MULTIPLE_MEASUREMENTS_JSON).resolve();

    assertThat(resolved).isEqualTo(codec().decode(RfcExamples.RESOLVED_DATA_JSON));
  }

  @Test
  void givenAllFields_whenEncoding_thenWritesFieldsInRfcOrderWithPlainNumbers() {
    assertThat(codec().encode(FULL_PACK)).isEqualTo(FULL_JSON);
  }

  @Test
  void givenAllFields_whenDecoding_thenReadsEveryField() {
    assertThat(codec().decode(FULL_JSON)).isEqualTo(FULL_PACK);
  }

  @Test
  void givenPack_whenEncodingAndDecoding_thenRoundTrips() {
    assertThat(codec().decode(codec().encode(FULL_PACK))).isEqualTo(FULL_PACK);
  }

  @Test
  void givenEmptyPack_whenEncodingAndDecoding_thenRoundTrips() {
    assertThat(codec().encode(SenmlPack.empty())).isEqualTo("[]");
    assertThat(codec().decode("[]")).isEqualTo(SenmlPack.empty());
  }

  @Test
  void givenWriterAndReader_whenEncodingAndDecoding_thenRoundTrips() {
    StringWriter writer = new StringWriter();

    codec().encode(FULL_PACK, writer);

    assertThat(writer.toString()).isEqualTo(FULL_JSON);
    assertThat(codec().decode(new StringReader(writer.toString()))).isEqualTo(FULL_PACK);
  }

  @Test
  void givenBytes_whenEncodingAndDecoding_thenUsesUtf8() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("room").stringValue("żółć").build());

    byte[] bytes = codec().encodeToBytes(pack);

    assertThat(new String(bytes, StandardCharsets.UTF_8))
        .isEqualTo("[{\"n\":\"room\",\"vs\":\"żółć\"}]");
    assertThat(codec().decode(bytes)).isEqualTo(pack);
  }

  @Test
  void givenIntegerAndExponentNumbers_whenDecoding_thenReadsAsDouble() {
    SenmlPack pack = codec().decode("[{\"n\":\"a\",\"v\":21,\"t\":1.320067464E9}]");

    assertThat(pack.getRecords().get(0).getValue()).isEqualTo(21.0);
    assertThat(pack.getRecords().get(0).getTime()).isEqualTo(1.320067464e9);
  }

  @Test
  void givenDecimalBaseValue_whenDecodingResolvingAndEncoding_thenWritesDecimalSum() {
    SenmlPack pack = codec().decode("[{\"bn\":\"a\",\"bv\":0.1,\"v\":0.2,\"t\":1.5e9}]");

    assertThat(codec().encode(pack.resolve()))
        .isEqualTo("[{\"n\":\"a\",\"t\":1500000000,\"v\":0.3}]");
  }

  @Test
  void givenUnknownOptionalLabel_whenDecoding_thenIgnoresIt() {
    SenmlPack pack = codec().decode("[{\"n\":\"a\",\"foo\":{\"x\":[1,2]},\"v\":1}]");

    assertThat(pack).isEqualTo(SenmlPack.of(SenmlRecord.builder().name("a").value(1.0).build()));
  }

  @Test
  void givenScalarMandatoryField_whenDecoding_thenKeepsItAsExtensionForResolveToCheck() {
    SenmlPack pack = codec().decode("[{\"n\":\"a\",\"v\":1,\"foo_\":1}]");

    assertThat(pack.getFirst().getExtensions().getNumber("foo_")).isEqualTo(1.0);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "[{\"n\":\"a\",\"v\":1,\"foo_\":{\"x\":1}}]",
        "[{\"n\":\"a\",\"v\":1,\"foo_\":[1]}]",
        "[{\"n\":\"a\",\"v\":1,\"foo_\":null}]",
        "[{\"n\":\"a\",\"v\":1,\"a b_\":1}]"
      })
  void givenMandatoryFieldThatCannotBeKept_whenDecoding_thenRejectsPack(String json) {
    assertThatThrownBy(() -> codec().decode(json))
        .isInstanceOf(SenmlCodecException.class)
        .hasMessageContaining("_");
  }

  static Stream<String> invalidJson() {
    return Stream.of(
        "[{\"n\":\"a\",\"vd\":\"!!!\"}]",
        "[{\"n\":1}]",
        "[{\"v\":\"1\"}]",
        "[{\"vb\":\"true\"}]",
        "[{\"vs\":true}]",
        "[{\"bver\":10.5}]",
        "[{\"t\":null}]",
        "[1]",
        "[[]]",
        "{\"n\":\"a\"}",
        "\"a\"",
        "null",
        "",
        "[{\"n\":\"a\"}",
        "[]x",
        "[{\"v\":1e400}]");
  }

  @ParameterizedTest
  @MethodSource("invalidJson")
  void givenInvalidInput_whenDecodingString_thenThrowsCodecException(String json) {
    assertThatThrownBy(() -> codec().decode(json)).isInstanceOf(SenmlCodecException.class);
  }

  @ParameterizedTest
  @MethodSource("invalidJson")
  void givenInvalidInput_whenDecodingBytes_thenThrowsCodecException(String json) {
    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(() -> codec().decode(bytes)).isInstanceOf(SenmlCodecException.class);
  }

  @ParameterizedTest
  @MethodSource("invalidJson")
  void givenInvalidInput_whenDecodingReader_thenThrowsCodecException(String json) {
    assertThatThrownBy(() -> codec().decode(new StringReader(json)))
        .isInstanceOf(SenmlCodecException.class);
  }

  @Test
  void givenExtensions_whenEncoding_thenWritesThemAfterRegisteredFields() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .name("a")
                .value(1.0)
                .extension("foo", "bar")
                .extension("accuracy", 0.1)
                .extension("ok", true)
                .extension("raw", new byte[] {1, 2, 3})
                .build());

    assertThat(codec().encode(pack))
        .isEqualTo(
            "[{\"n\":\"a\",\"v\":1,\"foo\":\"bar\",\"accuracy\":0.1,\"ok\":true,\"raw\":\"AQID\"}]");
  }

  @Test
  void givenScalarUnknownFields_whenDecoding_thenKeepsThemAsExtensions() {
    SenmlPack pack =
        codec().decode("[{\"n\":\"a\",\"foo\":\"bar\",\"accuracy\":0.1,\"ok\":true,\"v\":1}]");

    SenmlExtensions extensions = pack.getFirst().getExtensions();
    assertThat(extensions.names()).containsExactly("foo", "accuracy", "ok");
    assertThat(extensions.getString("foo")).isEqualTo("bar");
    assertThat(extensions.getNumber("accuracy")).isEqualTo(0.1);
    assertThat(extensions.getBoolean("ok")).isTrue();
    assertThat(pack.getFirst().getValue()).isEqualTo(1.0);
  }

  @Test
  void givenExtensions_whenEncodingAndDecoding_thenRoundTrips() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .name("a")
                .value(1.0)
                .extension("foo", "bar")
                .extension("accuracy", 0.1)
                .extension("ok", false)
                .build());

    assertThat(codec().decode(codec().encode(pack))).isEqualTo(pack);
  }

  @Test
  void givenBytesExtension_whenEncodingAndDecoding_thenComesBackAsBase64UrlString() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder().name("a").extension("raw", new byte[] {1, 2, 3}).build());

    SenmlPack decoded = codec().decode(codec().encode(pack));

    assertThat(decoded.getFirst().getExtensions().getString("raw")).isEqualTo("AQID");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "[{\"n\":\"a\",\"foo\":null}]",
        "[{\"n\":\"a\",\"foo\":[1,2]}]",
        "[{\"n\":\"a\",\"foo\":{\"x\":1}}]",
        "[{\"n\":\"a\",\"-foo\":1}]",
        "[{\"n\":\"a\",\"a b\":1}]"
      })
  void givenNonScalarOrInvalidlyNamedOptionalField_whenDecoding_thenIgnoresIt(String json) {
    assertThat(codec().decode(json))
        .isEqualTo(SenmlPack.of(SenmlRecord.builder().name("a").build()));
  }

  private static Arguments rfcExample(String section, String json, SenmlPack pack) {
    return Arguments.of(Named.of("RFC 8428, Section " + section, json), pack);
  }
}
