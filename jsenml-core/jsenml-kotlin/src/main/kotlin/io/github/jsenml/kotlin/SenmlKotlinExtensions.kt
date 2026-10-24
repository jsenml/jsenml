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
import io.github.jsenml.SenmlPack
import io.github.jsenml.SenmlRecord

/**
 * Builds a [SenmlPack] using a [SenmlPackDsl] block.
 *
 * @param block configures the pack
 * @return the built pack
 */
public fun buildSenmlPack(block: SenmlPackDsl.() -> Unit): SenmlPack =
    DefaultSenmlPackDsl(SenmlPack.builder()).apply(block).build()

/**
 * Creates a [SenmlPack] of given records, in the same order.
 *
 * @param records records of the pack
 * @return new pack
 */
public fun senmlPackOf(vararg records: SenmlRecord): SenmlPack = SenmlPack.of(*records)

/**
 * Creates a [SenmlPack] of records of this iterable, in iteration order.
 *
 * @return new pack
 */
public fun Iterable<SenmlRecord>.toSenmlPack(): SenmlPack =
    SenmlPack.of(this as? Collection<SenmlRecord> ?: toList())

/**
 * Creates a [SenmlPack] of records of this sequence, in sequence order. The sequence is consumed.
 *
 * @return new pack
 */
public fun Sequence<SenmlRecord>.toSenmlPack(): SenmlPack = SenmlPack.of(toList())

/**
 * Returns a copy of this record, modified by a [SenmlRecordDsl] block. Fields not touched by the
 * block keep their values.
 *
 * Example:
 * ```
 * val updated = record.copy { value(24.0) }
 * ```
 *
 * @param block modifies the copy
 * @return the modified copy
 */
public fun SenmlRecord.copy(block: SenmlRecordDsl.() -> Unit): SenmlRecord =
    DefaultSenmlRecordDsl(toBuilder()).apply(block).build()

/**
 * Returns a copy of these extensions, modified by a [SenmlExtensionsDsl] block. Extensions not
 * touched by the block keep their values.
 *
 * Example:
 * ```
 * val updated = extensions.copy { put("accuracy", 0.2) }
 * ```
 *
 * @param block modifies the copy
 * @return the modified copy
 */
public fun SenmlExtensions.copy(block: SenmlExtensionsDsl.() -> Unit): SenmlExtensions =
    DefaultSenmlExtensionsDsl(toBuilder()).apply(block).build()
