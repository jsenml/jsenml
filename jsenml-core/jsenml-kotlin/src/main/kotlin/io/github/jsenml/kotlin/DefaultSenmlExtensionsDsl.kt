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
import io.github.jsenml.SenmlExtensionsBuilder

/** Default implementation of [SenmlExtensionsDsl], backed by a [SenmlExtensionsBuilder]. */
internal class DefaultSenmlExtensionsDsl(private val builder: SenmlExtensionsBuilder) :
    SenmlExtensionsDsl {

  override fun put(name: String, value: String) {
    builder.put(name, value)
  }

  override fun put(name: String, value: Number) {
    builder.put(name, value)
  }

  override fun put(name: String, value: Boolean) {
    builder.put(name, value)
  }

  override fun put(name: String, value: ByteArray) {
    builder.put(name, value)
  }

  override fun remove(name: String) {
    builder.remove(name)
  }

  fun build(): SenmlExtensions = builder.build()
}
