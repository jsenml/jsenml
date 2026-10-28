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

package io.github.jsenml.spring.boot.autoconfigure;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.cbor.SenmlCborCodec;
import io.github.jsenml.json.SenmlJsonCodec;
import io.github.jsenml.xml.SenmlXmlCodec;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class SenmlPackController {

  public static final String PATH = "/packs";

  SenmlPackController() {}

  @PostMapping(
      path = PATH,
      consumes = {SenmlJsonCodec.MEDIA_TYPE, SenmlCborCodec.MEDIA_TYPE, SenmlXmlCodec.MEDIA_TYPE},
      produces = {SenmlJsonCodec.MEDIA_TYPE, SenmlCborCodec.MEDIA_TYPE, SenmlXmlCodec.MEDIA_TYPE})
  SenmlPack echo(@RequestBody SenmlPack pack) {
    return pack;
  }
}
