# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][keepachangelog], and this project adheres to [Semantic Versioning][semver].

## [Unreleased]

### Added

- Add SenML ([RFC 8428](https://www.rfc-editor.org/rfc/rfc8428)) model - `SenmlPack`, `SenmlRecord` and
  `SenmlExtensions` with their builders.
- Add resolution of packs (`SenmlPack.resolve`) with validation of records (`SenmlViolationException`).
- Add `jsenml-json` with `SenmlJson` entry point for SenML JSON (`application/senml+json`).
- Add `jsenml-cbor` with `SenmlCbor` entry point for SenML CBOR (`application/senml+cbor`).
- Add discovery of codec backends via `java.util.ServiceLoader`, with a provider SPI per representation.
- Add Jackson 3 backends - `jsenml-json-jackson` and `jsenml-cbor-jackson`, with Jackson modules.
- Add Jackson 2 backends - `jsenml-json-jackson2` and `jsenml-cbor-jackson2`, with Jackson modules.
- Add Gson backend - `jsenml-json-gson`, with `SenmlTypeAdapterFactory`.
- Add `jsenml-kotlin` with Kotlin DSL for building packs and `copy` functions for records and extensions.
- Add `jsenml-spring-boot-autoconfigure` with Spring Boot auto-configuration of Jackson mappers and Gson.
- Add `application/senml+cbor` HTTP message converter for Spring MVC and `RestClient`.
- Add SenML schemas to `springdoc-openapi` documents.
- Add registration of auto-configuration in Spring Boot test slices.
- Add `jsenml-spring-boot-starter`.
- Add `jsenml-bom`.

[keepachangelog]: https://keepachangelog.com/en/1.1.0/

[semver]: https://semver.org/spec/v2.0.0.html
