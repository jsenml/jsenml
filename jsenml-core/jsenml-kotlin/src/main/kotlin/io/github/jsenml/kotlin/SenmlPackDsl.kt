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

import io.github.jsenml.SenmlPackBuilder
import io.github.jsenml.SenmlRecord

/**
 * Mutable DSL receiver used by [buildSenmlPack]. Exposes adding records to a [SenmlPackBuilder],
 * whose `build()` is invoked once the DSL block completes.
 *
 * Example:
 * ```
 * val pack = buildSenmlPack {
 *     record {
 *         baseName("urn:dev:ow:10e2073a01080063:")
 *         name("voltage")
 *         unit("V")
 *         value(120.1)
 *     }
 *     record {
 *         name("current")
 *         unit("A")
 *         value(1.2)
 *     }
 * }
 * ```
 */
@SenmlDsl
public sealed interface SenmlPackDsl {

  /**
   * Appends a record.
   *
   * @param record record to append
   */
  public fun record(record: SenmlRecord)

  /**
   * Appends a record configured by a [SenmlRecordDsl] block.
   *
   * @param block configures the record
   */
  public fun record(block: SenmlRecordDsl.() -> Unit)

  /**
   * Appends all given records, in iteration order.
   *
   * @param records records to append
   */
  public fun records(records: Iterable<SenmlRecord>)
}
