# 3. No XML and EXI representations

- Status: accepted
- Date: 2026-11-06

## Context

[RFC 8428][rfc8428] defines SenML in four representations: JSON (Section 5), CBOR (Section 6), XML (Section 7) and EXI
(Section 8).

## Decision

Due to their popularity, only JSON and CBOR are supported by this library for now. XML and EXI are not.

## Consequences

- `application/senml+xml` and `application/senml-exi` packs cannot be encoded or decoded.
- Each representation has its own module, codec type and provider SPI, so one can be added later, as a new module,
  without changing existing ones.

[rfc8428]: https://www.rfc-editor.org/rfc/rfc8428
