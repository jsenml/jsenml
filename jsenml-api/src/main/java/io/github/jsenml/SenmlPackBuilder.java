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

/** Mutable builder of {@link SenmlPack} instances. Not thread-safe. */
public sealed interface SenmlPackBuilder permits DefaultSenmlPackBuilder {

  /**
   * Appends a record.
   *
   * @param record record to append
   * @return this builder
   */
  SenmlPackBuilder add(SenmlRecord record);

  /**
   * Appends all given records, in iteration order.
   *
   * @param records records to append
   * @return this builder
   */
  SenmlPackBuilder addAll(Iterable<SenmlRecord> records);

  /**
   * Creates an immutable {@link SenmlPack} from the current state of this builder.
   *
   * @return new pack
   */
  SenmlPack build();
}
