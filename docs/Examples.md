# Examples

The [`examples/`][examples] folder holds runnable Kotlin applications. They are not published and use the root
package `com.example`.

## Consumers

[`example-webmvc`][example-webmvc] (Spring MVC) and [`example-webflux`][example-webflux] (Spring WebFlux, suspending
handlers) expose the same API on port 8080:

- `POST /packs` - accepts a pack in SenML JSON, CBOR or XML, each with its own handler, and stores its resolved records;
  a pack breaking SenML rules is rejected with `400 Bad Request` and a Problem Details body;
- `GET /packs` - returns all stored records in the representation requested by `Accept`, SenML JSON by default;
- `DELETE /packs` - clears the stored records;
- `/` - redirects to Swagger UI, which shows the SenML schemas.

```shell
./gradlew :examples:example-webmvc:bootRun
./gradlew :examples:example-webflux:bootRun
```

Both listen on port 8080, so run one at a time. `example-webflux` also shows the `CodecCustomizer` needed for SenML CBOR
and XML in WebFlux, see [Spring Boot](./Spring-Boot.md).

## Producer

[`example-producer`][example-producer] sends packs of random measurements of a simulated device, built with the Kotlin
DSL:

```shell
./gradlew :examples:example-producer:runJson
./gradlew :examples:example-producer:runCbor
./gradlew :examples:example-producer:runXml
./gradlew :examples:example-producer:run --args="xml --count=20 --interval-ms=500 --url=http://localhost:8080/packs"
```

Arguments are the representation (`json`, `cbor` or `xml`) followed by optional `--url` (default
`http://localhost:8080/packs`), `--count` (default 10) and `--interval-ms` (default 1000). The `runJson`, `runCbor` and
`runXml` tasks send a single pack each; change their arguments in the producer's `build.gradle.kts`.

## IntelliJ IDEA

Importing the project creates `Run [example-webmvc]` and `Run [example-webflux]` run configurations.

[examples]: https://github.com/jsenml/jsenml/tree/main/examples
[example-webmvc]: https://github.com/jsenml/jsenml/tree/main/examples/example-webmvc
[example-webflux]: https://github.com/jsenml/jsenml/tree/main/examples/example-webflux
[example-producer]: https://github.com/jsenml/jsenml/tree/main/examples/example-producer
