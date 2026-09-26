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

package io.github.jsenml.xml;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.xml.spi.SenmlXmlCodecProvider;
import java.io.Reader;
import java.io.Writer;

public final class TestProviders {

  public static final class XmlOnly implements SenmlXmlCodecProvider {

    @Override
    public String name() {
      return "xml-only";
    }

    @Override
    public SenmlXmlCodec create() {
      return new NamedXmlCodec(name());
    }
  }

  record NamedXmlCodec(String name) implements SenmlXmlCodec {

    @Override
    public String encode(SenmlPack pack) {
      return name;
    }

    @Override
    public void encode(SenmlPack pack, Writer writer) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SenmlPack decode(String xml) {
      return SenmlPack.of(SenmlRecord.builder().name(name).stringValue(xml).build());
    }

    @Override
    public SenmlPack decode(Reader reader) {
      throw new UnsupportedOperationException();
    }
  }

  private TestProviders() {}
}
