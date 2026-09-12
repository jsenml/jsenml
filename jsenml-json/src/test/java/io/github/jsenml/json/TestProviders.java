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

package io.github.jsenml.json;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.json.spi.SenmlJsonCodecProvider;
import java.io.Reader;
import java.io.Writer;

public final class TestProviders {

  public static final class High implements SenmlJsonCodecProvider {

    @Override
    public String name() {
      return "high";
    }

    @Override
    public int getPriority() {
      return 10;
    }

    @Override
    public SenmlJsonCodec create() {
      return new NamedCodec(name());
    }
  }

  public static final class Low implements SenmlJsonCodecProvider {

    @Override
    public String name() {
      return "low";
    }

    @Override
    public SenmlJsonCodec create() {
      return new NamedCodec(name());
    }
  }

  /** Same name as {@link Low} with lower priority, so it must be shadowed by it. */
  public static final class LowDuplicate implements SenmlJsonCodecProvider {

    @Override
    public String name() {
      return "low";
    }

    @Override
    public int getPriority() {
      return -1;
    }

    @Override
    public SenmlJsonCodec create() {
      throw new AssertionError("shadowed provider must not be used");
    }
  }

  record NamedCodec(String name) implements SenmlJsonCodec {

    @Override
    public String encode(SenmlPack pack) {
      return name;
    }

    @Override
    public void encode(SenmlPack pack, Writer writer) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SenmlPack decode(String json) {
      return SenmlPack.of(SenmlRecord.builder().name(name).stringValue(json).build());
    }

    @Override
    public SenmlPack decode(Reader reader) {
      throw new UnsupportedOperationException();
    }
  }

  private TestProviders() {}
}
