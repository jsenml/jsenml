# RFC Conformance

Where JSenML differs from [RFC 8428][rfc8428]. Behavior is the same in all backends unless noted. Each item names the
section of the RFC it concerns.

## Deliberate decisions

- **Base Version is not checked** (Section 4.4). `resolve` accepts a version it does not know, a change of version
  within a pack, and zero or negative versions, so that applications can build their own models on SenML. Checking it is
  up to the application. See [ADR 1][adr-1].
- **`resolve` keeps the record order** (Section 4.6). Resolved form needs chronological order, but `resolve` does not
  sort and `isResolved` does not check it. Use `pack.resolve().sortedByTime()`. See [ADR 2][adr-2].
- **XML and EXI are not supported** (Sections 7 and 8). Only the JSON and CBOR representations are implemented. See
  [ADR 3][adr-3].

## Interpretations

The RFC can be read differently here, or leaves the behavior open.

- **Base Value needs a numeric Value** (Section 4.5.4). The RFC counts a missing Value as zero, which would give a
  record with a Base Value and no Value the Base Value as its value. `resolve` applies the Base Value only to records
  with `v`, as doing so for `vs`, `vb` or `vd` would create a second value field. So a record with only `bv` in effect
  is reported as having neither value nor sum, and a record with `s` gets no `v`. Base Sum follows the RFC literally.
- **Base extensions are dropped by `resolve`** (Section 4.6). Their resolution is left to the specifications defining
  them, so they are not carried over, including accepted mandatory ones. See [Resolution](./Resolution.md).
- **Additions are done in decimal** (Section 4.1). `resolve` adds base fields in decimal and rounds once, so `0.1 + 0.2`
  is `0.3`. Another implementation adding `double` values may differ in the last digits.
- **Mandatory extensions are checked by `resolve`, not by decoding** (Section 4.4). Decoding keeps scalar ones, as it
  cannot know which ones the application understands. An application that decodes without resolving never gets the
  required error. Non-scalar ones in JSON and CBOR are rejected when decoding. See [Extensions](./Extensions.md).
- **Validation is left to `resolve`** (Sections 4.2 and 4.5.1). Builders and codecs accept any combination of fields, so
  any received pack can be represented. Encoding does not validate either, so a pack with an invalid name or two value
  fields is written as it is.

## Stricter than the RFC

- **Non-finite numbers are rejected** (Section 5). CBOR can carry `NaN` and infinities, but the model only holds finite
  numbers, so such a pack fails to decode.
- **Unknown integer labels in CBOR are rejected** (Sections 4.4 and 6). The RFC calls its set of integer labels
  conclusive but also requires ignoring unknown fields, and its informative CDDL allows integer labels.
- **Base Version must be an integer literal that fits `int`** (Section 4.3). `10.0` and `1e1` are rejected in JSON,
  although their JSON type is Number. So is anything above 2^31-1, although the CDDL says `uint` and [RFC 9100][rfc9100]
  uses versions up to 2^53-1, which makes feature codes 31 and above unusable.
- **Duplicated labels are rejected** in JSON and CBOR.

## More lenient than the RFC

- **Empty packs** (Section 11). The informative CDDL requires at least one record, but empty packs are decoded, encoded
  (`[]`, `80`) and resolved.
- **Padded Data Value** (Section 5). Padding must be omitted and is never written, but `vd` with padding is decoded. The
  same goes for `SenmlRecordBuilder.dataValue(String)`.
- **Upper case exponent in JSON** (Section 5). `1E5` is decoded, only a lower case `e` is written.
- **Label `b`** (Section 11). The CDDL requires base labels to be longer, but it is a valid extension name.
- **Non-UTF-8 JSON with Jackson** (Section 5). `decode(byte[])` of the Jackson backends detects the encoding and accepts
  e.g. UTF-16, while Gson reads bytes as UTF-8 and fails.
- **Indefinite lengths in CBOR** (Section 6). Definite lengths are required for non-streaming SenML and always written,
  but indefinite-length arrays, maps, text and byte strings are decoded.
- **Text-string CBOR keys spelling a number** (Section 6). `"2"` is read as the integer label `2`, as Jackson exposes
  integer keys as text.

## Not implemented

Optional parts of the RFC, and related specifications.

- SenSML streams and their media types (Section 4.8).
- Fragment identifiers such as `#rec=3` (Section 9).
- Units are not validated against the IANA registry nor converted (Sections 4.5.2 and 12.1), including the scaled units
  consumers should implement.
- Names are not checked to be URIs or URNs, which is recommended but not required (Section 4.5.1).
- Features of [RFC 9100][rfc9100], secondary units of [RFC 8798][rfc8798] and FETCH/PATCH of [RFC 8790][rfc8790].

## Number format

Numbers follow the recommendation of Section 5 with one exception: the 18 character limit of plain notation does not
count the sign, so a negative number may have a mantissa of 19 characters.

[rfc8428]: https://www.rfc-editor.org/rfc/rfc8428
[rfc8790]: https://www.rfc-editor.org/rfc/rfc8790
[rfc8798]: https://www.rfc-editor.org/rfc/rfc8798
[rfc9100]: https://www.rfc-editor.org/rfc/rfc9100
[adr-1]: https://github.com/jsenml/jsenml/blob/main/docs/adr/0001-accept-any-base-version.md
[adr-2]: https://github.com/jsenml/jsenml/blob/main/docs/adr/0002-keep-record-order-in-resolve.md
[adr-3]: https://github.com/jsenml/jsenml/blob/main/docs/adr/0003-no-xml-and-exi.md
