# Jackson 2 integration for JSenML

Jackson 2 (`com.fasterxml.jackson`) backends for applications not yet on Jackson 3, registered as provider
`jackson2`. They produce the same output as the Jackson 3 backends and share class names with them, in
`io.github.jsenml.<format>.jackson2` packages.

When both Jackson versions are on the classpath, Jackson 3 is the default; select Jackson 2 with
`SenmlJson.jsonCodec("jackson2")`. Codecs created with your own mapper work on a copy of it. See
[Codecs and Backends](../docs/Codecs-and-Backends.md).

## `jsenml-json-jackson2`

`JacksonSenmlJsonCodec` and `SenmlJsonJacksonModule`, also discovered by `findAndAddModules()`.

## `jsenml-cbor-jackson2`

`JacksonSenmlCborCodec` and `SenmlCborJacksonModule`, to register on CBOR mappers only.
