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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SenmlRecordTest {

  @Test
  void givenAllFields_whenBuilding_thenExposesThem() {
    SenmlRecord record =
        SenmlRecord.builder()
            .baseName("bn")
            .baseTime(1.0)
            .baseUnit("bu")
            .baseValue(2.0)
            .baseSum(3.0)
            .baseVersion(10)
            .name("n")
            .unit("u")
            .value(4.0)
            .stringValue("vs")
            .booleanValue(true)
            .dataValue("AQID")
            .sum(5.0)
            .time(6.0)
            .updateTime(7.0)
            .build();

    assertThat(record.getBaseName()).isEqualTo("bn");
    assertThat(record.getBaseTime()).isEqualTo(1.0);
    assertThat(record.getBaseUnit()).isEqualTo("bu");
    assertThat(record.getBaseValue()).isEqualTo(2.0);
    assertThat(record.getBaseSum()).isEqualTo(3.0);
    assertThat(record.getBaseVersion()).isEqualTo(10);
    assertThat(record.getName()).isEqualTo("n");
    assertThat(record.getUnit()).isEqualTo("u");
    assertThat(record.getValue()).isEqualTo(4.0);
    assertThat(record.getStringValue()).isEqualTo("vs");
    assertThat(record.getBooleanValue()).isTrue();
    assertThat(record.getDataValue()).containsExactly(1, 2, 3);
    assertThat(record.getSum()).isEqualTo(5.0);
    assertThat(record.getTime()).isEqualTo(6.0);
    assertThat(record.getUpdateTime()).isEqualTo(7.0);
    assertThat(record.hasBaseFields()).isTrue();
  }

  @Test
  void givenEmptyBuilder_whenBuilding_thenAllFieldsAreNull() {
    SenmlRecord record = SenmlRecord.builder().build();

    assertThat(record.getName()).isNull();
    assertThat(record.getValue()).isNull();
    assertThat(record.getDataValue()).isNull();
    assertThat(record.hasBaseFields()).isFalse();
    assertThat(record).hasToString("SenmlRecord[]");
  }

  @Test
  void givenRecord_whenToBuilder_thenCopiesAndDoesNotMutateOriginal() {
    SenmlRecord original = SenmlRecord.builder().name("a").value(1.0).build();

    SenmlRecord copy = original.toBuilder().value(2.0).build();

    assertThat(original.getValue()).isEqualTo(1.0);
    assertThat(copy).isEqualTo(SenmlRecord.builder().name("a").value(2.0).build());
  }

  @Test
  void givenEqualRecords_whenComparing_thenEqualAndSameHashCode() {
    SenmlRecord a = SenmlRecord.builder().name("a").unit("Cel").value(23.1).build();
    SenmlRecord b = SenmlRecord.builder().name("a").unit("Cel").value(23.1).build();

    assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    assertThat(a).isNotEqualTo(b.toBuilder().unit("K").build());
  }

  @Test
  void givenBase64UrlText_whenSettingDataValue_thenDecodesItWithOrWithoutPadding() {
    SenmlRecord record = SenmlRecord.builder().dataValue("-_8").build();

    assertThat(record.getDataValue()).containsExactly((byte) 0xfb, (byte) 0xff);
    assertThat(record.toString()).contains("vd=-_8");
    assertThat(SenmlRecord.builder().dataValue("AQI").build())
        .isEqualTo(SenmlRecord.builder().dataValue("AQI=").build())
        .isEqualTo(SenmlRecord.builder().dataValue(new byte[] {1, 2}).build());
  }

  @ParameterizedTest
  @ValueSource(strings = {"!!!", "+/8", "AQI==="})
  void givenInvalidBase64UrlText_whenSettingDataValue_thenThrows(String text) {
    assertThatThrownBy(() -> SenmlRecord.builder().dataValue(text))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'vd'");
  }

  @Test
  void givenDataValue_whenModifyingArrays_thenRecordIsUnchanged() {
    byte[] input = {1, 2, 3};
    SenmlRecord record = SenmlRecord.builder().dataValue(input).build();

    input[0] = 9;
    byte[] output = Objects.requireNonNull(record.getDataValue());
    output[1] = 9;

    assertThat(record.getDataValue()).containsExactly(1, 2, 3);
  }

  @Test
  void givenRecord_whenToString_thenListsPresentFieldsByLabel() {
    SenmlRecord record = SenmlRecord.builder().name("a").unit("Cel").value(23.1).build();

    assertThat(record).hasToString("SenmlRecord[n=a, u=Cel, v=23.1]");
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void givenNonFiniteNumber_whenSettingNumericField_thenThrows(double number) {
    assertThatThrownBy(() -> SenmlRecord.builder().value(number))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'v'");
    assertThatThrownBy(() -> SenmlRecord.builder().time(number))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'t'");
  }

  @Test
  void givenExtensions_whenBuilding_thenExposesThemAndIncludesThemInEqualityAndToString() {
    SenmlRecord record =
        SenmlRecord.builder()
            .name("a")
            .value(1.0)
            .extension("foo", "bar")
            .extension("accuracy", 0.1)
            .extension("ok", true)
            .extension("raw", new byte[] {1, 2, 3})
            .build();

    assertThat(record.getExtensions().names()).containsExactly("foo", "accuracy", "ok", "raw");
    assertThat(record.getExtensions().getNumber("accuracy")).isEqualTo(0.1);
    assertThat(record).isNotEqualTo(record.toBuilder().removeExtension("ok").build());
    assertThat(record)
        .isEqualTo(record.toBuilder().build())
        .hasSameHashCodeAs(record.toBuilder().build());
    assertThat(record)
        .hasToString("SenmlRecord[n=a, v=1.0, foo=bar, accuracy=0.1, ok=true, raw=AQID]");
  }

  @Test
  void givenNoExtensions_whenBuilding_thenExtensionsAreEmpty() {
    assertThat(SenmlRecord.builder().name("a").build().getExtensions())
        .isSameAs(SenmlExtensions.empty());
  }

  @Test
  void givenExtensionsObject_whenSettingOnBuilder_thenReplacesExistingOnes() {
    SenmlExtensions extensions = SenmlExtensions.builder().put("x", 1.0).build();

    SenmlRecord record =
        SenmlRecord.builder().name("a").extension("foo", "bar").extensions(extensions).build();

    assertThat(record.getExtensions()).isEqualTo(extensions);
  }

  @Test
  void givenInvalidExtensionName_whenSettingOnBuilder_thenThrows() {
    assertThatThrownBy(() -> SenmlRecord.builder().extension("v", 1.0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void givenBuilder_whenToString_thenListsCurrentStateLikeRecord() {
    SenmlRecordBuilder builder =
        SenmlRecord.builder().name("a").unit("Cel").value(23.1).extension("foo", "bar");

    assertThat(builder).hasToString("SenmlRecordBuilder[n=a, u=Cel, v=23.1, foo=bar]");
    assertThat(SenmlRecord.builder()).hasToString("SenmlRecordBuilder[]");
  }

  @Test
  void givenIntegralNumbers_whenSettingNumericFields_thenConvertsToDouble() {
    SenmlRecord record =
        SenmlRecord.builder()
            .baseTime((short) 1)
            .baseValue(2)
            .baseSum(3L)
            .value((short) 4)
            .sum(5)
            .time(1_700_000_000_000L)
            .updateTime(7)
            .extension("xs", (short) 8)
            .extension("xi", 9)
            .extension("xl", 10L)
            .build();

    assertThat(record.getBaseTime()).isEqualTo(1.0);
    assertThat(record.getBaseValue()).isEqualTo(2.0);
    assertThat(record.getBaseSum()).isEqualTo(3.0);
    assertThat(record.getValue()).isEqualTo(4.0);
    assertThat(record.getSum()).isEqualTo(5.0);
    assertThat(record.getTime()).isEqualTo(1.7e12);
    assertThat(record.getUpdateTime()).isEqualTo(7.0);
    assertThat(record.getExtensions().getNumber("xs")).isEqualTo(8.0);
    assertThat(record.getExtensions().getNumber("xi")).isEqualTo(9.0);
    assertThat(record.getExtensions().getNumber("xl")).isEqualTo(10.0);
  }

  @Test
  void givenFloat_whenSettingNumericFields_thenConvertsThroughDecimalRepresentation() {
    SenmlRecord record =
        SenmlRecord.builder()
            .baseTime(1.1f)
            .baseValue(2.2f)
            .baseSum(3.3f)
            .value(23.1f)
            .sum(5.5f)
            .time(6.6f)
            .updateTime(7.7f)
            .extension("accuracy", 0.1f)
            .build();

    assertThat(record.getBaseTime()).isEqualTo(1.1);
    assertThat(record.getBaseValue()).isEqualTo(2.2);
    assertThat(record.getBaseSum()).isEqualTo(3.3);
    assertThat(record.getValue()).isEqualTo(23.1);
    assertThat(record.getSum()).isEqualTo(5.5);
    assertThat(record.getTime()).isEqualTo(6.6);
    assertThat(record.getUpdateTime()).isEqualTo(7.7);
    assertThat(record.getExtensions().getNumber("accuracy")).isEqualTo(0.1);
  }

  @ParameterizedTest
  @ValueSource(floats = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
  void givenNonFiniteFloat_whenSettingNumericField_thenThrows(float number) {
    assertThatThrownBy(() -> SenmlRecord.builder().value(number))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SenmlRecord.builder().extension("foo", number))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void givenNullBoxedNumber_whenSettingNumericField_thenClearsIt() {
    Integer missing = null;

    SenmlRecord record = SenmlRecord.builder().value(1).value(missing).value(null).build();

    assertThat(record.getValue()).isNull();
  }

  @Test
  void givenOtherNumberTypes_whenSettingNumericFields_thenConvertsWithDoubleValue() {
    SenmlRecord record =
        SenmlRecord.builder()
            .value(new BigDecimal("12.5"))
            .sum((byte) 3)
            .extension("xb", new BigInteger("42"))
            .build();

    assertThat(record.getValue()).isEqualTo(12.5);
    assertThat(record.getSum()).isEqualTo(3.0);
    assertThat(record.getExtensions().getNumber("xb")).isEqualTo(42.0);
  }

  @Test
  void givenNumberBeyondDoubleRange_whenSettingNumericField_thenThrows() {
    BigDecimal huge = new BigDecimal("1e400");

    assertThatThrownBy(() -> SenmlRecord.builder().value(huge))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SenmlRecord.builder().extension("foo", huge))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
