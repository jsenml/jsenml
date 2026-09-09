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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.junit.jupiter.api.Test;

class SerializationTest {

  private static final SenmlRecord RECORD =
      SenmlRecord.builder()
          .baseName("urn:dev:ow:10e2073a01080063:")
          .baseTime(1.320067464e9)
          .baseUnit("V")
          .baseValue(1)
          .baseSum(2)
          .baseVersion(10)
          .name("voltage")
          .unit("A")
          .time(-1.25)
          .updateTime(60)
          .value(120.1)
          .sum(3.5)
          .extension("foo", "bar")
          .extension("accuracy", 0.1)
          .extension("ok", true)
          .extension("raw", new byte[] {1, 2, 3})
          .build();

  private static final SenmlPack PACK =
      SenmlPack.of(
          RECORD,
          SenmlRecord.builder().name("label").stringValue("żółć").build(),
          SenmlRecord.builder().name("open").booleanValue(false).build(),
          SenmlRecord.builder().name("blob").dataValue(new byte[] {1, 2, 3}).build());

  @Test
  void givenRecord_whenSerializingAndDeserializing_thenEqualsOriginal() throws Exception {
    assertThat(roundTrip(RECORD)).isEqualTo(RECORD);
  }

  @Test
  void givenPack_whenSerializingAndDeserializing_thenEqualsOriginal() throws Exception {
    assertThat(roundTrip(PACK)).isEqualTo(PACK);
  }

  @Test
  void givenExtensions_whenSerializingAndDeserializing_thenEqualsOriginalWithOwnBytes()
      throws Exception {
    SenmlExtensions deserialized = roundTrip(RECORD.getExtensions());

    assertThat(deserialized).isEqualTo(RECORD.getExtensions());
    assertThat(deserialized.names()).containsExactly("foo", "accuracy", "ok", "raw");
  }

  @Test
  void givenEmptyPackAndExtensions_whenSerializingAndDeserializing_thenEqualsEmpty()
      throws Exception {
    assertThat(roundTrip(SenmlPack.empty())).isEqualTo(SenmlPack.empty());
    assertThat(roundTrip(SenmlExtensions.empty())).isEqualTo(SenmlExtensions.empty());
  }

  @Test
  void givenBuilders_whenSerializingAndDeserializing_thenBuildEqualObjects() throws Exception {
    SenmlRecordBuilder recordBuilder = RECORD.toBuilder();
    SenmlPackBuilder packBuilder = SenmlPack.builder().addAll(PACK);
    SenmlExtensionsBuilder extensionsBuilder = RECORD.getExtensions().toBuilder();

    assertThat(roundTrip(recordBuilder).build()).isEqualTo(RECORD);
    assertThat(roundTrip(packBuilder).build()).isEqualTo(PACK);
    assertThat(roundTrip(extensionsBuilder).build()).isEqualTo(RECORD.getExtensions());
  }

  @Test
  void givenDeserializedBuilder_whenModifying_thenOriginalIsUnchanged() throws Exception {
    SenmlRecordBuilder original = SenmlRecord.builder().name("a").value(1);

    roundTrip(original).value(2);

    assertThat(original.build().getValue()).isEqualTo(1.0);
  }

  @Test
  void givenResolvedPack_whenSerializingAndDeserializing_thenStaysResolved() throws Exception {
    assertThat(roundTrip(RfcExamples.RESOLVED_DATA).isResolved()).isTrue();
    assertThat(roundTrip(RfcExamples.MULTIPLE_MEASUREMENTS).isResolved()).isFalse();
  }

  @SuppressWarnings("unchecked")
  private static <T> T roundTrip(T object) throws Exception {
    assertThat(object).isInstanceOf(Serializable.class);
    return (T) deserialize(serialize((Serializable) object));
  }

  private static byte[] serialize(Serializable object) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
      output.writeObject(object);
    }
    return bytes.toByteArray();
  }

  private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
    try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return input.readObject();
    }
  }
}
