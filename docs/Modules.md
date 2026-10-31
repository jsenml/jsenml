# Modules

Every module is published under the `io.github.jsenml` group, with versions aligned by `jsenml-bom`. Library modules
are JPMS modules (`module-info.java`); the Kotlin and Spring Boot modules declare an `Automatic-Module-Name`.

## BOM

- `jsenml-bom` - Maven BOM (Gradle platform) aligning versions of all modules below.

## Core

Library-independent modules, without serialization dependencies. They are added by backends, so you rarely declare them
yourself.

- `jsenml-api` (`io.github.jsenml`) - the model: `SenmlPack`, `SenmlRecord`, `SenmlExtensions` with their builders,
  resolution (`SenmlPack.resolve`) and violations (`SenmlViolation`, `SenmlViolationException`), text labels and number
  formatting (`SenmlTextFormat`) and the base backend SPI (`SenmlCodecProvider`). Depends on JSpecify only.
- `jsenml-json` (`io.github.jsenml.json`) - SenML JSON: `SenmlJsonCodec`, its SPI (`SenmlJsonCodecProvider`) and the
  `SenmlJson` entry point.
- `jsenml-cbor` (`io.github.jsenml.cbor`) - SenML CBOR: `SenmlCborCodec`, its SPI, integer labels (`SenmlCborFormat`)
  and the `SenmlCbor` entry point.
- `jsenml-xml` (`io.github.jsenml.xml`) - SenML XML: `SenmlXmlCodec`, its SPI and the `SenmlXml` entry point.
- `jsenml-kotlin` (`io.github.jsenml.kotlin`) - Kotlin DSL (`buildSenmlPack`), `SenmlRecord.copy`,
  `SenmlExtensions.copy` and conversions from collections and sequences. See [Kotlin](./Kotlin.md).

The format modules publish shared contract tests in their test fixtures (`AbstractSenmlJsonCodecTest`, ...), see
[Writing a Backend](./Writing-a-Backend.md).

## Jackson 3

Backends on Jackson 3 (`tools.jackson`), registered as provider `jackson` with the highest priority, so they are the
default when several backends are present.

- `jsenml-json-jackson` (`io.github.jsenml.json.jackson`) - `JacksonSenmlJsonCodec` and `SenmlJsonJacksonModule`, also
  discovered by `findAndAddModules()`.
- `jsenml-cbor-jackson` (`io.github.jsenml.cbor.jackson`) - `JacksonSenmlCborCodec` and `SenmlCborJacksonModule`.
- `jsenml-xml-jackson` (`io.github.jsenml.xml.jackson`) - `JacksonSenmlXmlCodec` and `SenmlXmlJacksonModule`.

## Jackson 2

Backends on Jackson 2 (`com.fasterxml.jackson`) for applications not yet on Jackson 3, registered as provider
`jackson2`. They produce the same output as the Jackson 3 backends and share class names with them, in
`io.github.jsenml.<format>.jackson2` packages.

- `jsenml-json-jackson2` (`io.github.jsenml.json.jackson2`) - `JacksonSenmlJsonCodec` and `SenmlJsonJacksonModule`,
  also discovered by `findAndAddModules()`.
- `jsenml-cbor-jackson2` (`io.github.jsenml.cbor.jackson2`) - `JacksonSenmlCborCodec` and `SenmlCborJacksonModule`.
- `jsenml-xml-jackson2` (`io.github.jsenml.xml.jackson2`) - `JacksonSenmlXmlCodec` and `SenmlXmlJacksonModule`.

## Gson

- `jsenml-json-gson` (`io.github.jsenml.json.gson`) - `GsonSenmlJsonCodec` and `SenmlTypeAdapterFactory`, registered
  as provider `gson` with lower priority than the Jackson backends. Gson has no CBOR or XML support.

## Spring Boot

Spring Boot 4 integration, see [Spring Boot](./Spring-Boot.md).

- `jsenml-spring-boot-autoconfigure` (`io.github.jsenml.spring.boot.autoconfigure`) - autoconfiguration of Boot's
  Jackson 3 mappers, `Gson`, HTTP message converters and springdoc-openapi schemas.
- `jsenml-spring-boot-starter` (`io.github.jsenml.spring.boot.starter`) - autoconfiguration, `jsenml-json-jackson`
  and `spring-boot-starter-jackson`; other backends are optional and picked up at runtime.
