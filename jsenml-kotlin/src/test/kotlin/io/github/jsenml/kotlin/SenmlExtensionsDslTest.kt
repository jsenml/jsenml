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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SenmlExtensionsDslTest {

  @Test
  fun `given block when copying empty extensions then all values are applied`() {
    val extensions =
        SenmlExtensions.empty().copy {
          put("foo", "bar")
          put("accuracy", 0.1)
          put("ok", true)
          put("raw", byteArrayOf(1, 2, 3))
          put("tmp", "x")
          remove("tmp")
        }

    assertThat(extensions)
        .isEqualTo(
            SenmlExtensions.builder()
                .put("foo", "bar")
                .put("accuracy", 0.1)
                .put("ok", true)
                .put("raw", byteArrayOf(1, 2, 3))
                .build(),
        )
  }

  @Test
  fun `given empty block when copying empty extensions then returns empty singleton`() {
    assertThat(SenmlExtensions.empty().copy {}).isSameAs(SenmlExtensions.empty())
  }

  @Test
  fun `given other numeric types when copying empty extensions then converts to double`() {
    val extensions =
        SenmlExtensions.empty().copy {
          put("xs", 1.toShort())
          put("xi", 2)
          put("xl", 3L)
          put("xf", 0.1f)
        }

    assertThat(extensions.getNumber("xs")).isEqualTo(1.0)
    assertThat(extensions.getNumber("xi")).isEqualTo(2.0)
    assertThat(extensions.getNumber("xl")).isEqualTo(3.0)
    assertThat(extensions.getNumber("xf")).isEqualTo(0.1)
  }

  @Test
  fun `given extensions when copying then untouched extensions are kept`() {
    val extensions = SenmlExtensions.builder().put("accuracy", 0.1).put("ok", true).build()

    val copy = extensions.copy {
      put("accuracy", 0.2)
      remove("ok")
      put("raw", byteArrayOf(1))
    }

    assertThat(copy)
        .isEqualTo(
            SenmlExtensions.builder().put("accuracy", 0.2).put("raw", byteArrayOf(1)).build(),
        )
    assertThat(extensions.getNumber("accuracy")).isEqualTo(0.1)
  }
}
