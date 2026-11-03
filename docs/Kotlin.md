# Kotlin

`jsenml-kotlin` adds a DSL for building packs, `copy` functions for records and extensions, and conversions from Kotlin
collections and sequences. It works with every backend: it only builds the model from `jsenml-api`.

```kotlin
dependencies {
    implementation(platform("io.github.jsenml:jsenml-bom:<version>"))
    implementation("io.github.jsenml:jsenml-json-jackson")
    implementation("io.github.jsenml:jsenml-kotlin")
}
```

The module targets Kotlin 2.2 (API and language version) and does not declare `kotlin-stdlib` as a dependency: your
Kotlin application brings its own standard library, version 2.2 or newer.

## Building packs

`buildSenmlPack` is the entry point of the DSL. Records are added with `record { }`, extensions of a record with
`extensions { }`:

```kotlin
val pack = buildSenmlPack {
    record {
        baseName("urn:dev:ow:10e2073a01080063:")
        name("voltage")
        unit("V")
        value(120.1)
    }
    record {
        name("current")
        unit("A")
        value(1.2)
        extensions {
            put("accuracy", 0.01)
            put("calibrated", true)
        }
    }
}
```

- Every setter of the Java builder is available, without chaining: each call in the block is applied.
- Numeric setters take any `Number` (`Int`, `Long`, `Float`, ...), converted to `Double` as described in
  [Model](./Model.md); `null` clears a field.
- `extension(name, value)` and `removeExtension(name)` set single extensions, `extensions { }` replaces all of them.
- Already built records are added with `record(record)` and `records(iterable)`.
- The DSL is marked with `@DslMarker`, so an inner block cannot call the outer one's functions by accident (e.g.
  `record { }` inside `extensions { }`).

Records exist only as part of a pack, so there is no separate builder function for them. Use the Java builder
(`SenmlRecord.builder()`) or `copy` when you need a record on its own.

## Copies

`copy` returns a modified copy; fields the block does not touch keep their values:

```kotlin
val updated = pack.first.copy { value(121.0) }

val extensions = updated.extensions.copy {
    put("accuracy", 0.02)
    remove("calibrated")
}
```

## Conversions

Packs are `Iterable<SenmlRecord>`, so the standard library works on them, and the results convert back:

```kotlin
val voltages = pack.asSequence().filter { it.unit == "V" }.toSenmlPack()
val chronological = pack.resolve().sortedByTime()
val two = senmlPackOf(pack.first, pack.last)
```

- `Iterable<SenmlRecord>.toSenmlPack()` - in iteration order;
- `Sequence<SenmlRecord>.toSenmlPack()` - in sequence order, consuming the sequence;
- `senmlPackOf(vararg records)` - from given records.

Java getters read as Kotlin properties: `pack.first`, `record.name`, `record.extensions`.

## Nullness

The Java API is annotated with JSpecify, so Kotlin sees nullable types where fields are optional (`record.value` is
`Double?`) and non-null types elsewhere.
