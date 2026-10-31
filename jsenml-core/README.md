# JSenML Core

Library-independent core of JSenML: the SenML model and one API module per representation. These modules have no
serialization dependencies; add a backend (`jsenml-jackson`, `jsenml-jackson2`, `jsenml-gson`) to encode and decode.

Entry points discover backends with `java.util.ServiceLoader`; a representation whose module is not on the classpath
cannot be used, as its entry point does not compile.

## `jsenml-api`

The model: `SenmlPack`, `SenmlRecord` and `SenmlExtensions` with their builders, resolution (`SenmlPack.resolve`) and
violations, text labels and number formatting (`SenmlTextFormat`) and the base backend SPI (`SenmlCodecProvider`).
See [Model](../docs/Model.md), [Extensions](../docs/Extensions.md) and [Resolution](../docs/Resolution.md).

## `jsenml-json`

SenML JSON (`application/senml+json`): `SenmlJsonCodec`, its SPI and the `SenmlJson` entry point. Contract tests for
backends are in its test fixtures (`AbstractSenmlJsonCodecTest`).

## `jsenml-cbor`

SenML CBOR (`application/senml+cbor`): `SenmlCborCodec`, its SPI, integer labels (`SenmlCborFormat`) and the
`SenmlCbor` entry point. Contract tests for backends are in its test fixtures (`AbstractSenmlCborCodecTest`).

## `jsenml-xml`

SenML XML (`application/senml+xml`): `SenmlXmlCodec`, its SPI and the `SenmlXml` entry point. Contract tests for
backends are in its test fixtures (`AbstractSenmlXmlCodecTest`).

## `jsenml-kotlin`

Kotlin DSL (`buildSenmlPack`), `SenmlRecord.copy`, `SenmlExtensions.copy` and conversions from collections and
sequences. See [Kotlin](../docs/Kotlin.md).
