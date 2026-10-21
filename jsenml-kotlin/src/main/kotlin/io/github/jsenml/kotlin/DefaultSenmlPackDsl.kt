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
import io.github.jsenml.SenmlPackBuilder
import io.github.jsenml.SenmlRecord

/** Default implementation of [SenmlPackDsl], backed by a [SenmlPackBuilder]. */
internal class DefaultSenmlPackDsl(private val builder: SenmlPackBuilder) : SenmlPackDsl {

  override fun record(record: SenmlRecord) {
    builder.add(record)
  }

  override fun record(block: SenmlRecordDsl.() -> Unit) {
    builder.add(DefaultSenmlRecordDsl(SenmlRecord.builder()).apply(block).build())
  }

  override fun records(records: Iterable<SenmlRecord>) {
    builder.addAll(records)
  }

  fun build(): SenmlPack = builder.build()
}
