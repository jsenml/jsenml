# JSenML

[![CI](https://github.com/jsenml/jsenml/actions/workflows/ci.yml/badge.svg)](https://github.com/jsenml/jsenml/actions/workflows/ci.yml)

SenML ([RFC 8428](https://www.rfc-editor.org/rfc/rfc8428)) for Java 17+: an immutable model of SenML packs and
records, JSON, CBOR and XML representations with Jackson 3, Jackson 2 and Gson backends, a Kotlin DSL and Spring Boot
integration.

Full documentation - modules, model, extensions, resolution, codecs, representations, Kotlin, Spring Boot and writing
backends - lives in [`docs/`](docs) and is published to the [wiki](https://github.com/jsenml/jsenml/wiki).

## Installation

Import the BOM and add a backend of each representation you need:

```kotlin
dependencies {
    implementation(platform("io.github.jsenml:jsenml-bom:<version>"))
    implementation("io.github.jsenml:jsenml-json-jackson")
}
```

See [Getting Started](docs/Getting-Started.md) for Maven and the other backends.

## Encode and decode

```java
SenmlPack pack =
    SenmlPack.of(
        SenmlRecord.builder()
            .baseName("urn:dev:ow:10e2073a01080063:")
            .name("voltage")
            .unit("V")
            .value(120.1)
            .build());

String json = SenmlJson.toJson(pack);
SenmlPack decoded = SenmlJson.fromJson(json);
```

CBOR and XML work the same way with `SenmlCbor` and `SenmlXml`.

## Resolve received packs

`resolve` applies base fields, converts relative times to absolute ones and validates every record:

```java
try {
    for (SenmlRecord record : SenmlJson.fromJson(json).resolve()) {
        store.save(record.getName(), record.getTime(), record.getValue());
    }
} catch (SenmlViolationException e) {
    log.warn("Rejected pack: {}", e.getViolations());
}
```

## Kotlin

```kotlin
val pack = buildSenmlPack {
    record {
        name("urn:dev:ow:10e2073a01080063:temp")
        unit("Cel")
        value(23.1)
    }
}
```

## Spring Boot

Add `jsenml-spring-boot-starter` and use packs in controllers:

```java
@PostMapping(path = "/packs", consumes = SenmlJsonCodec.MEDIA_TYPE)
void add(@RequestBody SenmlPack pack) {
    store.add(pack.resolve());
}
```

## Building

```shell
./gradlew  # spotlessApply build
```

Licensed under the [Apache License 2.0](LICENSE).
