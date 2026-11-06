# Representations

RFC 8428 defines a pack in several representations. JSenML supports JSON and CBOR, each in its own module, and all
backends of a representation produce identical output for the same pack.

## JSON

`application/senml+json`, [RFC 8428, Section 5][rfc-5]. A pack is a JSON array of objects, one per record, with fields
under their text labels:

```json
[
  {"bn":"urn:dev:ow:10e2073a01080063:","bt":1320067464,"bu":"%RH","v":20},
  {"u":"lon","v":24.30621},
  {"n":"temp","u":"Cel","t":60,"v":23.1}
]
```

- Labels: `bn`, `bt`, `bu`, `bv`, `bs`, `bver`, `n`, `u`, `v`, `vs`, `vb`, `vd`, `s`, `t`, `ut` (constants in
  `SenmlTextFormat`).
- Numbers are written without trailing zeros, as plain literals up to 18 characters (sign aside) and in exponent
  notation with a lower case `e` above that, as RFC 8428, Section 5 recommends a mantissa shorter than 19 characters:
  `1.27602E9` becomes `1276020000`, `21.0` becomes `21`, `1.0E300` becomes `1e300`, `1.5E-20` becomes `1.5e-20`.
- The Data Value (`vd`) and octet extensions are written as unpadded base64url strings.
- Decoding ignores unknown fields with non-scalar values and keeps scalar ones as extensions, see
  [Extensions](./Extensions.md).
- Decoding rejects a record with a duplicated label and a Base Version (`bver`) that is not an integer literal (e.g.
  `10.0` or `1e1`).

## CBOR

`application/senml+cbor`, [RFC 8428, Section 6][rfc-6]. The same structure as JSON, encoded as CBOR with integer map
keys for registered labels (constants in `SenmlCborFormat`):

- base fields: `bver` = -1, `bn` = -2, `bt` = -3, `bu` = -4, `bv` = -5, `bs` = -6;
- regular fields: `n` = 0, `u` = 1, `v` = 2, `vs` = 3, `vb` = 4, `s` = 5, `t` = 6, `ut` = 7, `vd` = 8.

Encoding rules:

- numbers in their most compact exact form: integer, single or double precision float;
- the Data Value and octet extensions as byte strings;
- extensions under text-string keys, using their names.

Decoding rules:

- unknown integer keys are rejected, as the set of integer labels is closed;
- a record with a duplicated key is rejected;
- unknown text-string keys follow the same rules as unknown JSON fields;
- every extension type round-trips, including octets.

## Not supported

XML ([RFC 8428, Section 7][rfc-7]) and EXI ([RFC 8428, Section 8][rfc-8]) are deliberately not supported, see
[RFC Conformance](./RFC-Conformance.md#deliberate-decisions). Each representation has its own codec type and provider
SPI, so a new one would be added as a new module without changing existing ones - see
[Writing a Backend](./Writing-a-Backend.md).

[rfc-5]: https://www.rfc-editor.org/rfc/rfc8428#section-5
[rfc-6]: https://www.rfc-editor.org/rfc/rfc8428#section-6
[rfc-7]: https://www.rfc-editor.org/rfc/rfc8428#section-7
[rfc-8]: https://www.rfc-editor.org/rfc/rfc8428#section-8
