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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class SenmlPackTest {

  private static final String INVALID_NAME =
      "resolved name is empty or contains invalid characters";

  private static final String MULTIPLE_VALUE_FIELDS = "more than one value field";

  private static final String MISSING_VALUE_AND_SUM = "neither value nor sum";

  private static final String VALUE_OVERFLOW = "resolved value is not finite";

  private static final String SUM_OVERFLOW = "resolved sum is not finite";

  private static final String TIME_OVERFLOW = "resolved time is not finite";

  private static final SenmlRecord A = SenmlRecord.builder().name("a").value(1.0).build();
  private static final SenmlRecord B = SenmlRecord.builder().name("b").value(2.0).build();

  private static final Instant NOW = Instant.ofEpochSecond(1_700_000_000, 500_000_000);

  @Test
  void givenRecords_whenCreatingPack_thenKeepsOrder() {
    SenmlPack pack = SenmlPack.of(A, B);

    assertThat(pack.getRecords()).containsExactly(A, B);
    assertThat(pack).containsExactly(A, B);
    assertThat(pack.size()).isEqualTo(2);
    assertThat(pack.isEmpty()).isFalse();
  }

  @Test
  void givenPack_whenGettingByIndex_thenReturnsRecordAtPosition() {
    SenmlPack pack = SenmlPack.of(A, B);

    assertThat(pack.get(0)).isSameAs(A);
    assertThat(pack.get(1)).isSameAs(B);
  }

  @Test
  void givenIndexOutOfRange_whenGettingByIndex_thenThrows() {
    SenmlPack pack = SenmlPack.of(A);

    assertThatThrownBy(() -> pack.get(1)).isInstanceOf(IndexOutOfBoundsException.class);
    assertThatThrownBy(() -> pack.get(-1)).isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  void givenPack_whenGettingFirstAndLast_thenReturnsRecordsAtBothEnds() {
    SenmlPack pack = SenmlPack.of(A, B, A);

    assertThat(pack.getFirst()).isSameAs(A);
    assertThat(pack.getLast()).isSameAs(A);
    assertThat(SenmlPack.of(A, B).getLast()).isSameAs(B);
  }

  @Test
  void givenSingleRecordPack_whenGettingFirstAndLast_thenReturnsThatRecord() {
    SenmlPack pack = SenmlPack.of(B);

    assertThat(pack.getFirst()).isSameAs(B);
    assertThat(pack.getLast()).isSameAs(B);
  }

  @Test
  void givenEmptyPack_whenGettingFirstOrLast_thenThrows() {
    assertThatThrownBy(() -> SenmlPack.empty().getFirst())
        .isInstanceOf(NoSuchElementException.class);
    assertThatThrownBy(() -> SenmlPack.empty().getLast())
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void givenPack_whenStreaming_thenStreamsRecordsInOrder() {
    SenmlPack pack = SenmlPack.of(A, B, A);

    assertThat(pack.stream()).containsExactly(A, B, A);
    assertThat(pack.stream().map(SenmlRecord::getName)).containsExactly("a", "b", "a");
    assertThat(SenmlPack.empty().stream()).isEmpty();
  }

  @Test
  void givenPack_whenIteratingWithForEach_thenVisitsRecordsInOrder() {
    List<SenmlRecord> visited = new ArrayList<>();

    SenmlPack.of(A, B).forEach(visited::add);

    assertThat(visited).containsExactly(A, B);
  }

  @Test
  void givenPack_whenGettingSpliterator_thenReportsSizedOrderedImmutableNonNull() {
    Spliterator<SenmlRecord> spliterator = SenmlPack.of(A, B).spliterator();

    assertThat(spliterator.estimateSize()).isEqualTo(2);
    assertThat(spliterator.getExactSizeIfKnown()).isEqualTo(2);
    assertThat(
            spliterator.hasCharacteristics(
                Spliterator.ORDERED
                    | Spliterator.SIZED
                    | Spliterator.SUBSIZED
                    | Spliterator.IMMUTABLE
                    | Spliterator.NONNULL))
        .isTrue();
  }

  @Test
  void givenMutableList_whenCreatingPack_thenPackIsNotAffectedByLaterChanges() {
    List<SenmlRecord> records = new ArrayList<>(List.of(A));

    SenmlPack pack = SenmlPack.of(records);
    records.add(B);

    assertThat(pack.getRecords()).containsExactly(A);
  }

  @Test
  void givenPack_whenModifyingRecords_thenThrows() {
    SenmlPack pack = SenmlPack.of(A);

    assertThatThrownBy(() -> pack.getRecords().add(B))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void givenPackFromVarargsOrCollection_whenModifyingRecords_thenThrows() {
    for (SenmlPack pack : List.of(SenmlPack.of(A, B), SenmlPack.of(List.of(A, B)))) {
      assertThatThrownBy(() -> pack.getRecords().add(A))
          .isInstanceOf(UnsupportedOperationException.class);
      assertThatThrownBy(() -> pack.getRecords().set(0, B))
          .isInstanceOf(UnsupportedOperationException.class);
      assertThatThrownBy(
              () -> {
                Iterator<SenmlRecord> iterator = pack.iterator();
                iterator.next();
                iterator.remove();
              })
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Test
  void givenNoRecords_whenCreatingPack_thenReturnsEmptyPack() {
    assertThat(SenmlPack.of()).isSameAs(SenmlPack.empty());
    assertThat(SenmlPack.builder().build()).isSameAs(SenmlPack.empty());
    assertThat(SenmlPack.empty().isEmpty()).isTrue();
  }

  @Test
  void givenBuilder_whenAddingRecords_thenBuildsPackInOrder() {
    SenmlPack pack = SenmlPack.builder().add(A).addAll(List.of(B, A)).build();

    assertThat(pack).isEqualTo(SenmlPack.of(A, B, A)).hasSameHashCodeAs(SenmlPack.of(A, B, A));
  }

  @Test
  void givenPack_whenToString_thenListsRecordsInBrackets() {
    assertThat(SenmlPack.of(A, B))
        .hasToString("SenmlPack[SenmlRecord[n=a, v=1.0], SenmlRecord[n=b, v=2.0]]");
    assertThat(SenmlPack.empty()).hasToString("SenmlPack[]");
  }

  @Test
  void givenSingleRecord_whenCreatingPack_thenContainsOnlyThatRecord() {
    SenmlPack pack = SenmlPack.of(A);

    assertThat(pack.getRecords()).containsExactly(A);
    assertThat(pack.size()).isEqualTo(1);
    assertThat(pack)
        .isEqualTo(SenmlPack.of(List.of(A)))
        .isEqualTo(SenmlPack.builder().add(A).build());
    assertThatThrownBy(() -> pack.getRecords().add(B))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @SuppressWarnings("NullAway")
  @Test
  void givenNullSingleRecord_whenCreatingPack_thenThrows() {
    assertThatThrownBy(() -> SenmlPack.of((SenmlRecord) null))
        .isInstanceOf(NullPointerException.class);
  }

  @SuppressWarnings("NullAway")
  @Test
  void givenNullRecord_whenCreatingPack_thenThrows() {
    assertThatThrownBy(() -> SenmlPack.of(A, null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void givenRfcMultipleDatapoints_whenResolving_thenAppliesBaseFields() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .baseName("urn:dev:ow:10e2073a01080063:")
                .baseTime(1.320067464e9)
                .baseUnit("%RH")
                .value(20.0)
                .build(),
            SenmlRecord.builder().unit("lon").value(24.30621).build(),
            SenmlRecord.builder().unit("lat").value(60.07965).build(),
            SenmlRecord.builder().time(60.0).value(20.3).build());

    assertThat(pack.resolve(NOW))
        .containsExactly(
            resolved("urn:dev:ow:10e2073a01080063:", 1.320067464e9).unit("%RH").value(20.0).build(),
            resolved("urn:dev:ow:10e2073a01080063:", 1.320067464e9)
                .unit("lon")
                .value(24.30621)
                .build(),
            resolved("urn:dev:ow:10e2073a01080063:", 1.320067464e9)
                .unit("lat")
                .value(60.07965)
                .build(),
            resolved("urn:dev:ow:10e2073a01080063:", 1.320067524e9)
                .unit("%RH")
                .value(20.3)
                .build());
  }

  @Test
  void givenBaseNameAndName_whenResolving_thenConcatenatesThem() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder().baseName("dev:").name("temp").value(1.0).time(1.5e9).build(),
            SenmlRecord.builder().name("hum").value(2.0).time(1.5e9).build());

    assertThat(pack.resolve(NOW).getRecords())
        .extracting(SenmlRecord::getName)
        .containsExactly("dev:temp", "dev:hum");
  }

  @Test
  void givenDecimalFractions_whenResolving_thenAddsWithoutBinaryRoundingErrors() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .baseName("a")
                .baseValue(0.1)
                .baseSum(0.1)
                .baseTime(1_500_000_000.1)
                .value(0.2)
                .sum(0.2)
                .time(0.2)
                .build());

    SenmlRecord record = pack.resolve(NOW).getFirst();

    assertThat(record.getValue()).isEqualTo(0.3);
    assertThat(record.getSum()).isEqualTo(0.3);
    assertThat(record.getTime()).isEqualTo(1_500_000_000.3);
  }

  @Test
  void givenRelativeTimeWithFraction_whenResolving_thenAddsToNowWithoutBinaryRoundingErrors() {
    Instant now = Instant.ofEpochSecond(1_700_000_000, 100_000_000);
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("a").value(1.0).time(0.2).build());

    assertThat(pack.resolve(now).getFirst().getTime()).isEqualTo(1_700_000_000.3);
  }

  @Test
  void givenBaseValueAndBaseSum_whenResolving_thenAddsThemToValueAndSum() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .baseName("a")
                .baseValue(10.0)
                .baseSum(100.0)
                .value(1.0)
                .sum(2.0)
                .time(1.5e9)
                .build(),
            SenmlRecord.builder().stringValue("x").time(1.5e9).build());

    SenmlPack resolved = pack.resolve(NOW);

    assertThat(resolved.getRecords().get(0).getValue()).isEqualTo(11.0);
    assertThat(resolved.getRecords().get(0).getSum()).isEqualTo(102.0);
    assertThat(resolved.getRecords().get(1).getValue()).isNull();
    assertThat(resolved.getRecords().get(1).getSum()).isEqualTo(100.0);
  }

  @Test
  void givenBaseSumWithoutSum_whenResolving_thenSumIsBaseSum() {
    SenmlPack pack =
        SenmlPack.of(SenmlRecord.builder().baseName("a").baseSum(100.0).time(1.5e9).build());

    assertThat(pack.resolve(NOW).getRecords().getFirst().getSum()).isEqualTo(100.0);
  }

  @Test
  void givenSumWithoutBaseSum_whenResolving_thenKeepsSum() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("a").sum(5.0).time(1.5e9).build());

    assertThat(pack.resolve(NOW).getRecords().getFirst().getSum()).isEqualTo(5.0);
  }

  @Test
  void givenNoBaseSumAndNoSum_whenResolving_thenSumIsAbsent() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("a").value(1.0).time(1.5e9).build());

    assertThat(pack.resolve(NOW).getRecords().getFirst().getSum()).isNull();
  }

  @Test
  void givenValueAndSum_whenResolving_thenKeepsBoth() {
    SenmlPack pack =
        SenmlPack.of(SenmlRecord.builder().name("a").unit("W").value(1500.0).sum(3.6e6).build());

    SenmlRecord record = pack.resolve(NOW).getRecords().getFirst();

    assertThat(record.getValue()).isEqualTo(1500.0);
    assertThat(record.getSum()).isEqualTo(3.6e6);
  }

  @Test
  void givenClock_whenResolving_thenUsesClockInstantAsNow() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("a").value(1.0).time(-60.0).build());

    assertThat(pack.resolve(Clock.fixed(NOW, ZoneOffset.UTC))).isEqualTo(pack.resolve(NOW));
  }

  @Test
  void givenRelativeTimes_whenResolving_thenMakesThemAbsoluteUsingClock() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder().name("now").value(1.0).build(),
            SenmlRecord.builder().name("past").value(1.0).time(-60.0).build(),
            SenmlRecord.builder().name("base").baseTime(-10.0).value(1.0).time(5.0).build());

    assertThat(pack.resolve(NOW).getRecords())
        .extracting(SenmlRecord::getTime)
        .containsExactly(1_700_000_000.5, 1_699_999_940.5, 1_699_999_995.5);
  }

  @Test
  void givenTimeAtThreshold_whenResolving_thenTreatsItAsAbsolute() {
    SenmlPack pack =
        SenmlPack.of(SenmlRecord.builder().name("a").value(1.0).time(268_435_456.0).build());

    assertThat(pack.resolve(NOW).getRecords().getFirst().getTime()).isEqualTo(268_435_456.0);
  }

  @Test
  void givenBaseVersionAndUpdateTime_whenResolving_thenKeepsThem() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder().baseVersion(11).name("a").value(1.0).updateTime(30.0).build(),
            SenmlRecord.builder().name("b").value(1.0).build());

    assertThat(pack.resolve(NOW).getRecords())
        .allSatisfy(record -> assertThat(record.getBaseVersion()).isEqualTo(11));
    assertThat(pack.resolve(NOW).getRecords().getFirst().getUpdateTime()).isEqualTo(30.0);
  }

  @Test
  void givenResolvedRecords_whenResolving_thenNoBaseFieldsExceptVersionRemain() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .baseName("a")
                .baseTime(1.5e9)
                .baseUnit("V")
                .baseValue(1.0)
                .baseSum(1.0)
                .value(1.0)
                .build());

    SenmlRecord record = pack.resolve(NOW).getRecords().getFirst();

    assertThat(record.getBaseName()).isNull();
    assertThat(record.getBaseTime()).isNull();
    assertThat(record.getBaseUnit()).isNull();
    assertThat(record.getBaseValue()).isNull();
    assertThat(record.getBaseSum()).isNull();
  }

  @Test
  void givenMissingName_whenResolving_thenReportsViolation() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().value(1.0).build());

    assertThat(violationsOf(pack)).containsExactly(SenmlViolation.from(0, INVALID_NAME));
  }

  @Test
  void givenNameWithInvalidCharacters_whenResolving_thenReportsViolation() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("-temp").value(1.0).build());

    assertThat(violationsOf(pack)).containsExactly(SenmlViolation.from(0, INVALID_NAME));
  }

  @Test
  void givenNeitherValueNorSum_whenResolving_thenReportsViolation() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder().baseName("dev:").baseTime(1.5e9).build(),
            SenmlRecord.builder().name("a").value(1.0).build());

    assertThat(violationsOf(pack)).containsExactly(SenmlViolation.from(0, MISSING_VALUE_AND_SUM));
  }

  @Test
  void givenMultipleValueFields_whenResolving_thenReportsViolation() {
    SenmlPack pack =
        SenmlPack.of(SenmlRecord.builder().name("a").value(1.0).booleanValue(true).build());

    assertThat(violationsOf(pack)).containsExactly(SenmlViolation.from(0, MULTIPLE_VALUE_FIELDS));
  }

  @Test
  void givenTimeOverflowingToNegativeInfinity_whenResolving_thenReportsTimeOverflow() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .name("a")
                .value(1.0)
                .baseTime(-Double.MAX_VALUE)
                .time(-Double.MAX_VALUE)
                .build());

    assertThat(violationsOf(pack)).containsExactly(SenmlViolation.from(0, TIME_OVERFLOW));
  }

  @Test
  void givenOverflowingResolvedNumbers_whenResolving_thenReportsViolations() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .baseName("a")
                .baseValue(Double.MAX_VALUE)
                .baseSum(Double.MAX_VALUE)
                .baseTime(Double.MAX_VALUE)
                .value(Double.MAX_VALUE)
                .sum(Double.MAX_VALUE)
                .time(Double.MAX_VALUE)
                .build());

    assertThat(violationsOf(pack))
        .containsExactly(
            SenmlViolation.from(0, VALUE_OVERFLOW),
            SenmlViolation.from(0, SUM_OVERFLOW),
            SenmlViolation.from(0, TIME_OVERFLOW));
  }

  @Test
  void givenOverflowAndOtherProblemsInOneRecord_whenResolving_thenReportsAllOfThem() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .name("-a")
                .baseValue(Double.MAX_VALUE)
                .value(Double.MAX_VALUE)
                .stringValue("x")
                .build());

    assertThat(violationsOf(pack))
        .containsExactly(
            SenmlViolation.from(0, VALUE_OVERFLOW),
            SenmlViolation.from(0, INVALID_NAME),
            SenmlViolation.from(0, MULTIPLE_VALUE_FIELDS));
  }

  @Test
  void givenManyInvalidRecords_whenResolving_thenReportsAllViolationsInPackOrder() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder().name("-a").value(1.0).stringValue("x").build(),
            SenmlRecord.builder().name("ok").value(1.0).build(),
            SenmlRecord.builder().name("b").build(),
            SenmlRecord.builder().value(1.0).build());

    assertThatThrownBy(() -> pack.resolve(NOW))
        .isInstanceOfSatisfying(
            SenmlViolationException.class,
            e ->
                assertThat(e.getViolations())
                    .containsExactly(
                        SenmlViolation.from(0, INVALID_NAME),
                        SenmlViolation.from(0, MULTIPLE_VALUE_FIELDS),
                        SenmlViolation.from(2, MISSING_VALUE_AND_SUM),
                        SenmlViolation.from(3, INVALID_NAME)))
        .hasMessage(
            "4 SenML violation(s): record #0 resolved name is empty or contains invalid characters;"
                + " record #0 more than one value field; record #2 neither value nor sum; record #3"
                + " resolved name is empty or contains invalid characters");
  }

  private static List<SenmlViolation> violationsOf(SenmlPack pack) {
    try {
      pack.resolve(NOW);
    } catch (SenmlViolationException e) {
      return e.getViolations();
    }
    throw new AssertionError("Expected SenmlViolationException");
  }

  private static SenmlRecordBuilder resolved(String name, double time) {
    return SenmlRecord.builder().name(name).time(time);
  }

  @Test
  void givenRegularExtensions_whenResolving_thenKeepsThemOnResolvedRecords() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .baseName("dev:")
                .name("t")
                .value(1.0)
                .extension("foo", "bar")
                .extension("accuracy_", 0.1)
                .build());

    SenmlRecord resolved = pack.resolve(NOW, Set.of("accuracy_")).getFirst();

    assertThat(resolved.getExtensions().names()).containsExactly("foo", "accuracy_");
    assertThat(resolved.getExtensions().getString("foo")).isEqualTo("bar");
  }

  @Test
  void givenOptionalBaseExtension_whenResolving_thenDropsIt() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .name("t")
                .value(1.0)
                .extension("bfoo", "x")
                .extension("foo", "y")
                .build());

    assertThat(pack.resolve(NOW).getFirst().getExtensions().names()).containsExactly("foo");
  }

  @Test
  void givenMandatoryExtensionNotAccepted_whenResolving_thenReportsViolation() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder().name("t").value(1.0).extension("foo_", "x").build(),
            SenmlRecord.builder().name("u").value(1.0).extension("bfoo_", "y").build());

    assertThat(violationsOf(pack))
        .containsExactly(
            SenmlViolation.from(0, "mandatory extension 'foo_' is not supported"),
            SenmlViolation.from(1, "mandatory extension 'bfoo_' is not supported"));
  }

  @Test
  void givenAcceptedMandatoryExtensions_whenResolving_thenKeepsRegularAndDropsBaseOnes() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .name("t")
                .value(1.0)
                .extension("foo_", "x")
                .extension("bfoo_", "y")
                .extension("qux", "z")
                .build());

    SenmlPack resolved = pack.resolve(NOW, Set.of("foo_", "bfoo_"));

    assertThat(resolved.getFirst().getExtensions().names()).containsExactly("foo_", "qux");
  }

  @Test
  void givenAcceptedExtensions_whenResolvingWithClock_thenUsesThemToo() {
    SenmlPack pack =
        SenmlPack.of(SenmlRecord.builder().name("t").value(1.0).extension("foo_", "x").build());

    SenmlPack resolved = pack.resolve(Clock.fixed(NOW, ZoneOffset.UTC), Set.of("foo_"));

    assertThat(resolved.getFirst().getExtensions().getString("foo_")).isEqualTo("x");
  }

  @ParameterizedTest
  @ValueSource(strings = {"foo", "-foo_", "n", "a b_"})
  void givenInvalidAcceptedExtension_whenResolving_thenThrows(String accepted) {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("t").value(1.0).build());

    assertThatThrownBy(() -> pack.resolve(NOW, Set.of(accepted)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(accepted);
  }

  @Test
  void givenBuilder_whenToString_thenListsRecordsAddedSoFar() {
    SenmlPackBuilder builder = SenmlPack.builder().add(A).add(B);

    assertThat(builder)
        .hasToString("SenmlPackBuilder[SenmlRecord[n=a, v=1.0], SenmlRecord[n=b, v=2.0]]");
    assertThat(SenmlPack.builder()).hasToString("SenmlPackBuilder[]");
  }

  @Test
  void givenStreamOfRecords_whenCollecting_thenCreatesPackInEncounterOrder() {
    SenmlPack pack = Stream.of(A, B, A).collect(SenmlPack.collector());

    assertThat(pack).isEqualTo(SenmlPack.of(A, B, A));
  }

  @Test
  void givenFilteredPackStream_whenCollecting_thenKeepsMatchingRecords() {
    SenmlPack pack =
        SenmlPack.of(A, B, A).stream()
            .filter(record -> "b".equals(record.getName()))
            .collect(SenmlPack.collector());

    assertThat(pack).isEqualTo(SenmlPack.of(B));
  }

  @Test
  void givenEmptyStream_whenCollecting_thenReturnsEmptySingleton() {
    assertThat(Stream.<SenmlRecord>empty().collect(SenmlPack.collector()))
        .isSameAs(SenmlPack.empty());
  }

  @Test
  void givenLargeParallelStream_whenCollecting_thenPreservesEncounterOrder() {
    List<SenmlRecord> records =
        IntStream.range(0, 1000)
            .mapToObj(i -> SenmlRecord.builder().name("r" + i).value((double) i).build())
            .toList();

    SenmlPack pack = records.parallelStream().collect(SenmlPack.collector());

    assertThat(pack.getRecords()).containsExactlyElementsOf(records);
  }

  @SuppressWarnings("NullAway")
  @Test
  void givenNullRecord_whenCollecting_thenThrows() {
    assertThatThrownBy(() -> Stream.of(A, null).collect(SenmlPack.collector()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void
      givenCollector_whenRequestedTwiceAndReused_thenSharesInstanceAndKeepsCollectionsIndependent() {
    Collector<SenmlRecord, ?, SenmlPack> collector = SenmlPack.collector();

    SenmlPack first = Stream.of(A).collect(collector);
    SenmlPack second = Stream.of(B, B).collect(collector);

    assertThat(SenmlPack.collector()).isSameAs(collector);
    assertThat(first).isEqualTo(SenmlPack.of(A));
    assertThat(second).isEqualTo(SenmlPack.of(B, B));
  }

  @Test
  void givenDefaultBaseVersion_whenResolving_thenOmitsIt() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().baseVersion(10).name("a").value(1).build());

    SenmlPack resolved = pack.resolve(NOW);

    assertThat(resolved.getFirst().getBaseVersion()).isNull();
    assertThat(resolved.isResolved()).isTrue();
  }

  @Test
  void givenRfcExamples_whenCheckingResolved_thenOnlyResolvedDataIs() {
    assertThat(RfcExamples.RESOLVED_DATA.isResolved()).isTrue();
    assertThat(RfcExamples.MULTIPLE_MEASUREMENTS.isResolved()).isFalse();
    assertThat(RfcExamples.MULTIPLE_MEASUREMENTS.resolve().isResolved()).isTrue();
    assertThat(SenmlPack.empty().isResolved()).isTrue();
  }

  @Test
  void givenRecordsNotInResolvedForm_whenCheckingResolved_thenReturnsFalse() {
    SenmlRecord resolved = SenmlRecord.builder().name("a").value(1).time(1.7e9).build();

    assertThat(SenmlPack.of(resolved).isResolved()).isTrue();
    assertThat(SenmlPack.of(resolved.toBuilder().baseUnit("V").build()).isResolved()).isFalse();
    assertThat(SenmlPack.of(resolved.toBuilder().time(null).build()).isResolved()).isFalse();
    assertThat(SenmlPack.of(resolved.toBuilder().time(-5).build()).isResolved()).isFalse();
    assertThat(SenmlPack.of(resolved.toBuilder().baseVersion(10).build()).isResolved()).isFalse();
    assertThat(SenmlPack.of(resolved.toBuilder().extension("bfoo", 1).build()).isResolved())
        .isFalse();
  }

  @Test
  void givenNonDefaultBaseVersion_whenCheckingResolved_thenRequiresItInEveryRecord() {
    SenmlRecord withVersion =
        SenmlRecord.builder().baseVersion(11).name("a").value(1).time(1.7e9).build();
    SenmlRecord withoutVersion = SenmlRecord.builder().name("b").value(1).time(1.7e9).build();

    assertThat(SenmlPack.of(withVersion, withVersion).isResolved()).isTrue();
    assertThat(SenmlPack.of(withVersion, withoutVersion).isResolved()).isFalse();
  }

  @Test
  void givenResolvedValidPack_whenResolving_thenReturnsSameInstance() {
    SenmlPack pack = RfcExamples.RESOLVED_DATA;

    assertThat(pack.resolve()).isSameAs(pack);
  }

  @Test
  void givenResolvedInvalidPack_whenResolving_thenStillValidates() {
    SenmlPack withoutName = SenmlPack.of(SenmlRecord.builder().value(1).time(1.7e9).build());
    SenmlPack withMandatory =
        SenmlPack.of(
            SenmlRecord.builder().name("a").value(1).time(1.7e9).extension("foo_", 1).build());

    assertThat(withoutName.isResolved()).isTrue();
    assertThatThrownBy(withoutName::resolve).isInstanceOf(SenmlViolationException.class);
    assertThatThrownBy(withMandatory::resolve).isInstanceOf(SenmlViolationException.class);
    assertThat(withMandatory.resolve(Set.of("foo_"))).isSameAs(withMandatory);
  }

  @Test
  void givenResolvedPack_whenSortingByTime_thenOrdersRecordsChronologically() {
    SenmlRecord late = SenmlRecord.builder().name("a").value(1).time(1.7e9 + 60).build();
    SenmlRecord early = SenmlRecord.builder().name("b").value(2).time(1.7e9).build();
    SenmlRecord middle = SenmlRecord.builder().name("c").value(3).time(1.7e9 + 30).build();

    SenmlPack sorted = SenmlPack.of(late, early, middle).sortedByTime();

    assertThat(sorted.getRecords()).containsExactly(early, middle, late);
    assertThat(sorted.isResolved()).isTrue();
  }

  @Test
  void givenResolvedPackWithEqualKeys_whenSorting_thenKeepsTheirOrder() {
    SenmlRecord first = SenmlRecord.builder().name("a").value(1).time(1.7e9 + 60).build();
    SenmlRecord second = SenmlRecord.builder().name("b").value(2).time(1.7e9).build();
    SenmlRecord third = SenmlRecord.builder().name("c").value(3).time(1.7e9).build();

    SenmlPack sorted = SenmlPack.of(first, second, third).sortedByTime();

    assertThat(sorted.getRecords()).containsExactly(second, third, first);
  }

  @Test
  void givenReceivedPack_whenResolvingAndSorting_thenSortsResolvedRecords() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder()
                .baseName("dev:")
                .baseTime(1.7e9)
                .name("a")
                .time(60)
                .value(1)
                .build(),
            SenmlRecord.builder().name("b").value(2).build());

    SenmlPack sorted = pack.resolve().sortedByTime();

    assertThat(sorted.getRecords())
        .extracting(SenmlRecord::getName, SenmlRecord::getTime)
        .containsExactly(tuple("dev:b", 1.7e9), tuple("dev:a", 1.7e9 + 60));
  }

  @Test
  void givenEmptyPack_whenSorting_thenReturnsEmptyPack() {
    assertThat(SenmlPack.empty().sortedByTime()).isEqualTo(SenmlPack.empty());
  }

  @Test
  void givenUnresolvedPack_whenSorting_thenThrows() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder().baseName("dev:").name("a").value(1).time(1.7e9).build(),
            SenmlRecord.builder().name("b").value(2).time(1.6e9).build());

    assertThatThrownBy(pack::sortedByTime).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> pack.sorted(SenmlRecord::getName))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void givenPackWithRelativeTime_whenSorting_thenThrows() {
    SenmlPack pack = SenmlPack.of(SenmlRecord.builder().name("a").value(1).time(-5).build());

    assertThatThrownBy(pack::sortedByTime).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void givenRfcResolvedExample_whenSortingWithoutResolving_thenSortsIt() {
    SenmlPack pack = RfcExamples.RESOLVED_DATA;

    SenmlPack sorted = pack.sorted(SenmlRecord::getUnit);

    assertThat(sorted.getRecords()).containsExactlyInAnyOrderElementsOf(pack.getRecords());
    assertThat(sorted.getRecords())
        .isSortedAccordingTo(
            Comparator.comparing(SenmlRecord::getUnit)
                .thenComparing(SenmlRecord::getTime, Comparator.naturalOrder()));
  }

  @Test
  void givenSameNonDefaultBaseVersionInEveryRecord_whenSorting_thenSortsPack() {
    SenmlRecord late = SenmlRecord.builder().baseVersion(11).name("a").value(1).time(1.8e9).build();
    SenmlRecord early =
        SenmlRecord.builder().baseVersion(11).name("b").value(2).time(1.7e9).build();

    assertThat(SenmlPack.of(late, early).sortedByTime().getRecords()).containsExactly(early, late);
  }

  static Stream<SenmlRecord> recordsNotInResolvedForm() {
    SenmlRecordBuilder resolved = SenmlRecord.builder().name("a").value(1).time(1.7e9);
    return Stream.of(
        resolved.build().toBuilder().time(null).build(),
        resolved.build().toBuilder().time(60).build(),
        resolved.build().toBuilder().baseVersion(10).build(),
        resolved.build().toBuilder().baseTime(1.7e9).build(),
        resolved.build().toBuilder().baseUnit("V").build(),
        resolved.build().toBuilder().baseValue(1).build(),
        resolved.build().toBuilder().baseSum(1).build(),
        resolved.build().toBuilder().extension("bfoo", 1).build());
  }

  @ParameterizedTest
  @MethodSource("recordsNotInResolvedForm")
  void givenRecordNotInResolvedForm_whenSorting_thenThrows(SenmlRecord record) {
    SenmlRecord other = SenmlRecord.builder().name("b").value(2).time(1.6e9).build();
    SenmlPack pack = SenmlPack.of(other, record);

    assertThatThrownBy(pack::sortedByTime).isInstanceOf(IllegalStateException.class);
    assertThat(pack.resolve(Instant.ofEpochSecond(1_700_000_000)).sortedByTime()).hasSize(2);
  }

  @Test
  void givenMixedBaseVersions_whenSorting_thenThrows() {
    SenmlPack pack =
        SenmlPack.of(
            SenmlRecord.builder().baseVersion(11).name("a").value(1).time(1.8e9).build(),
            SenmlRecord.builder().name("b").value(2).time(1.7e9).build());

    assertThatThrownBy(pack::sortedByTime).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void givenResolvedPackWithNullKeys_whenSortingByKey_thenPutsThemFirst() {
    SenmlRecord named = SenmlRecord.builder().name("a").value(1).time(1.7e9).build();
    SenmlRecord unnamed = SenmlRecord.builder().value(2).time(1.7e9).build();

    SenmlPack sorted = SenmlPack.of(named, unnamed).sorted(SenmlRecord::getName);

    assertThat(sorted.getRecords()).containsExactly(unnamed, named);
  }

  @Test
  @SuppressWarnings("NullAway")
  void givenNullKeyExtractor_whenSorting_thenThrows() {
    SenmlPack pack = RfcExamples.RESOLVED_DATA;

    assertThatThrownBy(() -> pack.sorted((Function<SenmlRecord, Double>) null))
        .isInstanceOf(NullPointerException.class);
  }
}
