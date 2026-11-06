# 1. Accept any Base Version in resolve

- Status: accepted
- Date: 2026-11-03

## Context

[RFC 8428, Section 4.4][rfc-4-4] tells a recipient not to use a pack whose version is higher than the one it
understands, and requires all records of a pack to have the same version. Section 4.1 defines the Base Version as a
positive integer. [RFC 9100][rfc-9100] turns the version into a feature bitmap, so e.g. version 26 is version 10 with
the Secondary Units feature.

`SenmlPack.resolve` could enforce all of this by reporting a violation.

## Decision

`resolve` does not reject any Base Version: not an unknown one, not a change of version within a pack, not one that is
zero or negative. A non-default version is kept on resolved records, the default one (10) is dropped.

Applications that build their own models on SenML must not be blocked by the library.

## Consequences

- This deviates from RFC 8428, Section 4.4 on purpose. Javadoc of `resolve` and the Resolution page say so.
- Checking the version is up to the application, e.g. with `getBaseVersion()` of resolved records. An application that
  does not check treats a pack of an unknown version as regular SenML.
- Codecs accept zero and negative versions too, although CBOR allows only an unsigned integer. They do require an
  integer literal that fits `int`, as `SenmlRecord.getBaseVersion()` is an `Integer`. Versions above 2^31-1 are
  rejected, so feature codes 31 and above of RFC 9100 are out of reach.

[rfc-4-4]: https://www.rfc-editor.org/rfc/rfc8428#section-4.4
[rfc-9100]: https://www.rfc-editor.org/rfc/rfc9100
