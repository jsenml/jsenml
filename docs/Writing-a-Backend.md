# Writing a Backend

A backend implements the codec of one representation on top of a serialization library. Backends live in this
repository, grouped by library (`jsenml-jackson/`, `jsenml-gson/`, ...), one module per representation, named
`jsenml-<format>-<library>`.

## Provider

Implement the provider SPI of the representation:

- `io.github.jsenml.json.spi.SenmlJsonCodecProvider` - creates `SenmlJsonCodec`s;
- `io.github.jsenml.cbor.spi.SenmlCborCodecProvider` - creates `SenmlCborCodec`s;
- `io.github.jsenml.xml.spi.SenmlXmlCodecProvider` - creates `SenmlXmlCodec`s.

All of them extend `io.github.jsenml.SenmlCodecProvider`:

- `name()` - a unique, stable name used to select the backend, e.g. `"jackson"`;
- `getPriority()` - the backend with the highest priority is the default, ties broken by name; 0 by default. Existing
  backends use 5000 (`jackson`), 4000 (`jackson2`) and 1000 (`gson`);
- `create()` - a new codec.

Providers need a public no-arg constructor. Register them for both the class path and the module path:

```text
# src/main/resources/META-INF/services/io.github.jsenml.json.spi.SenmlJsonCodecProvider
io.github.jsenml.json.mylib.MyLibSenmlJsonCodecProvider
```

```java
module io.github.jsenml.json.mylib {
    requires transitive io.github.jsenml.json;

    exports io.github.jsenml.json.mylib;

    provides io.github.jsenml.json.spi.SenmlJsonCodecProvider with
        io.github.jsenml.json.mylib.MyLibSenmlJsonCodecProvider;
}
```

## Codec

- Codecs must be thread-safe.
- Encode with the shared text format, so output is identical across backends: labels and number formatting from
  `SenmlTextFormat` (`formatNumber`, `formatBytes`), integer labels from `SenmlCborFormat`.
- Follow the decoding rules of the representation for unknown fields and mandatory extensions, see
  [Representations](./Representations.md) and [Extensions](./Extensions.md).
- Throw `SenmlCodecException` for malformed input and I/O failures.
- XML codecs must not resolve external entities.
- Offer a constructor taking the library's own configured instance (mapper, `Gson`, ...) without modifying it, as
  existing backends do.

## Contract tests

Each format module publishes shared contract tests in its test fixtures, built from the
[RFC 8428 examples][rfc-examples] and edge cases. Extend the abstract test and return your codec:

```java
class MyLibSenmlJsonCodecTest extends AbstractSenmlJsonCodecTest {

    @Override
    protected SenmlJsonCodec codec() {
        return new MyLibSenmlJsonCodec();
    }
}
```

```kotlin
dependencies {
    testImplementation(testFixtures(project(":jsenml-core:jsenml-json")))
}
```

The CBOR and XML counterparts are `AbstractSenmlCborCodecTest` and `AbstractSenmlXmlCodecTest`. Test fixtures are not
published, so they are available to modules of this repository only. Add backend-specific tests next to them, but put
behavior every backend shares into the contract tests instead of duplicating it.

## Wiring the module

A new published module also needs:

- an `include` in `settings.gradle.kts`;
- a constraint in `jsenml-bom/build.gradle.kts`;
- `nmcpAggregation` and `kover` entries in the root `build.gradle.kts`;
- a JUnit run configuration in `build-logic/src/main/kotlin/internal.idea-convention.gradle.kts`;
- a mention in [Modules](./Modules.md).

[rfc-examples]: https://github.com/damianmalczewski/jsenml/blob/main/jsenml-core/jsenml-api/src/testFixtures/java/io/github/jsenml/RfcExamples.java
