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

package io.github.jsenml.json.jackson2;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import java.io.Serial;

/**
 * Jackson 2 module handling SenML JSON representation of {@link SenmlPack} and {@link SenmlRecord}.
 *
 * <pre>{@code
 * JsonMapper mapper = JsonMapper.builder().addModule(new SenmlJsonJacksonModule()).build();
 * SenmlPack pack = mapper.readValue(json, SenmlPack.class);
 * }</pre>
 *
 * <p>The module is also registered as a {@code com.fasterxml.jackson.databind.Module} service, so
 * it is picked up by {@code MapperBuilder.findAndAddModules()}.
 */
public final class SenmlJsonJacksonModule extends SimpleModule {

  @Serial //
  private static final long serialVersionUID = 1L;

  /** Creates the module. */
  public SenmlJsonJacksonModule() {
    super(SenmlJsonJacksonModule.class.getSimpleName());

    SenmlJsonRecordSerializer recordSerializer = new SenmlJsonRecordSerializer();
    SenmlJsonRecordDeserializer recordDeserializer = new SenmlJsonRecordDeserializer();

    addSerializer(SenmlRecord.class, recordSerializer);
    addSerializer(SenmlPack.class, new SenmlJsonPackSerializer(recordSerializer));
    addDeserializer(SenmlRecord.class, recordDeserializer);
    addDeserializer(SenmlPack.class, new SenmlJsonPackDeserializer(recordDeserializer));
  }
}
