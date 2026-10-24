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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jsenml.RfcExamples;
import io.github.jsenml.SenmlCodecException;
import io.github.jsenml.SenmlExtensions;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HexFormat;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Contract tests every {@link SenmlCborCodec} backend must pass, so that all backends produce
 * identical SenML CBOR and agree on what input is rejected.
 */
public abstract class AbstractSenmlCborCodecTest {

  private static final HexFormat HEX = HexFormat.of();

  private static final String RFC_EXAMPLE =
      RfcExamples.MULTIPLE_DATA_POINTS_WITH_TIME_CBOR.replaceAll("\\s", "");

  private static final SenmlPack RFC_PACK = RfcExamples.MULTIPLE_DATA_POINTS_WITH_TIME_CBOR_PACK;

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
          SenmlRecord.builder().name("label").stringValue("żółć").build(),
          SenmlRecord.builder().name("open").booleanValue(false).build(),
          SenmlRecord.builder().name("blob").dataValue(new byte[] {1, 2, 3}).build());

  /**
   * Returns codec under test.
   *
   * @return codec under test
   */
  protected abstract SenmlCborCodec codec();

  @Test
  void givenRfcExample_whenDecoding_thenReadsAllRecords() {
    assertThat(codec().decode(HEX.parseHex(RFC_EXAMPLE))).isEqualTo(RFC_PACK);
  }

  @Test
  void givenRfcExamplePack_whenEncoding_thenProducesRfcCbor() {
    String cbor = HEX.formatHex(codec().encode(RFC_PACK));

    // 1.5 is a half-precision float in the RFC; single precision is just as exact and allowed
    assertThat(cbor).isIn(RFC_EXAMPLE, RFC_EXAMPLE.replace("02f93e00", "02fa3fc00000"));
  }

  @Test
  void givenRfcExamplePack_whenEncodingAndDecoding_thenRoundTrips() {
    assertThat(codec().decode(codec().encode(RFC_PACK))).isEqualTo(RFC_PACK);
  }

  @Test
  void givenAllFields_whenEncodingAndDecoding_thenRoundTrips() {
    assertThat(codec().decode(codec().encode(FULL_PACK))).isEqualTo(FULL_PACK);
  }

  @Test
  void givenEmptyPack_whenEncodingAndDecoding_thenRoundTrips() {
    assertThat(HEX.formatHex(codec().encode(SenmlPack.empty()))).isEqualTo("80");
    assertThat(codec().decode(HEX.parseHex("80"))).isEqualTo(SenmlPack.empty());
  }

  @Test
  void givenRecord_whenEncoding_thenUsesIntegerKeysAndMostCompactExactNumbers() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder().name("a").unit("Cel").value(23.1).sum(1.5).time(1.5e9).build());

    assertThat(HEX.formatHex(codec().encode(pack)))
        .isEqualTo("81a5006161016343656c061a59682f0002fb403719999999999a05fa3fc00000");
  }

  @Test
  void givenBaseFields_whenEncoding_thenWritesThemFirstWithNegativeKeys() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .baseName("x")
                .baseVersion(10)
                .name("y")
                .booleanValue(true)
                .build());

    assertThat(HEX.formatHex(codec().encode(pack))).isEqualTo("81a4216178200a00617904f5");
  }

  @Test
  void givenDataValue_whenEncoding_thenWritesByteString() {
    SenmlPack pack =
        SenmlPack.of(SenmlRecord.builder().name("b").dataValue(new byte[] {1, 2, 3}).build());

    assertThat(HEX.formatHex(codec().encode(pack))).isEqualTo("81a20061620843010203");
  }

  @Test
  void givenNegativeIntegralAndLargeValues_whenEncoding_thenUsesIntegerAndDouble() {
    SenmlPack negative = SenmlPack.of(SenmlRecord.builder().name("c").value(-100.0).build());
    SenmlPack large = SenmlPack.of(SenmlRecord.builder().name("d").value(1e20).build());

    assertThat(HEX.formatHex(codec().encode(negative))).isEqualTo("81a2006163023863");
    assertThat(HEX.formatHex(codec().encode(large))).isEqualTo("81a200616402fb4415af1d78b58c40");
    assertThat(codec().decode(codec().encode(large))).isEqualTo(large);
  }

  @Test
  void givenIntegerValues_whenDecoding_thenReadsThemAsDoubles() {
    SenmlPack pack = codec().decode(HEX.parseHex("81a2006163023863"));

    assertThat(pack.getFirst().getValue()).isEqualTo(-100.0);
  }

  @Test
  void givenScalarTextLabels_whenDecoding_thenKeepsThemAsExtensions() {
    SenmlPack pack =
        codec()
            .decode(
                HEX.parseHex(
                    "81a5006161"
                        + "63666f6f"
                        + "63626172"
                        + "686163637572616379"
                        + "fb3fb999999999999a"
                        + "626f6b"
                        + "f5"
                        + "63726177"
                        + "43010203"));

    SenmlExtensions extensions = pack.getFirst().getExtensions();
    assertThat(extensions.names()).containsExactly("foo", "accuracy", "ok", "raw");
    assertThat(extensions.getString("foo")).isEqualTo("bar");
    assertThat(extensions.getNumber("accuracy")).isEqualTo(0.1);
    assertThat(extensions.getBoolean("ok")).isTrue();
    assertThat(extensions.getBytes("raw")).containsExactly(1, 2, 3);
  }

  @Test
  void givenScalarMandatoryTextLabel_whenDecoding_thenKeepsItAsExtensionForResolveToCheck() {
    SenmlPack pack = codec().decode(HEX.parseHex("81a2006161" + "64666f6f5f" + "01"));

    assertThat(pack.getFirst().getExtensions().getNumber("foo_")).isEqualTo(1.0);
  }

  @Test
  void givenNonScalarTextLabel_whenDecoding_thenIgnoresIt() {
    SenmlPack pack = codec().decode(HEX.parseHex("81a3006161" + "63666f6f" + "820102" + "0201"));

    assertThat(pack).isEqualTo(SenmlPack.of(SenmlRecord.builder().name("a").value(1).build()));
  }

  @Test
  void givenNonScalarMandatoryTextLabel_whenDecoding_thenRejectsPack() {
    assertThatThrownBy(() -> codec().decode(HEX.parseHex("81a2006161" + "64666f6f5f" + "80")))
        .isInstanceOf(SenmlCodecException.class)
        .hasMessageContaining("foo_");
  }

  @Test
  void givenExtensions_whenEncoding_thenWritesThemAsTextLabelsAfterRegisteredFields() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .name("a")
                .extension("foo", "bar")
                .extension("accuracy", 0.5)
                .extension("ok", true)
                .extension("raw", new byte[] {1, 2, 3})
                .build());

    assertThat(HEX.formatHex(codec().encode(pack)))
        .isEqualTo(
            "81a5006161"
                + "63666f6f63626172"
                + "686163637572616379fa3f000000"
                + "626f6bf5"
                + "6372617743010203");
  }

  @Test
  void givenExtensions_whenEncodingAndDecoding_thenRoundTrips() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .name("a")
                .value(1)
                .extension("foo", "bar")
                .extension("accuracy", 0.1)
                .extension("ok", false)
                .extension("raw", new byte[] {1, 2, 3})
                .build());

    assertThat(codec().decode(codec().encode(pack))).isEqualTo(pack);
  }

  @Test
  void givenUnknownIntegerLabel_whenDecoding_thenRejectsPack() {
    assertThatThrownBy(() -> codec().decode(HEX.parseHex("81a2006161" + "1863" + "01")))
        .isInstanceOf(SenmlCodecException.class)
        .hasMessageContaining("99");
  }

  @Test
  void givenOutputAndInputStreams_whenEncodingAndDecoding_thenRoundTripsWithoutClosingThem() {
    CloseTrackingOutputStream output = new CloseTrackingOutputStream();

    codec().encode(FULL_PACK, output);
    CloseTrackingInputStream input = new CloseTrackingInputStream(output.toByteArray());
    SenmlPack decoded = codec().decode(input);

    assertThat(output.toByteArray()).isEqualTo(codec().encode(FULL_PACK));
    assertThat(decoded).isEqualTo(FULL_PACK);
    assertThat(output.closed).isFalse();
    assertThat(input.closed).isFalse();
  }

  static Stream<String> invalidCbor() {
    return Stream.of(
        "",
        "f6",
        "a1006161",
        "81",
        "81a100",
        "8101",
        "81a1000161",
        "81a1000a",
        "81a1026131",
        "81a10401",
        "81a1086141",
        "81a120fa3fc00000",
        "8000",
        "81a10261",
        "ff");
  }

  @ParameterizedTest
  @MethodSource("invalidCbor")
  void givenInvalidInput_whenDecodingBytes_thenThrowsCodecException(String hex) {
    byte[] bytes = HEX.parseHex(hex);

    assertThatThrownBy(() -> codec().decode(bytes)).isInstanceOf(SenmlCodecException.class);
  }

  @ParameterizedTest
  @MethodSource("invalidCbor")
  void givenInvalidInput_whenDecodingStream_thenThrowsCodecException(String hex) {
    InputStream input = new ByteArrayInputStream(HEX.parseHex(hex));

    assertThatThrownBy(() -> codec().decode(input)).isInstanceOf(SenmlCodecException.class);
  }

  private static final class CloseTrackingOutputStream extends ByteArrayOutputStream {

    private boolean closed;

    @Override
    public void close() {
      closed = true;
    }
  }

  private static final class CloseTrackingInputStream extends ByteArrayInputStream {

    private boolean closed;

    CloseTrackingInputStream(byte[] bytes) {
      super(bytes);
    }

    @Override
    public void close() {
      closed = true;
    }
  }
}
