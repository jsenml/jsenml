# Gson integration for JSenML

Gson backend for SenML JSON, registered as provider `gson` with lower priority than the Jackson backends. Gson has no
CBOR support, so there is no Gson backend for that representation. See
[Codecs and Backends](../docs/Codecs-and-Backends.md).

## `jsenml-json-gson`

`GsonSenmlJsonCodec` and `SenmlTypeAdapterFactory`, to register on your own `Gson`.

```java
new GsonBuilder().registerTypeAdapterFactory(new SenmlTypeAdapterFactory())
```
