# Spring Boot

Spring Boot 4 integration: SenML support in Boot's mappers, SenML media types in HTTP request and response bodies, and
SenML schemas in springdoc-openapi documents.

## Starter

`jsenml-spring-boot-starter` brings the autoconfiguration, the Jackson 3 JSON backend (`jsenml-json-jackson`) and
`spring-boot-starter-jackson`. Everything else is optional: the autoconfiguration picks it up at runtime when it is on
the classpath.

```kotlin
dependencies {
    implementation(platform("io.github.jsenml:jsenml-bom:<version>"))
    implementation("io.github.jsenml:jsenml-spring-boot-starter")
    // optional, picked up at runtime
    implementation("io.github.jsenml:jsenml-cbor-jackson")
    implementation("io.github.jsenml:jsenml-xml-jackson")
}
```

- `jsenml-cbor-jackson`, `jsenml-xml-jackson` - SenML CBOR and XML in Boot's `CBORMapper` and `XmlMapper`;
- `jsenml-json-gson` - SenML JSON in Boot's `Gson`, next to `spring-boot-starter-gson`;
- springdoc-openapi - SenML schemas in API documents.

To avoid Jackson altogether, use `jsenml-spring-boot-autoconfigure` instead of the starter, next to the backends you
use.

`SenmlPack` and `SenmlRecord` then work in controllers like any other type:

```java
@RestController
@RequestMapping("/packs")
class PackController {

    @PostMapping(consumes = SenmlJsonCodec.MEDIA_TYPE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void add(@RequestBody SenmlPack pack) {
        store.add(pack.resolve());
    }

    @GetMapping(produces = {SenmlJsonCodec.MEDIA_TYPE, SenmlCborCodec.MEDIA_TYPE, SenmlXmlCodec.MEDIA_TYPE})
    SenmlPack all() {
        return store.all();
    }
}
```

## Mappers

Each part is active only when its backend is on the classpath:

- `SenmlJacksonAutoConfiguration` registers the Jackson 3 modules in Boot's `JsonMapper` (`jsenml-json-jackson`),
  `CBORMapper` (`jsenml-cbor-jackson`) and `XmlMapper` (`jsenml-xml-jackson`);
- `SenmlGsonAutoConfiguration` registers `SenmlTypeAdapterFactory` in Boot's `Gson` (`jsenml-json-gson`).

Every registration is a customizer bean. Define a bean of the same type to replace it:

- `SenmlJsonMapperBuilderCustomizer`, `SenmlCborMapperBuilderCustomizer`, `SenmlXmlMapperBuilderCustomizer`;
- `SenmlGsonBuilderCustomizer`.

Boot's Jackson 2 support is deprecated for removal, so it is not autoconfigured. To use the Jackson 2 backends with it,
register their modules yourself, e.g. `SenmlJsonJacksonModule` from `jsenml-json-jackson2` in a
`Jackson2ObjectMapperBuilderCustomizer`.

## Spring MVC and RestClient

HTTP message converters handle request and response bodies of Spring MVC, `RestClient` and `RestTemplate`:

- `application/senml+json` and `application/senml+xml` work out of the box: Boot's JSON and XML converters use its
  mappers and accept `+json` and `+xml` media types.
- `application/senml+cbor` is handled by a converter using Boot's `CBORMapper`, added by
  `SenmlHttpMessageConvertersAutoConfiguration` when `jsenml-cbor-jackson` is on the classpath. It is added after all
  other converters, so they and the default media type of responses stay unchanged.

The CBOR converter is registered by two customizer beans, replaceable separately:

- `SenmlCborServerHttpMessageConvertersCustomizer` - server converters (Spring MVC);
- `SenmlCborClientHttpMessageConvertersCustomizer` - client converters (`RestClient`, `RestTemplate`).

Boot picks Jackson for JSON when it is on the classpath, and `spring-boot-starter-webmvc` brings it. To have Boot's
`Gson` handle JSON bodies instead, set `spring.http.converters.preferred-json-mapper=gson`.

## Spring WebFlux and WebClient

HTTP codecs handle request and response bodies of Spring WebFlux and `WebClient`:

- `application/senml+json` works out of the box, as Boot's JSON codecs use its `JsonMapper`.
- `application/senml+cbor` and `application/senml+xml` need the application's own `CodecCustomizer`. Boot builds the
  CBOR and XML codecs with new mappers, without SenML support, and the CBOR codecs accept `application/cbor` only.

Spring's Jackson CBOR and XML encoders also reject every `encode` call as unsupported stream encoding, including single
values such as response bodies, so the customizer uses encoders that encode those with `encodeValue`.
[`example-webflux`][example-webflux] shows both:

```kotlin
@Bean
fun senmlCodecCustomizer(cborMapper: CBORMapper, xmlMapper: XmlMapper) = CodecCustomizer {
    val defaults = it.defaultCodecs()
    val senmlCbor = MediaType.parseMediaType(SenmlCborCodec.MEDIA_TYPE)
    defaults.jacksonCborDecoder(JacksonCborDecoder(cborMapper, MediaType.APPLICATION_CBOR, senmlCbor))
    defaults.jacksonCborEncoder(SingleValueCborEncoder(cborMapper, MediaType.APPLICATION_CBOR, senmlCbor))
    defaults.jacksonXmlDecoder(JacksonXmlDecoder(xmlMapper))
    defaults.jacksonXmlEncoder(SingleValueXmlEncoder(xmlMapper))
}
```

This replaces the default CBOR and XML codecs of the whole application, which is why JSenML leaves it to you instead of
autoconfiguring it.

## springdoc-openapi

With springdoc-openapi on the classpath, `SenmlSpringDocAutoConfiguration` registers a `SenmlModelConverter`
describing `SenmlPack` and `SenmlRecord` in their SenML representation (`bn` instead of `baseName`), as reusable
`SenmlPack` and `SenmlRecord` components, including XML metadata for `application/senml+xml` (the `sensml` wrapper
element, the SenML namespace and fields as attributes). Define a `SenmlModelConverter` bean to replace it.

## Test slices

The autoconfiguration is registered for Spring Boot's test slices too, so SenML works in them without extra setup:

- `@JsonTest`, `@WebMvcTest`, `@WebFluxTest`, `@RestClientTest` and `@WebClientTest` - SenML in Boot's Jackson mappers
  and `Gson`;
- `@WebMvcTest` and `@RestClientTest` - the `application/senml+cbor` HTTP message converter.

## Properties

- `jsenml.enabled` - `false` disables all JSenML autoconfiguration; default `true`.
- `jsenml.converter.cbor.enabled` - `false` disables the `application/senml+cbor` HTTP message converter; default
  `true`.

[example-webflux]: https://github.com/jsenml/jsenml/tree/main/examples/example-webflux
