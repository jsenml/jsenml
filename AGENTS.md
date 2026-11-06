# `jsenml` - Agent Instructions

SenML (RFC 8428) library for Java. Framework-agnostic model with a module per representation (JSON, CBOR), each
with pluggable backends discovered via `java.util.ServiceLoader`.

## Design Rules

- `jsenml-api` must stay free of serialization dependencies (only `org.jspecify`).
- One module per SenML content type (`jsenml-json`, `jsenml-cbor`), with its own codec interface, provider
  SPI (`io.github.jsenml.<format>.spi`) and entry point (`SenmlJson`, ...). Never add a format switch/parameter to an
  existing codec, never put format-specific API into `jsenml-api`.
- Backends must produce identical output. Shared behavior is tested in `AbstractSenml<Format>CodecTest` (test fixtures
  of the format module), RFC 8428 examples are in `RfcExamples` (`jsenml-api` test fixtures) - extend them instead of
  duplicating tests per backend.
- New published modules go to `jsenml-bom`.
- No Java records in public API.

## Documentation

- `README.md` stays short: overview, installation and a few minimal use cases.
- Full documentation is in `docs/`, published to the GitHub wiki (`.github/workflows/wiki.yml`). Update the affected
  pages in the same change as the behavior they describe; link pages as `./Page.md` and files outside `docs/` with
  absolute GitHub URLs.

## Build

```shell
./gradlew  # default: spotlessApply build (preferred)
```

Let `spotlessApply` handle all formatting - never format manually.

## Coding Rules

- Always use braces for `if`/`for`/`while`, even for single statements.
- No self-explaining comments - only add comments for non-obvious context.

## Test Conventions

- No `// given`, `// when`, `// then` section comments.
- Cover both positive and negative cases.
