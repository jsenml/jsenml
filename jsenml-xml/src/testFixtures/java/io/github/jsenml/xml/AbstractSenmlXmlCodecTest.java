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

package io.github.jsenml.xml;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Contract tests every {@link SenmlXmlCodec} backend must pass, so that all backends produce
 * identical SenML XML and agree on what input is rejected.
 */
public abstract class AbstractSenmlXmlCodecTest {

  private static final String FULL_XML =
      "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\">"
          + "<senml bn=\"urn:dev:ow:10e2073a01080063:\" bt=\"1320067464\" bu=\"V\" bv=\"1\""
          + " bs=\"2\" bver=\"10\" n=\"voltage\" u=\"A\" t=\"-1.25\" ut=\"60\" v=\"120.1\""
          + " s=\"3.5\"/>"
          + "<senml n=\"label\" vs=\"kitchen &lt;&amp;> 'x' &quot;y&quot;\"/>"
          + "<senml n=\"open\" vb=\"false\"/>"
          + "<senml n=\"blob\" vd=\"AQID\"/>"
          + "</sensml>";

  private static final SenmlPack FULL_PACK =
      SenmlPack.of(
          SenmlRecord.builder()
              .baseName("urn:dev:ow:10e2073a01080063:")
              .baseTime(1.320067464e9)
              .baseUnit("V")
              .baseValue(1)
              .baseSum(2)
              .baseVersion(10)
              .name("voltage")
              .unit("A")
              .value(120.1)
              .sum(3.5)
              .time(-1.25)
              .updateTime(60)
              .build(),
          SenmlRecord.builder().name("label").stringValue("kitchen <&> 'x' \"y\"").build(),
          SenmlRecord.builder().name("open").booleanValue(false).build(),
          SenmlRecord.builder().name("blob").dataValue(new byte[] {1, 2, 3}).build());

  /**
   * Returns codec under test.
   *
   * @return codec under test
   */
  protected abstract SenmlXmlCodec codec();

  @Test
  void givenRfcExample_whenDecoding_thenReadsPackItRepresents() {
    assertThat(codec().decode(RfcExamples.MULTIPLE_DATA_POINTS_WITH_TIME_XML))
        .isEqualTo(RfcExamples.MULTIPLE_DATA_POINTS_WITH_TIME);
  }

  @Test
  void givenRfcExamplePack_whenEncodingAndDecoding_thenRoundTrips() {
    SenmlPack pack = RfcExamples.MULTIPLE_DATA_POINTS_WITH_TIME;

    assertThat(codec().decode(codec().encode(pack))).isEqualTo(pack);
  }

  @Test
  void givenAllFields_whenEncoding_thenWritesAttributesInRfcOrderWithPlainNumbers() {
    assertThat(codec().encode(FULL_PACK)).isEqualTo(FULL_XML);
  }

  @Test
  void givenAllFields_whenDecoding_thenReadsEveryField() {
    assertThat(codec().decode(FULL_XML)).isEqualTo(FULL_PACK);
  }

  @Test
  void givenEmptyPack_whenEncodingAndDecoding_thenRoundTrips() {
    String xml = codec().encode(SenmlPack.empty());

    assertThat(xml).isEqualTo("<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"/>");
    assertThat(codec().decode(xml)).isEqualTo(SenmlPack.empty());
    assertThat(codec().decode("<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"></sensml>"))
        .isEqualTo(SenmlPack.empty());
  }

  @Test
  void givenRecordWithoutFields_whenEncodingAndDecoding_thenRoundTrips() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().build());

    assertThat(codec().decode(codec().encode(pack))).isEqualTo(pack);
  }

  @Test
  void givenWriterAndReader_whenEncodingAndDecoding_thenRoundTrips() {
    StringWriter writer = new StringWriter();

    codec().encode(FULL_PACK, writer);

    assertThat(writer.toString()).isEqualTo(FULL_XML);
    assertThat(codec().decode(new StringReader(writer.toString()))).isEqualTo(FULL_PACK);
  }

  @Test
  void givenBytes_whenEncodingAndDecoding_thenUsesUtf8() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("room").stringValue("żółć").build());

    byte[] bytes = codec().encodeToBytes(pack);

    assertThat(new String(bytes, StandardCharsets.UTF_8))
        .isEqualTo(
            "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"><senml n=\"room\" vs=\"żółć\"/></sensml>");
    assertThat(codec().decode(bytes)).isEqualTo(pack);
  }

  @Test
  void givenXmlDeclarationAndWhitespace_whenDecoding_thenIgnoresThem() {
    SenmlPack pack =
        codec()
            .decode(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\">\n"
                    + "  <senml n=\"a\" v=\"1\" vb=\"1\"></senml>\n"
                    + "</sensml>\n");

    assertThat(pack)
        .isEqualTo(
            SenmlPack.of(SenmlRecord.builder().name("a").value(1).booleanValue(true).build()));
  }

  @Test
  void givenExtensions_whenEncoding_thenWritesThemAsAttributesAfterRegisteredFields() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .name("a")
                .extension("foo", "bar")
                .extension("accuracy", 0.1)
                .extension("ok", true)
                .extension("raw", new byte[] {1, 2, 3})
                .build());

    assertThat(codec().encode(pack))
        .isEqualTo(
            "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\">"
                + "<senml n=\"a\" foo=\"bar\" accuracy=\"0.1\" ok=\"true\" raw=\"AQID\"/>"
                + "</sensml>");
  }

  @Test
  void givenUnknownAttributes_whenDecoding_thenKeepsThemAsStringExtensions() {
    SenmlPack pack =
        codec()
            .decode(
                "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\">"
                    + "<senml n=\"a\" foo=\"bar\" accuracy=\"0.1\" foo_=\"1\"/>"
                    + "</sensml>");

    SenmlExtensions extensions = pack.getFirst().getExtensions();
    assertThat(extensions.names()).containsExactly("foo", "accuracy", "foo_");
    assertThat(extensions.getString("foo")).isEqualTo("bar");
    assertThat(extensions.getString("accuracy")).isEqualTo("0.1");
    assertThat(extensions.getString("foo_")).isEqualTo("1");
  }

  @Test
  void givenChildElementsOfRecord_whenDecoding_thenIgnoresThem() {
    SenmlPack pack =
        codec()
            .decode(
                "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\">"
                    + "<senml n=\"a\" v=\"1\"><foo bar=\"baz\"/>text</senml>"
                    + "</sensml>");

    assertThat(pack).isEqualTo(SenmlPack.of(SenmlRecord.builder().name("a").value(1).build()));
  }

  static Stream<String> invalidXml() {
    return Stream.of(
        "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"><senml n=\"a\" vd=\"!!!\"/></sensml>",
        "",
        "not xml",
        "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\">",
        "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"><senml v=\"abc\"/></sensml>",
        "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"><senml v=\"NaN\"/></sensml>",
        "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"><senml v=\"INF\"/></sensml>",
        "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"><senml bver=\"1.5\"/></sensml>",
        "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"><senml vb=\"yes\"/></sensml>",
        "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"><senml n=\"a\" n=\"b\"/></sensml>");
  }

  @ParameterizedTest
  @MethodSource("invalidXml")
  void givenInvalidInput_whenDecodingString_thenThrowsCodecException(String xml) {
    assertThatThrownBy(() -> codec().decode(xml)).isInstanceOf(SenmlCodecException.class);
  }

  @ParameterizedTest
  @MethodSource("invalidXml")
  void givenInvalidInput_whenDecodingBytes_thenThrowsCodecException(String xml) {
    byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(() -> codec().decode(bytes)).isInstanceOf(SenmlCodecException.class);
  }

  @Test
  void givenExternalEntity_whenDecoding_thenDoesNotResolveIt() {
    String xml =
        "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE sensml [<!ENTITY secret SYSTEM \"file:///etc/hostname\">]>"
            + "<sensml xmlns=\"urn:ietf:params:xml:ns:senml\"><senml n=\"&secret;\"/></sensml>";

    assertThatThrownBy(() -> codec().decode(xml)).isInstanceOf(SenmlCodecException.class);
  }
}
