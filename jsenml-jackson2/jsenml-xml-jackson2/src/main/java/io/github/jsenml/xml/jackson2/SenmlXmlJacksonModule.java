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

package io.github.jsenml.xml.jackson2;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import java.io.Serial;

/**
 * Jackson 2 module handling SenML XML representation of {@link SenmlPack} and {@link SenmlRecord}.
 *
 * <pre>{@code
 * XmlMapper mapper = XmlMapper.builder().addModule(new SenmlXmlJacksonModule()).build();
 * SenmlPack pack = mapper.readValue(xml, SenmlPack.class);
 * }</pre>
 *
 * <p>Register it on XML mappers only. Unlike the JSON module, it is deliberately not registered as
 * a {@code com.fasterxml.jackson.databind.Module} service, so {@code findAndAddModules()} never
 * applies XML serializers to a JSON mapper.
 */
public final class SenmlXmlJacksonModule extends SimpleModule {

  @Serial //
  private static final long serialVersionUID = 1L;

  /** Creates the module. */
  public SenmlXmlJacksonModule() {
    super(SenmlXmlJacksonModule.class.getSimpleName());

    SenmlXmlRecordSerializer recordSerializer = new SenmlXmlRecordSerializer();
    SenmlXmlRecordDeserializer recordDeserializer = new SenmlXmlRecordDeserializer();

    addSerializer(SenmlRecord.class, recordSerializer);
    addSerializer(SenmlPack.class, new SenmlXmlPackSerializer(recordSerializer));
    addDeserializer(SenmlRecord.class, recordDeserializer);
    addDeserializer(SenmlPack.class, new SenmlXmlPackDeserializer(recordDeserializer));
  }
}
