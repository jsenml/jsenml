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

import io.github.jsenml.SenmlPack
import io.github.jsenml.SenmlRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SenmlPackDslTest {

  private val a = SenmlRecord.builder().name("a").value(1.0).build()

  private val b = SenmlRecord.builder().name("b").value(2.0).build()

  @Test
  fun `given record blocks when building pack then appends them in order`() {
    val pack = buildSenmlPack {
      record {
        baseName("urn:dev:ow:10e2073a01080063:")
        name("voltage")
        unit("V")
        value(120.1)
      }
      record {
        name("current")
        unit("A")
        value(1.2)
      }
    }

    assertThat(pack.records.map { it.name }).containsExactly("voltage", "current")
    assertThat(pack.first.baseName).isEqualTo("urn:dev:ow:10e2073a01080063:")
  }

  @Test
  fun `given record objects and iterable when building pack then appends them in order`() {
    val pack = buildSenmlPack {
      record(a)
      records(listOf(b, a))
    }

    assertThat(pack).isEqualTo(SenmlPack.of(a, b, a))
  }

  @Test
  fun `given empty block when building pack then returns empty pack`() {
    assertThat(buildSenmlPack {}).isSameAs(SenmlPack.empty())
  }

  @Test
  fun `given records when using senmlPackOf then creates pack in order`() {
    assertThat(senmlPackOf(a, b)).isEqualTo(SenmlPack.of(a, b))
    assertThat(senmlPackOf()).isSameAs(SenmlPack.empty())
  }

  @Test
  fun `given iterable when converting to pack then keeps iteration order`() {
    assertThat(listOf(b, a).toSenmlPack()).isEqualTo(SenmlPack.of(b, a))
    assertThat(setOf(a, b).asIterable().toSenmlPack()).isEqualTo(SenmlPack.of(a, b))
  }

  @Test
  fun `given sequence of pack when filtering and converting back then creates filtered pack`() {
    val pack = senmlPackOf(a, b, a)

    val filtered = pack.asSequence().filter { it.name == "a" }.toSenmlPack()

    assertThat(filtered).isEqualTo(SenmlPack.of(a, a))
  }

  @Test
  fun `given pack when indexing and iterating then works without extensions`() {
    val pack = senmlPackOf(a, b)

    assertThat(pack[1]).isEqualTo(b)
    assertThat(pack.mapNotNull { it.value }).containsExactly(1.0, 2.0)
  }

  @Test
  fun `given resolved pack when sorting by time or key then orders records`() {
    val late = SenmlRecord.builder().name("a").value(1.0).time(1.7e9 + 60).build()
    val early = SenmlRecord.builder().name("b").value(2.0).time(1.7e9).build()
    val pack = senmlPackOf(late, early)

    assertThat(pack.sortedByTime()).isEqualTo(senmlPackOf(early, late))
    assertThat(pack.sorted { it.time }).isEqualTo(senmlPackOf(early, late))
    assertThat(pack.sorted { it.name }).isEqualTo(senmlPackOf(late, early))
  }
}
