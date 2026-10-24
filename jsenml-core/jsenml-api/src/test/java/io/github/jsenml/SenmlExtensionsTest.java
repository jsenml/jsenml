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

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SenmlExtensionsTest {

  @Test
  void givenValuesOfEachType_whenBuilding_thenExposesThemThroughTypedGetters() {
    SenmlExtensions extensions =
        SenmlExtensions.builder()
            .put("foo", "bar")
            .put("accuracy", 0.1)
            .put("ok", true)
            .put("raw", new byte[] {1, 2, 3})
            .build();

    assertThat(extensions.getString("foo")).isEqualTo("bar");
    assertThat(extensions.getNumber("accuracy")).isEqualTo(0.1);
    assertThat(extensions.getBoolean("ok")).isTrue();
    assertThat(extensions.getBytes("raw")).containsExactly(1, 2, 3);
    assertThat(extensions.get("foo")).isEqualTo("bar");
    assertThat(extensions.names()).containsExactly("foo", "accuracy", "ok", "raw");
    assertThat(extensions.size()).isEqualTo(4);
    assertThat(extensions.contains("ok")).isTrue();
    assertThat(extensions.contains("missing")).isFalse();
  }

  @Test
  void givenMissingExtension_whenGettingIt_thenReturnsNull() {
    SenmlExtensions extensions = SenmlExtensions.builder().put("foo", "bar").build();

    assertThat(extensions.get("missing")).isNull();
    assertThat(extensions.getString("missing")).isNull();
    assertThat(extensions.getNumber("missing")).isNull();
    assertThat(extensions.getBoolean("missing")).isNull();
    assertThat(extensions.getBytes("missing")).isNull();
  }

  @Test
  void givenExtensionOfOtherType_whenGettingWithTypedGetter_thenThrows() {
    SenmlExtensions extensions = SenmlExtensions.builder().put("foo", "bar").build();

    assertThatThrownBy(() -> extensions.getNumber("foo"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Extension 'foo' is String, not Double");
  }

  @Test
  void givenBytes_whenBuildingAndReading_thenCopiesThemDefensively() {
    byte[] input = {1, 2, 3};
    SenmlExtensions extensions = SenmlExtensions.builder().put("raw", input).build();

    input[0] = 9;
    byte[] output = Objects.requireNonNull(extensions.getBytes("raw"));
    output[1] = 9;

    assertThat(extensions.getBytes("raw")).containsExactly(1, 2, 3);
    assertThat((byte[]) extensions.get("raw")).containsExactly(1, 2, 3);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"n", "v", "bn", "bver", "vd", "ut", "", "-foo", "_foo", "a b", "a/b", "żółć"})
  void givenInvalidName_whenPutting_thenThrows(String name) {
    assertThat(SenmlExtensions.isValidName(name)).isFalse();
    assertThatThrownBy(() -> SenmlExtensions.builder().put(name, "x"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"foo", "foo_", "bfoo", "a-b_c:d.e", "0", "X1"})
  void givenValidName_whenPutting_thenAccepts(String name) {
    assertThat(SenmlExtensions.isValidName(name)).isTrue();
    assertThat(SenmlExtensions.builder().put(name, "x").build().contains(name)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void givenNonFiniteNumber_whenPutting_thenThrows(double number) {
    assertThatThrownBy(() -> SenmlExtensions.builder().put("foo", number))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void givenSameExtensionsInOtherOrder_whenComparing_thenEqualAndSameHashCode() {
    SenmlExtensions a =
        SenmlExtensions.builder().put("x", 1.0).put("raw", new byte[] {1, 2}).build();
    SenmlExtensions b =
        SenmlExtensions.builder().put("raw", new byte[] {1, 2}).put("x", 1.0).build();

    assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    assertThat(a).isNotEqualTo(a.toBuilder().put("raw", new byte[] {1, 3}).build());
    assertThat(a).isNotEqualTo(a.toBuilder().remove("x").build());
  }

  @Test
  void givenExtensions_whenToBuilderAndModifying_thenOriginalIsUnchanged() {
    SenmlExtensions original = SenmlExtensions.builder().put("foo", "bar").build();

    SenmlExtensions modified = original.toBuilder().put("foo", "baz").put("ok", false).build();

    assertThat(original.getString("foo")).isEqualTo("bar");
    assertThat(modified.getString("foo")).isEqualTo("baz");
    assertThat(modified.names()).containsExactly("foo", "ok");
  }

  @Test
  void givenNoExtensions_whenBuilding_thenReturnsEmptySingleton() {
    assertThat(SenmlExtensions.builder().build()).isSameAs(SenmlExtensions.empty());
    assertThat(SenmlExtensions.empty().isEmpty()).isTrue();
    assertThat(SenmlExtensions.empty().names()).isEmpty();
  }

  @Test
  void givenExtensions_whenModifyingNames_thenThrows() {
    SenmlExtensions extensions = SenmlExtensions.builder().put("foo", "bar").build();

    assertThatThrownBy(() -> extensions.names().add("x"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void givenExtensions_whenToString_thenListsThemWithBytesAsBase64Url() {
    SenmlExtensions extensions =
        SenmlExtensions.builder()
            .put("foo", "bar")
            .put("accuracy", 0.1)
            .put("raw", new byte[] {(byte) 0xfb, (byte) 0xff})
            .build();

    assertThat(extensions).hasToString("SenmlExtensions[foo=bar, accuracy=0.1, raw=-_8]");
  }

  @Test
  void givenBuilder_whenToString_thenListsCurrentStateLikeExtensions() {
    SenmlExtensionsBuilder builder =
        SenmlExtensions.builder().put("foo", "bar").put("raw", new byte[] {1, 2, 3});

    assertThat(builder).hasToString("SenmlExtensionsBuilder[foo=bar, raw=AQID]");
    assertThat(SenmlExtensions.builder()).hasToString("SenmlExtensionsBuilder[]");
  }

  @Test
  void givenOtherNumericTypes_whenPutting_thenConvertsToDouble() {
    SenmlExtensions extensions =
        SenmlExtensions.builder()
            .put("xs", (short) 1)
            .put("xi", 2)
            .put("xl", 3L)
            .put("xf", 0.1f)
            .build();

    assertThat(extensions.getNumber("xs")).isEqualTo(1.0);
    assertThat(extensions.getNumber("xi")).isEqualTo(2.0);
    assertThat(extensions.getNumber("xl")).isEqualTo(3.0);
    assertThat(extensions.getNumber("xf")).isEqualTo(0.1);
  }
}
