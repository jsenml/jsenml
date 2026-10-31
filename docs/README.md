# JSenML

SenML ([RFC 8428][rfc8428]) for Java 17+: an immutable, framework-agnostic model of SenML packs and records, with one
module per representation - JSON, CBOR and XML - and pluggable backends (Jackson 3, Jackson 2, Gson) discovered via
`java.util.ServiceLoader`. Kotlin DSL and Spring Boot integration included.

> These pages live in the [`docs/`][docs-folder] folder of the repository and are published to the wiki on every push
> to `main`. Edit them there - changes made directly in the wiki are overwritten on the next publish.

## Contents

**Using JSenML**

- [Getting Started](./Getting-Started.md) - dependencies, choosing backends, first encode and decode.
- [Modules](./Modules.md) - every published module, what it contains and its JPMS module name.
- [Model](./Model.md) - packs, records, fields, builders and immutability.
- [Extensions](./Extensions.md) - fields not defined by RFC 8428, including ones mandatory to understand.
- [Resolution](./Resolution.md) - base fields, relative times, validation and violations.
- [Codecs and Backends](./Codecs-and-Backends.md) - entry points, backend selection, own mappers, Jackson and Gson
  integration.
- [Representations](./Representations.md) - JSON, CBOR and XML on the wire.

**Integrations**

- [Kotlin](./Kotlin.md) - DSL, copies and conversions from collections and sequences.
- [Spring Boot](./Spring-Boot.md) - starter, autoconfiguration, HTTP bodies in Spring MVC and WebFlux, springdoc.

**Contributing**

- [Writing a Backend](./Writing-a-Backend.md) - provider SPI and shared contract tests.
- [Examples](./Examples.md) - Spring MVC and WebFlux applications and a producer sending random measurements.
- [Development](./Development.md) - building, conventions, CI and these docs.

## At a glance

- **Java:** 17+, JPMS modules, JSpecify nullness annotations
- **Representations:** `application/senml+json`, `application/senml+cbor`, `application/senml+xml`
- **Backends:** Jackson 3 (`tools.jackson`), Jackson 2 (`com.fasterxml.jackson`), Gson (JSON only)
- **Kotlin:** DSL for building packs, `copy` for records and extensions
- **Spring Boot:** 4.x starter (Jackson 3, Gson optional), springdoc-openapi schemas
- **License:** Apache 2.0

[rfc8428]: https://www.rfc-editor.org/rfc/rfc8428
[docs-folder]: https://github.com/damianmalczewski/jsenml/tree/main/docs
