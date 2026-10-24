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

package io.github.jsenml.spring.boot.autoconfigure.springdoc;

import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;

/**
 * Swagger {@link ModelConverter} describing {@link SenmlPack} and {@link SenmlRecord} in their
 * SenML JSON representation (e.g. {@code "bn"} instead of {@code "baseName"}), as defined in <a
 * href="https://www.rfc-editor.org/rfc/rfc8428#section-4">RFC 8428, Section 4</a>.
 *
 * <p>The default implementation defines both types as reusable components, named {@value
 * #RECORD_SCHEMA_NAME} and {@value #PACK_SCHEMA_NAME}, references them wherever they are used and
 * passes other types down the converter chain. Define a bean of this type to replace it.
 */
public interface SenmlModelConverter extends ModelConverter {

  /** Name of the component schema describing {@link SenmlRecord}. */
  String RECORD_SCHEMA_NAME = "SenmlRecord";

  /** Name of the component schema describing {@link SenmlPack}. */
  String PACK_SCHEMA_NAME = "SenmlPack";

  /**
   * Resolves {@link SenmlPack} and {@link SenmlRecord} to schemas of their SenML JSON
   * representation. Other types are resolved by the next converter in the chain.
   *
   * @param type type to resolve
   * @param context context to define component schemas in
   * @param chain remaining converters
   * @return resolved schema, or {@code null} if no converter can resolve given type
   */
  @Override
  @Nullable Schema<?> resolve(
      AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain);
}
