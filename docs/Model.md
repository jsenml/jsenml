# Model

The model lives in `jsenml-api` (package `io.github.jsenml`) and has no serialization dependencies. All types are
immutable, thread-safe and `Serializable`; they are sealed interfaces created through builders and static factories.

## Pack

`SenmlPack` is an ordered list of records ([RFC 8428, Section 4][rfc-4]). It implements `Iterable<SenmlRecord>`.

Creating packs:

- `SenmlPack.of(record, ...)` and `SenmlPack.of(collection)` - from records, in the given order;
- `SenmlPack.empty()` - the shared empty pack;
- `SenmlPack.builder()` - `add(record)`, `addAll(records)`, `build()`;
- `SenmlPack.collector()` - a `Collector` for streams, e.g.
  `pack.stream().filter(r -> "Cel".equals(r.getUnit())).collect(SenmlPack.collector())`.

Reading packs:

- `getRecords()`, `get(index)`, `getFirst()`, `getLast()`, `size()`, `isEmpty()`, `stream()`;
- `isResolved()` - whether the pack is already in resolved form;
- `resolve(...)` - the equivalent pack in resolved form, see [Resolution](./Resolution.md).
- `sortedByTime()`, `sorted(keyExtractor)` - the pack with records sorted (stable), only for a pack in resolved form,
  see [Resolution](./Resolution.md#chronological-order).

## Record

`SenmlRecord` holds base fields, which apply to this record and all following records of the pack until overridden,
and regular fields, which apply to this record only. Every field is optional. Getters return `null` for absent fields:

- **Base fields:**
  - `getBaseName()` (`bn`) - prefix of names of this and following records;
  - `getBaseTime()` (`bt`) - added to times;
  - `getBaseUnit()` (`bu`) - default unit;
  - `getBaseValue()` (`bv`) - added to numeric values;
  - `getBaseSum()` (`bs`) - added to sums;
  - `getBaseVersion()` (`bver`) - SenML version, 10 by default.
- **Regular fields:**
  - `getName()` (`n`), `getUnit()` (`u`);
  - value fields: `getValue()` (`v`, number), `getStringValue()` (`vs`), `getBooleanValue()` (`vb`), `getDataValue()`
    (`vd`, octets, returned as a copy);
  - `getSum()` (`s`), `getTime()` (`t`), `getUpdateTime()` (`ut`).
- **Extensions:** `getExtensions()` - fields not defined by RFC 8428, see [Extensions](./Extensions.md).

`hasBaseFields()` tells whether any of `bn`, `bt`, `bu`, `bv`, `bs` or `bver` is present.

Numeric fields are `Double` and must be finite. Records are not checked against cross-field rules (a single value
field, a name present) when built, so any pack received on the wire can be represented as-is; [resolving](./Resolution.md)
validates them.

## Builders

`SenmlRecord.builder()` creates a record, `record.toBuilder()` a modified copy:

```java
SenmlRecord record =
    SenmlRecord.builder()
        .name("temp")
        .unit("Cel")
        .value(21.5)
        .time(1_700_000_000)
        .extension("accuracy", 0.1)
        .build();

SenmlRecord warmer = record.toBuilder().value(22.0).build();
```

Builder rules:

- Setters accept `null` to clear a field.
- Numeric setters accept any `Number`, converted to `double`: `Float` through its decimal representation (`23.1f`
  becomes `23.1`), other types with `Number.doubleValue()`, so `long` values beyond ±2^53 or `BigDecimal` values may
  lose precision. Infinite and NaN values are rejected with `IllegalArgumentException`.
- `dataValue(String)` takes base64url text (padding optional), as carried in JSON; `dataValue(byte[])` takes
  octets and copies the array.
- `extension(name, value)`, `removeExtension(name)` and `extensions(SenmlExtensions)` manage extensions.
- Builders are mutable and not thread-safe; built records are immutable.

## Equality and text

Packs, records and extensions implement `equals` and `hashCode` by value, so a decoded pack equals the pack it was
encoded from. `toString()` lists present fields with their SenML labels, e.g.
`SenmlRecord[n=temp, u=Cel, v=21.5]`, for logging only.

## Labels and number format

`SenmlTextFormat` holds the labels of the text representations (`SenmlTextFormat.BASE_NAME` is `"bn"`, ...), the number
formatting shared by every backend (`formatNumber`), base64url formatting of octets (`formatBytes`) and
`isMandatoryToUnderstand(label)`. CBOR uses integer labels instead, see [Representations](./Representations.md).

## Exceptions

All exceptions extend `SenmlException` (a `RuntimeException`):

- `SenmlCodecException` - encoding or decoding failed, e.g. malformed input;
- `SenmlViolationException` - a pack breaks SenML rules, thrown by `resolve`, see [Resolution](./Resolution.md).

`SenmlException` itself is thrown when no backend of a representation is available, see
[Codecs and Backends](./Codecs-and-Backends.md). Invalid builder arguments throw `IllegalArgumentException`.

[rfc-4]: https://www.rfc-editor.org/rfc/rfc8428#section-4
