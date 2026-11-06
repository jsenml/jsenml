# 2. Keep record order in resolve

- Status: accepted
- Date: 2026-11-03

## Context

[RFC 8428, Section 4.6][rfc-4-6] says that resolved records need to be in chronological order. The requirement concerns
the result: before resolution, times may be relative and base fields apply to the records following them, so the input
order carries meaning. The section does not use a normative MUST here, and its example does not show reordering, as its
input is already chronological.

The options were to sort in `resolve`, optionally also checking the order in `isResolved`, or to leave sorting to a
separate operation.

## Decision

`resolve` does not sort and `isResolved` does not check the order of records. Chronological order is an explicit step:

```java
SenmlPack chronological = pack.resolve().sortedByTime();
```

Sorting is only allowed on a pack in resolved form and throws `IllegalStateException` otherwise, as reordering records
of other packs would change the meaning of their base fields.

## Consequences

- A pack returned by `resolve` is not necessarily chronological, unlike the resolved form of RFC 8428, Section 4.6.
- Resolved records keep the positions of the received ones, which is also what `SenmlViolation.getRecordIndex()` refers
  to.
- Code that needs the order has to ask for it, and pays for sorting only then.

[rfc-4-6]: https://www.rfc-editor.org/rfc/rfc8428#section-4.6
