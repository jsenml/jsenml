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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;

/** Default implementation of {@link SenmlPack}. */
final class DefaultSenmlPack implements SenmlPack, Serializable {

  @Serial //
  private static final long serialVersionUID = 1L;

  static final SenmlPack EMPTY = new DefaultSenmlPack(List.of());

  /** Stateless, so shared: each collection gets its own builder from the supplier. */
  static final Collector<SenmlRecord, ?, SenmlPack> COLLECTOR =
      Collector.of(
          DefaultSenmlPackBuilder::new,
          DefaultSenmlPackBuilder::add,
          DefaultSenmlPackBuilder::merge,
          DefaultSenmlPackBuilder::build);

  private final List<SenmlRecord> records;

  private static final byte UNKNOWN = 0;
  private static final byte RESOLVED = 1;
  private static final byte NOT_RESOLVED = 2;

  /**
   * Whether records are in resolved form, see {@link #isResolved()}: {@link #UNKNOWN} until first
   * computed. The race between threads is benign, like {@code String.hash}, as the result is
   * deterministic. Not serialized, so recomputed after deserialization.
   */
  private transient byte resolvedState;

  DefaultSenmlPack(List<SenmlRecord> records) {
    this(records, UNKNOWN);
  }

  DefaultSenmlPack(List<SenmlRecord> records, byte resolvedState) {
    this.records = records;
    this.resolvedState = resolvedState;
  }

  @Override
  public List<SenmlRecord> getRecords() {
    return records;
  }

  @Override
  public SenmlRecord get(int index) {
    return records.get(index);
  }

  @Override
  public SenmlRecord getFirst() {
    if (records.isEmpty()) {
      throw new NoSuchElementException();
    }
    return records.get(0);
  }

  @Override
  public SenmlRecord getLast() {
    if (records.isEmpty()) {
      throw new NoSuchElementException();
    }
    return records.get(records.size() - 1);
  }

  @Override
  public int size() {
    return records.size();
  }

  @Override
  public boolean isEmpty() {
    return records.isEmpty();
  }

  @Override
  public boolean isResolved() {
    byte state = resolvedState;
    if (state == UNKNOWN) {
      state = SenmlResolution.isResolved(records) ? RESOLVED : NOT_RESOLVED;
      resolvedState = state;
    }
    return state == RESOLVED;
  }

  @Override
  public Iterator<SenmlRecord> iterator() {
    return records.iterator();
  }

  @Override
  public Spliterator<SenmlRecord> spliterator() {
    return Spliterators.spliterator(
        records, Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
  }

  @Override
  public Stream<SenmlRecord> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  @Override
  public SenmlPack resolve(Instant now, Set<String> extensions) {
    SenmlResolution resolution = new SenmlResolution(now, extensions);
    if (isResolved()) {
      records.forEach(resolution::validate);
      resolution.throwIfViolated();
      return this;
    }
    List<SenmlRecord> resolved = new ArrayList<>(records.size());
    for (SenmlRecord record : records) {
      resolved.add(resolution.resolve(record));
    }
    resolution.throwIfViolated();
    return new DefaultSenmlPack(List.copyOf(resolved), RESOLVED);
  }

  @Override
  public <U extends Comparable<? super U>> SenmlPack sorted(
      Function<? super SenmlRecord, ? extends @Nullable U> keyExtractor) {
    Objects.requireNonNull(keyExtractor, "keyExtractor");
    if (!isResolved()) {
      throw new IllegalStateException("SenML pack must be in resolved form to be sorted");
    }
    Comparator<@Nullable U> keyOrder = Comparator.nullsFirst(Comparator.naturalOrder());
    Comparator<SenmlRecord> order = Comparator.comparing(keyExtractor, keyOrder);
    return new DefaultSenmlPack(records.stream().sorted(order).toList(), RESOLVED);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SenmlPack other)) {
      return false;
    }
    return records.equals(other.getRecords());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(records);
  }

  @Override
  public String toString() {
    return "SenmlPack" + records;
  }
}
