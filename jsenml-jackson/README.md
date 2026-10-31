# Jackson integration for JSenML

Jackson 3 (`tools.jackson`) backends, registered as provider `jackson` with the highest priority, so they are the
default when several backends are present. Each codec can be created with your own mapper, e.g.
`new JacksonSenmlJsonCodec(myJsonMapper)`, which is not modified. See
[Codecs and Backends](../docs/Codecs-and-Backends.md).

## `jsenml-json-jackson`

`JacksonSenmlJsonCodec` and `SenmlJsonJacksonModule`, also discovered by `findAndAddModules()`.

## `jsenml-cbor-jackson`

`JacksonSenmlCborCodec` and `SenmlCborJacksonModule`, writing registered labels as integer keys and the data value as a
byte string. The module is not discovered by `findAndAddModules()`; register it on CBOR mappers only.

## `jsenml-xml-jackson`

`JacksonSenmlXmlCodec` and `SenmlXmlJacksonModule`, writing a `sensml` element in the SenML namespace with fields as
attributes. The module is not discovered by `findAndAddModules()`; register it on XML mappers only.
