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

package io.github.jsenml.kotlin

import io.github.jsenml.SenmlExtensions
import io.github.jsenml.SenmlRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SenmlRecordDslTest {

  @Test
  fun `given block when building record in pack then all fields are applied`() {
    val record = buildRecord {
      baseName("bn")
      baseTime(1.0)
      baseUnit("bu")
      baseValue(2.0)
      baseSum(3.0)
      baseVersion(10)
      name("n")
      unit("u")
      value(4.0)
      sum(5.0)
      time(6.0)
      updateTime(7.0)
      extension("foo", "bar")
    }

    assertThat(record)
        .isEqualTo(
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
                .sum(5.0)
                .time(6.0)
                .updateTime(7.0)
                .extension("foo", "bar")
                .build(),
        )
  }

  @Test
  fun `given empty block when building record in pack then record is empty`() {
    assertThat(buildRecord {}).isEqualTo(SenmlRecord.builder().build())
  }

  @Test
  fun `given value types when building record in pack then each value field is set`() {
    assertThat(buildRecord { stringValue("x") }.stringValue).isEqualTo("x")
    assertThat(buildRecord { booleanValue(true) }.booleanValue).isTrue()
    assertThat(buildRecord { dataValue("AQID") }.dataValue).containsExactly(1, 2, 3)
    assertThat(buildRecord { dataValue(byteArrayOf(1, 2, 3)) }.dataValue).containsExactly(1, 2, 3)
  }

  @Test
  fun `given nested extensions block when building record in pack then replaces extensions`() {
    val record = buildRecord {
      name("temp")
      extension("old", "x")
      extensions {
        put("accuracy", 0.1)
        put("calibrated", true)
      }
    }

    assertThat(record.extensions.names()).containsExactly("accuracy", "calibrated")
    assertThat(record.extensions.getNumber("accuracy")).isEqualTo(0.1)
  }

  @Test
  fun `given extensions object and removal when building record in pack then applies them in order`() {
    val record = buildRecord {
      extensions(SenmlExtensions.builder().put("a", 1.0).put("b", 2.0).build())
      removeExtension("a")
      extension("raw", byteArrayOf(1))
      extension("ok", false)
    }

    assertThat(record.extensions.names()).containsExactly("b", "raw", "ok")
  }

  @Test
  fun `given record when copying then untouched fields are kept`() {
    val record = buildRecord {
      name("temp")
      unit("Cel")
      value(21.5)
    }

    val copy = record.copy { value(22.0) }

    assertThat(copy)
        .isEqualTo(
            buildRecord {
              name("temp")
              unit("Cel")
              value(22.0)
            },
        )
    assertThat(record.value).isEqualTo(21.5)
  }

  @Test
  fun `given invalid value when building record in pack then throws`() {
    assertThatThrownBy { buildRecord { value(Double.NaN) } }
        .isInstanceOf(IllegalArgumentException::class.java)
    assertThatThrownBy { buildRecord { extension("v", 1.0) } }
        .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `given other numeric types when building record in pack then converts to double`() {
    val record = buildRecord {
      baseTime(1.toShort())
      baseValue(2)
      baseSum(3L)
      value(23.1f)
      sum(5)
      time(6L)
      updateTime(7.7f)
      extension("xi", 8)
      extension("xf", 0.1f)
    }

    assertThat(record.baseTime).isEqualTo(1.0)
    assertThat(record.baseValue).isEqualTo(2.0)
    assertThat(record.baseSum).isEqualTo(3.0)
    assertThat(record.value).isEqualTo(23.1)
    assertThat(record.sum).isEqualTo(5.0)
    assertThat(record.time).isEqualTo(6.0)
    assertThat(record.updateTime).isEqualTo(7.7)
    assertThat(record.extensions.getNumber("xi")).isEqualTo(8.0)
    assertThat(record.extensions.getNumber("xf")).isEqualTo(0.1)
  }

  @Test
  fun `given null value when building record in pack then value is unset`() {
    val record = buildRecord {
      value(1)
      value(null)
    }

    assertThat(record.value).isNull()
  }

  private fun buildRecord(block: SenmlRecordDsl.() -> Unit): SenmlRecord =
      buildSenmlPack {
            record(block)
          }
          .first
}
