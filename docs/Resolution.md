# Resolution

Received packs are usually compact: base fields shared by several records, relative times, a base value added to
values. `SenmlPack.resolve` returns the equivalent pack in resolved form ([RFC 8428, Section 4.6][rfc-4-6]), where
every record stands on its own, and validates every record on the way.

## Rules

In resolved form, base fields (except Base Version) are applied to and removed from each record:

- base name is prepended to the name;
- base unit fills a missing unit;
- base value is added to the numeric value - only to records with a numeric value, as applying it to another value
  type would create a second value field;
- base sum and sum are added together, a missing one counting as zero, so a record with base sum in effect but no sum
  of its own resolves to the base sum (RFC 8428, Section 4.5.4);
- base time is added to the time;
- relative times (below 2^28, about 8.5 years) are converted to absolute times relative to "now"
  ([RFC 8428, Section 4.5.3][rfc-4-5-3]); a record without time and base time is a measurement taken "now".

Additions are done in decimal and rounded to `double` once, so `0.1 + 0.2` resolves to `0.3`.

Base Version is not a value to apply: a non-default version (other than 10) is kept on every resolved record, the
default one is dropped.

```java
// [{"bn":"urn:dev:ow:10e2073a01080063:","bt":1.320067464e+09,"bu":"%RH","v":20},
//  {"u":"lon","v":24.30621},
//  {"n":"temp","u":"Cel","t":60,"v":23.1}]
SenmlPack resolved = SenmlJson.fromJson(json).resolve();

// urn:dev:ow:10e2073a01080063:     %RH  t=1.320067464E9  v=20.0   (name is the base name alone)
// urn:dev:ow:10e2073a01080063:     lon  t=1.320067464E9  v=24.30621
// urn:dev:ow:10e2073a01080063:temp Cel  t=1.320067524E9  v=23.1
```

## Time

Relative times need "now". The overloads take it in several forms:

- `resolve()` and `resolve(extensions)` - the system UTC clock;
- `resolve(Clock)` and `resolve(Clock, extensions)` - a clock, e.g. a fixed one in tests;
- `resolve(Instant)` and `resolve(Instant, extensions)` - an instant, e.g. the time the pack was received.

## Validation

Every resolved record must have:

- a valid name, matching `[A-Za-z0-9][A-Za-z0-9-:./_]*`;
- at most one value field (`v`, `vs`, `vb`, `vd`);
- a value field, unless it has a sum ([RFC 8428, Section 4.2][rfc-4-2]);
- finite resolved numbers (adding base fields must not overflow);
- no mandatory extensions (ending with `_`) other than the accepted ones, see [Extensions](./Extensions.md).

Validation does not stop at the first problem. `SenmlViolationException.getViolations()` lists all violations of all
records in pack order, each with `getRecordIndex()` and a human-readable `getReason()`:

```java
try {
    SenmlPack resolved = pack.resolve();
} catch (SenmlViolationException e) {
    for (SenmlViolation violation : e.getViolations()) {
        log.warn("record {}: {}", violation.getRecordIndex(), violation.getReason());
    }
}
```

The exception message is derived from the violations for logging only and is not meant to be parsed.

## Extensions in resolved records

Regular extensions are kept on each resolved record. Base extensions (names starting with `b`) are not carried over:
resolved records have no base fields, and how a base extension resolves is defined by the specification introducing
it. Accepted mandatory base extensions are left to the caller, who reads them from the original pack.

## Resolved form

`isResolved()` tells whether a pack is already in resolved form: no record has base fields or base extensions, every
record has an absolute time, and a non-default Base Version is present in every record. Resolving such a pack returns it
as is. Resolved form does not imply validity - only `resolve` validates records.

[rfc-4-2]: https://www.rfc-editor.org/rfc/rfc8428#section-4.2
[rfc-4-5-3]: https://www.rfc-editor.org/rfc/rfc8428#section-4.5.3
[rfc-4-6]: https://www.rfc-editor.org/rfc/rfc8428#section-4.6
