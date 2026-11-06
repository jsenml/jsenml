# Codecs and Backends

Each SenML representation has its own module with its own codec type, provider SPI and entry point. Backend modules
implement the codecs on top of a serialization library and are discovered with `java.util.ServiceLoader`.

## Entry points

Static methods for the common cases, using the default codec of the representation:

- **JSON** (`jsenml-json`, `SenmlJson`): `toJson(pack)`, `toJsonBytes(pack)`, `fromJson(String)`, `fromJson(byte[])`.
- **CBOR** (`jsenml-cbor`, `SenmlCbor`): `toCbor(pack)`, `fromCbor(byte[])`.

If no backend of the representation is on the classpath, they throw `SenmlException` naming the modules to add.
Encoding and decoding failures throw `SenmlCodecException`.

## Codecs

`SenmlJson.jsonCodec()` and `SenmlCbor.cborCodec()` return the default codec, for streams and
repeated use. Codecs are thread-safe; keep one and reuse it.

- `SenmlJsonCodec`: `encode(pack)` to `String`, `encode(pack, Writer)`, `encodeToBytes(pack)`,
  `decode(String)`, `decode(Reader)`, `decode(byte[])`.
- `SenmlCborCodec`: `encode(pack)` to `byte[]`, `encode(pack, OutputStream)`, `decode(byte[])`, `decode(InputStream)`.

```java
SenmlJsonCodec codec = SenmlJson.jsonCodec();

try (Reader reader = Files.newBufferedReader(path)) {
    SenmlPack pack = codec.decode(reader);
}
```

Every codec type declares its media type as `MEDIA_TYPE` (`application/senml+json`, `application/senml+cbor`), e.g. for
HTTP content negotiation.

## Backend selection

Every backend registers a provider with a name and a priority. When several backends of a representation are on the
classpath, the one with the highest priority is the default:

- `jackson` (Jackson 3) - priority 5000;
- `jackson2` (Jackson 2) - priority 4000;
- `gson` (Gson, JSON only) - priority 1000.

Select a backend explicitly by name:

```java
SenmlJsonCodec gsonCodec = SenmlJson.jsonCodec("gson");
SenmlCborCodec jackson2Cbor = SenmlCbor.cborCodec("jackson2");
```

An unknown name throws `SenmlException`.

## Your own mappers

Entry points use codecs with default mappers. To keep your mapper's configuration, create a codec of a backend
directly. The given mapper is not modified - the SenML module is registered on a derived instance:

```java
SenmlJsonCodec jackson = new JacksonSenmlJsonCodec(myJsonMapper);
SenmlCborCodec cbor = new JacksonSenmlCborCodec(myCborMapper);
SenmlJsonCodec gson = new GsonSenmlJsonCodec(myGson);
```

Jackson 2 backends have the same class names in `io.github.jsenml.<format>.jackson2` packages, taking Jackson 2
mappers.

## Plain Jackson and Gson

Backends also integrate with plain mappers, so `SenmlPack` and `SenmlRecord` can be (de)serialized as part of your own
types:

```java
// or JsonMapper.builder().findAndAddModules()
JsonMapper jsonMapper =
    JsonMapper.builder()
        .addModule(new SenmlJsonJacksonModule())
        .build();

// the CBOR module is not discovered by findAndAddModules(), register it on CBOR mappers only
CBORMapper cborMapper =
    CBORMapper.builder()
        .addModule(new SenmlCborJacksonModule())
        .build();

Gson gson =
    new GsonBuilder()
        .registerTypeAdapterFactory(new SenmlTypeAdapterFactory())
        .create();
```

Only `SenmlJsonJacksonModule` is discovered by `findAndAddModules()`: each representation needs its own module on its
own mapper, and a JSON module registered on a CBOR mapper would write the wrong representation.

## Spring Boot

With Spring Boot, use a starter instead - it registers the modules on Boot's mappers. See [Spring Boot](./Spring-Boot.md).
