# Getting Started

## Dependencies

All modules are published under the `io.github.jsenml` group. Import `jsenml-bom` to align their versions, then add a
backend of every representation you need. A backend brings its API modules (`jsenml-api` and `jsenml-json`,
`jsenml-cbor` or `jsenml-xml`) with it.

Gradle (Kotlin DSL):

```kotlin
dependencies {
    implementation(platform("io.github.jsenml:jsenml-bom:<version>"))
    implementation("io.github.jsenml:jsenml-json-jackson")
}
```

Maven:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.jsenml</groupId>
            <artifactId>jsenml-bom</artifactId>
            <version>${jsenml.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.jsenml</groupId>
        <artifactId>jsenml-json-jackson</artifactId>
    </dependency>
</dependencies>
```

## Choosing backends

Pick one backend per representation, matching the serialization library your application already uses:

- **SenML JSON:**
  - `jsenml-json-jackson` (Jackson 3);
  - `jsenml-json-jackson2` (Jackson 2);
  - `jsenml-json-gson` (Gson).
- **SenML CBOR:**
  - `jsenml-cbor-jackson` (Jackson 3);
  - `jsenml-cbor-jackson2` (Jackson 2).
- **SenML XML:**
  - `jsenml-xml-jackson` (Jackson 3);
  - `jsenml-xml-jackson2` (Jackson 2).

Add `jsenml-kotlin` for the Kotlin DSL, or `jsenml-spring-boot-starter` to integrate with Boot's mappers - see
[Kotlin](./Kotlin.md) and [Spring Boot](./Spring-Boot.md). The full list is in [Modules](./Modules.md).

## First pack

Build a pack, encode it and decode it back:

```java
SenmlPack pack =
    SenmlPack.of(
        SenmlRecord.builder()
            .baseName("urn:dev:ow:10e2073a01080063:")
            .name("voltage")
            .unit("V")
            .value(120.1)
            .build(),
        SenmlRecord.builder()
            .name("current")
            .unit("A")
            .value(1.2)
            .build());

String json = SenmlJson.toJson(pack);
// [{"bn":"urn:dev:ow:10e2073a01080063:","n":"voltage","u":"V","v":120.1},{"n":"current","u":"A","v":1.2}]

SenmlPack decoded = SenmlJson.fromJson(json);
```

Received packs usually carry base fields and relative times. Resolve them before use - it applies base fields,
converts times to absolute ones and validates every record:

```java
SenmlPack resolved = decoded.resolve();

for (SenmlRecord record : resolved) {
    // urn:dev:ow:10e2073a01080063:voltage V 120.1, then urn:dev:ow:10e2073a01080063:current A 1.2
    System.out.println(record.getName() + " " + record.getUnit() + " " + record.getValue());
}
```

See [Resolution](./Resolution.md) for the rules and [Codecs and Backends](./Codecs-and-Backends.md) for codecs,
streams and your own mappers.

## Other representations

CBOR and XML have their own entry points, available only when their modules are on the classpath:

```java
byte[] cbor = SenmlCbor.toCbor(pack);
SenmlPack fromCbor = SenmlCbor.fromCbor(cbor);

String xml = SenmlXml.toXml(pack);
SenmlPack fromXml = SenmlXml.fromXml(xml);
```

A representation whose module is missing cannot be used by mistake: its entry point does not compile.
