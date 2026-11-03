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

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Immutable SenML Pack, an ordered list of {@link SenmlRecord}s, as defined in <a
 * href="https://www.rfc-editor.org/rfc/rfc8428#section-4">RFC 8428, Section 4</a>.
 *
 * <p>Base fields of a record apply to that record and all following records of the pack until
 * overridden. Use {@link #resolve()} to obtain the equivalent pack in resolved form.
 */
public sealed interface SenmlPack extends Iterable<SenmlRecord> permits DefaultSenmlPack {

  /**
   * Returns an empty pack.
   *
   * @return empty pack
   */
  static SenmlPack empty() {
    return DefaultSenmlPack.EMPTY;
  }

  /**
   * Creates a pack of a single record.
   *
   * @param record the only record of the pack
   * @return new pack
   * @throws NullPointerException if record is {@code null}
   */
  static SenmlPack of(SenmlRecord record) {
    return of(List.of(record));
  }

  /**
   * Creates a pack of given records.
   *
   * @param records records, in pack order
   * @return new pack
   */
  static SenmlPack of(SenmlRecord... records) {
    return of(List.of(records));
  }

  /**
   * Creates a pack of given records.
   *
   * @param records records, in pack order
   * @return new pack
   */
  static SenmlPack of(Collection<SenmlRecord> records) {
    return records.isEmpty() ? DefaultSenmlPack.EMPTY : new DefaultSenmlPack(List.copyOf(records));
  }

  /**
   * Returns a {@link Collector} accumulating records of a stream into a pack, in encounter order.
   *
   * <pre>{@code
   * SenmlPack temperatures =
   *     pack.stream()
   *         .filter(record -> "Cel".equals(record.getUnit()))
   *         .collect(SenmlPack.collector());
   * }</pre>
   *
   * <p>The returned collector is stateless and shared between calls; it can be used for any number
   * of concurrent collections.
   *
   * @return collector creating a pack; it throws {@link NullPointerException} on {@code null}
   *     records and yields {@link #empty()} for an empty stream
   */
  static Collector<SenmlRecord, ?, SenmlPack> collector() {
    return DefaultSenmlPack.COLLECTOR;
  }

  /**
   * Creates a new, empty builder.
   *
   * @return new builder
   */
  static SenmlPackBuilder builder() {
    return new DefaultSenmlPackBuilder();
  }

  /**
   * Returns records of this pack.
   *
   * @return unmodifiable list of records, in pack order
   */
  List<SenmlRecord> getRecords();

  /**
   * Returns record at given position.
   *
   * @param index zero-based position of the record within this pack
   * @return record at given position
   * @throws IndexOutOfBoundsException if index is out of range
   */
  SenmlRecord get(int index);

  /**
   * Returns the first record of this pack.
   *
   * @return first record
   * @throws NoSuchElementException if this pack is empty
   */
  SenmlRecord getFirst();

  /**
   * Returns the last record of this pack.
   *
   * @return last record
   * @throws NoSuchElementException if this pack is empty
   */
  SenmlRecord getLast();

  /**
   * Returns number of records in this pack.
   *
   * @return number of records
   */
  int size();

  /**
   * Returns whether this pack has no records.
   *
   * @return {@code true} if this pack has no records
   */
  boolean isEmpty();

  /**
   * Returns whether this pack is in resolved form, as defined in <a
   * href="https://www.rfc-editor.org/rfc/rfc8428#section-4.6">RFC 8428, Section 4.6</a>: no record
   * has base fields or base extensions, every record has an absolute time, and the Base Version, if
   * other than the default one, is present in every record. Resolving such a pack returns it as is.
   *
   * <p>Resolved form does not imply validity, e.g. records may still lack a name; only {@link
   * #resolve()} validates records.
   *
   * @return {@code true} if this pack is in resolved form, including an empty pack
   */
  boolean isResolved();

  /**
   * Returns a sequential stream of records of this pack.
   *
   * @return stream of records, in pack order
   */
  Stream<SenmlRecord> stream();

  /**
   * Resolves this pack using the system UTC clock for relative times, accepting no extensions that
   * are mandatory to understand.
   *
   * @return pack in resolved form
   * @throws SenmlViolationException if any resolved record is not valid SenML
   * @see #resolve(Instant, Set)
   */
  default SenmlPack resolve() {
    return resolve(Clock.systemUTC(), Set.of());
  }

  /**
   * Resolves this pack using the system UTC clock for relative times.
   *
   * @param extensions names of extensions that are mandatory to understand (ending with {@code _})
   *     the caller understands
   * @return pack in resolved form
   * @throws SenmlViolationException if any resolved record is not valid SenML
   * @throws IllegalArgumentException if any accepted name is not a mandatory extension name
   * @see #resolve(Instant, Set)
   */
  default SenmlPack resolve(Set<String> extensions) {
    return resolve(Clock.systemUTC(), extensions);
  }

  /**
   * Resolves this pack using given clock for relative times, accepting no extensions that are
   * mandatory to understand.
   *
   * @param clock clock providing "now" for relative times
   * @return pack in resolved form
   * @throws SenmlViolationException if any resolved record is not valid SenML
   * @see #resolve(Instant, Set)
   */
  default SenmlPack resolve(Clock clock) {
    return resolve(clock.instant(), Set.of());
  }

  /**
   * Resolves this pack using given clock for relative times.
   *
   * @param clock clock providing "now" for relative times
   * @param extensions names of extensions that are mandatory to understand (ending with {@code _})
   *     the caller understands
   * @return pack in resolved form
   * @throws SenmlViolationException if any resolved record is not valid SenML
   * @throws IllegalArgumentException if any accepted name is not a mandatory extension name
   * @see #resolve(Instant, Set)
   */
  default SenmlPack resolve(Clock clock, Set<String> extensions) {
    return resolve(clock.instant(), extensions);
  }

  /**
   * Resolves this pack, accepting no extensions that are mandatory to understand.
   *
   * @param now instant that relative times are relative to
   * @return pack in resolved form
   * @throws SenmlViolationException if any resolved record is not valid SenML
   * @see #resolve(Instant, Set)
   */
  default SenmlPack resolve(Instant now) {
    return resolve(now, Set.of());
  }

  /**
   * Resolves this pack, as defined in <a
   * href="https://www.rfc-editor.org/rfc/rfc8428#section-4.6">RFC 8428, Section 4.6</a>.
   *
   * <p>In resolved form, base fields (except Base Version) are applied to and removed from each
   * record: names are concatenated, base unit fills missing units, base value is added to value,
   * base time is added to time, and relative times (below 2<sup>28</sup>) are converted to absolute
   * times relative to given instant.
   *
   * <p>Additions are done in decimal and rounded to {@code double} once, so {@code 0.1 + 0.2}
   * resolves to {@code 0.3}.
   *
   * <p>Base sum and sum are added together, a missing one counting as zero, so a record with base
   * sum in effect but no sum of its own resolves to the base sum (RFC 8428, Section 4.5.4). Base
   * value is only applied to records with a numeric value, as applying it to records with other
   * value types would yield a second value field.
   *
   * <p>Any Base Version is accepted and kept on resolved records if other than the default one, so
   * that applications can build their own SenML-based models on it. Rejecting packs of a version
   * the application does not understand, or of mixed versions (RFC 8428, Section 4.4), is left to
   * the application.
   *
   * <p>Every resolved record must have a valid name, at most one value field ({@code v}, {@code
   * vs}, {@code vb}, {@code vd}), and a value field unless it has a sum (RFC 8428, Section 4.2).
   *
   * <p>Extensions that are mandatory to understand (names ending with {@code _}) must be listed in
   * {@code extensions}, otherwise they are reported as violations, as required by RFC 8428, Section
   * 4.4. Other extensions are always accepted.
   *
   * <p>Regular extensions are kept on each resolved record. Base extensions (names starting with
   * {@code b}) are not carried over, as resolved records have no base fields and their resolution
   * is defined by the specifications introducing them; the caller handles accepted mandatory ones
   * from the original pack.
   *
   * @param now instant that relative times are relative to
   * @param extensions names of extensions that are mandatory to understand (ending with {@code _})
   *     the caller understands
   * @return pack in resolved form
   * @throws SenmlViolationException if any resolved record is not valid SenML, listing violations
   *     of all records
   * @throws IllegalArgumentException if any accepted name is not a mandatory extension name
   */
  SenmlPack resolve(Instant now, Set<String> extensions);
}
