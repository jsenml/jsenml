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

package io.github.jsenml.cbor;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.github.jsenml.cbor.spi.SenmlCborCodecProvider;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class TestProviders {

  public static final class CborHigh implements SenmlCborCodecProvider {

    @Override
    public String name() {
      return "cbor-high";
    }

    @Override
    public int getPriority() {
      return 10;
    }

    @Override
    public SenmlCborCodec create() {
      return new NamedCborCodec(name());
    }
  }

  public static final class CborLow implements SenmlCborCodecProvider {

    @Override
    public String name() {
      return "cbor-low";
    }

    @Override
    public SenmlCborCodec create() {
      return new NamedCborCodec(name());
    }
  }

  record NamedCborCodec(String name) implements SenmlCborCodec {

    @Override
    public byte[] encode(SenmlPack pack) {
      return name.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void encode(SenmlPack pack, OutputStream output) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SenmlPack decode(byte[] cbor) {
      return SenmlPack.of(
          SenmlRecord.builder()
              .name(name)
              .stringValue(new String(cbor, StandardCharsets.UTF_8))
              .build());
    }

    @Override
    public SenmlPack decode(InputStream input) {
      throw new UnsupportedOperationException();
    }
  }

  private TestProviders() {}
}
