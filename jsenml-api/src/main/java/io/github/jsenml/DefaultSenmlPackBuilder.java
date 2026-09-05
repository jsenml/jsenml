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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Default implementation of {@link SenmlPackBuilder}. */
final class DefaultSenmlPackBuilder implements SenmlPackBuilder, Serializable {

  @Serial //
  private static final long serialVersionUID = 1L;

  private final List<SenmlRecord> records = new ArrayList<>();

  DefaultSenmlPackBuilder() {}

  @Override
  public SenmlPackBuilder add(SenmlRecord record) {
    records.add(Objects.requireNonNull(record, "record"));
    return this;
  }

  @Override
  public SenmlPackBuilder addAll(Iterable<SenmlRecord> records) {
    SenmlPackBuilder builder = this;
    for (SenmlRecord record : records) {
      builder = builder.add(record);
    }
    return builder;
  }

  /**
   * Appends records of given builder, which were already validated when added to it.
   *
   * @param other builder to append records of
   * @return this builder
   */
  DefaultSenmlPackBuilder merge(DefaultSenmlPackBuilder other) {
    records.addAll(other.records);
    return this;
  }

  @Override
  public SenmlPack build() {
    return SenmlPack.of(records);
  }

  @Override
  public String toString() {
    return SenmlPackBuilder.class.getSimpleName() + records;
  }
}
