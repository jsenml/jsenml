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
import io.github.jsenml.SenmlRecordBuilder

/** Default implementation of [SenmlRecordDsl], backed by a [SenmlRecordBuilder]. */
internal class DefaultSenmlRecordDsl(private val builder: SenmlRecordBuilder) : SenmlRecordDsl {

  override fun baseName(baseName: String?) {
    builder.baseName(baseName)
  }

  override fun baseTime(baseTime: Number?) {
    builder.baseTime(baseTime)
  }

  override fun baseUnit(baseUnit: String?) {
    builder.baseUnit(baseUnit)
  }

  override fun baseValue(baseValue: Number?) {
    builder.baseValue(baseValue)
  }

  override fun baseSum(baseSum: Number?) {
    builder.baseSum(baseSum)
  }

  override fun baseVersion(baseVersion: Int?) {
    builder.baseVersion(baseVersion)
  }

  override fun name(name: String?) {
    builder.name(name)
  }

  override fun unit(unit: String?) {
    builder.unit(unit)
  }

  override fun value(value: Number?) {
    builder.value(value)
  }

  override fun stringValue(stringValue: String?) {
    builder.stringValue(stringValue)
  }

  override fun booleanValue(booleanValue: Boolean?) {
    builder.booleanValue(booleanValue)
  }

  override fun dataValue(dataValue: String?) {
    builder.dataValue(dataValue)
  }

  override fun dataValue(dataValue: ByteArray?) {
    builder.dataValue(dataValue)
  }

  override fun sum(sum: Number?) {
    builder.sum(sum)
  }

  override fun time(time: Number?) {
    builder.time(time)
  }

  override fun updateTime(updateTime: Number?) {
    builder.updateTime(updateTime)
  }

  override fun extension(name: String, value: String) {
    builder.extension(name, value)
  }

  override fun extension(name: String, value: Number) {
    builder.extension(name, value)
  }

  override fun extension(name: String, value: Boolean) {
    builder.extension(name, value)
  }

  override fun extension(name: String, value: ByteArray) {
    builder.extension(name, value)
  }

  override fun removeExtension(name: String) {
    builder.removeExtension(name)
  }

  override fun extensions(extensions: SenmlExtensions) {
    builder.extensions(extensions)
  }

  override fun extensions(block: SenmlExtensionsDsl.() -> Unit) {
    builder.extensions(DefaultSenmlExtensionsDsl(SenmlExtensions.builder()).apply(block).build())
  }

  fun build(): SenmlRecord = builder.build()
}
