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

package io.github.jsenml.json.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import io.github.jsenml.SenmlPack;
import io.github.jsenml.SenmlRecord;
import org.jspecify.annotations.Nullable;

/**
 * Gson {@link TypeAdapterFactory} handling SenML JSON representation of {@link SenmlPack} and
 * {@link SenmlRecord}.
 *
 * <pre>{@code
 * Gson gson = new GsonBuilder().registerTypeAdapterFactory(new SenmlTypeAdapterFactory()).create();
 * SenmlPack pack = gson.fromJson(json, SenmlPack.class);
 * }</pre>
 */
public final class SenmlTypeAdapterFactory implements TypeAdapterFactory {

  private final TypeAdapter<SenmlRecord> recordAdapter = new SenmlRecordTypeAdapter();
  private final TypeAdapter<SenmlPack> packAdapter = new SenmlPackTypeAdapter(recordAdapter);

  /**
   * Returns a null-safe SenML type adapter for {@link SenmlPack} and {@link SenmlRecord}, or {@code
   * null} for any other type, letting Gson consult other factories.
   *
   * @param gson Gson instance requesting the adapter
   * @param type requested type
   * @param <T> requested type
   * @return SenML type adapter, or {@code null} for unsupported types
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> @Nullable TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Class<? super T> rawType = type.getRawType();
    if (SenmlPack.class.isAssignableFrom(rawType)) {
      return (TypeAdapter<T>) packAdapter.nullSafe();
    }
    if (SenmlRecord.class.isAssignableFrom(rawType)) {
      return (TypeAdapter<T>) recordAdapter.nullSafe();
    }
    return null;
  }
}
