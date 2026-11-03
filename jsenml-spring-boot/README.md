# Spring Boot integration for JSenML

Spring Boot 4 integration: SenML support in Boot's mappers, SenML media types in HTTP bodies and SenML schemas in
springdoc-openapi documents. See [Spring Boot](../docs/Spring-Boot.md) for Spring MVC, WebFlux and configuration
details.

## `jsenml-spring-boot-autoconfigure`

Autoconfiguration, each part active only when its backend is on the classpath and disabled with
`jsenml.enabled=false`:

- SenML support in Boot's Jackson 3 `JsonMapper`, `CBORMapper` and `XmlMapper` and in `Gson`;
- an `application/senml+cbor` HTTP message converter for Spring MVC and `RestClient`, disabled with
  `jsenml.converter.cbor.enabled=false`;
- SenML schemas (JSON and XML) in `springdoc-openapi` documents.

Every part registers a customizer bean that a bean of the same type replaces. Boot's Jackson 2 support is deprecated
for removal and is not autoconfigured.

## `jsenml-spring-boot-starter`

Autoconfiguration, the Jackson 3 JSON backend (`jsenml-json-jackson`), the Kotlin DSL (`jsenml-kotlin`) and
`spring-boot-starter-jackson`. Everything else is optional and picked up at runtime when on the classpath: add
`jsenml-cbor-jackson` or `jsenml-xml-jackson` for CBOR or XML, `jsenml-json-gson` for Boot's `Gson`.
