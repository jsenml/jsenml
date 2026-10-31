# Extensions

Fields not defined by RFC 8428 are kept as record extensions ([RFC 8428, Section 4.4][rfc-4-4]), an immutable, ordered
`SenmlExtensions` set returned by `SenmlRecord.getExtensions()`.

## Values and names

Values are scalars only:

- `String`;
- finite number, stored as `Double`;
- `Boolean`;
- octets (`byte[]`, copied on the way in and out).

Names follow the SenML label syntax `[A-Za-z0-9][-_:.A-Za-z0-9]*` and must not be a label registered by RFC 8428
(`n`, `v`, `bn`, ...). `SenmlExtensions.isValidName(name)` checks both; invalid names are rejected with
`IllegalArgumentException`.

Two naming conventions carry meaning:

- names ending with `_` are **mandatory to understand** - a receiver that does not understand them must reject the
  pack, see below;
- names starting with `b` are **base extensions** - like base fields, they apply to following records, as defined by
  the specification introducing them.

## Building and reading

Set extensions on the record builder, or build a set separately:

```java
SenmlRecord record =
    SenmlRecord.builder()
        .name("temp")
        .value(21.5)
        .extension("accuracy", 0.1)
        .extension("calibrated", true)
        .build();

SenmlExtensions extensions =
    SenmlExtensions.builder()
        .put("accuracy", 0.1)
        .put("raw", new byte[] {1, 2, 3})
        .build();
```

Read them by name and type:

- `getString(name)`, `getNumber(name)`, `getBoolean(name)`, `getBytes(name)` - the value, or `null` if absent; an
  extension of another type throws `IllegalStateException`;
- `get(name)` - the value as `Object` (`String`, `Double`, `Boolean` or a copy of the `byte[]`), when the type is not
  known up front, e.g. after decoding XML;
- `names()`, `contains(name)`, `size()`, `isEmpty()`.

`SenmlExtensions.empty()` is the shared empty set, `toBuilder()` derives a modified copy.

## Decoding

Decoders keep unknown fields with scalar values as extensions and ignore unknown fields with other values (objects,
arrays). Fields ending with `_` are kept the same way; one that cannot be kept, e.g. an object, makes the decoder reject
the whole pack, as the receiver could not understand it.

The representations carry types differently, so extensions may come back with another type than they were written
with:

- **JSON:** strings, numbers and booleans keep their type; octets are written as base64url strings and come back as
  strings.
- **CBOR:** every type round-trips, octets as byte strings.
- **XML:** attributes carry no type, so every extension comes back as a string.

## Mandatory to understand

Decoding does not reject packs with scalar mandatory extensions - it cannot know which ones the application
understands. `SenmlPack.resolve` checks them instead: every extension ending with `_` must be listed as accepted,
otherwise it is reported as a violation:

```java
SenmlPack resolved = pack.resolve(Set.of("accuracy_"));
```

Listing a name that does not end with `_` throws `IllegalArgumentException`. See [Resolution](./Resolution.md) for how
extensions are carried to resolved records.

[rfc-4-4]: https://www.rfc-editor.org/rfc/rfc8428#section-4.4
