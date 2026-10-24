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

package io.github.jsenml.cbor.jackson2;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import java.io.Serial;

/**
 * Jackson 2 module handling SenML CBOR representation of {@link SenmlPack} and {@link SenmlRecord}.
 *
 * <pre>{@code
 * CBORMapper mapper = CBORMapper.builder().addModule(new SenmlCborJacksonModule()).build();
 * SenmlPack pack = mapper.readValue(cbor, SenmlPack.class);
 * }</pre>
 *
 * <p>Register it on CBOR mappers only. Unlike the JSON module, it is deliberately not registered as
 * a {@code com.fasterxml.jackson.databind.Module} service, so {@code findAndAddModules()} never
 * applies CBOR serializers (integer keys, byte strings) to a JSON mapper.
 */
public final class SenmlCborJacksonModule extends SimpleModule {

  @Serial //
  private static final long serialVersionUID = 1L;

  /** Creates the module. */
  public SenmlCborJacksonModule() {
    super(SenmlCborJacksonModule.class.getSimpleName());

    SenmlCborRecordSerializer recordSerializer = new SenmlCborRecordSerializer();
    SenmlCborRecordDeserializer recordDeserializer = new SenmlCborRecordDeserializer();

    addSerializer(SenmlRecord.class, recordSerializer);
    addSerializer(SenmlPack.class, new SenmlCborPackSerializer(recordSerializer));
    addDeserializer(SenmlRecord.class, recordDeserializer);
    addDeserializer(SenmlPack.class, new SenmlCborPackDeserializer(recordDeserializer));
  }
}
