# Business context

> This document is scoped to `audit-contracts`: the shared record/message types, not
> the ingestion pipeline or the producer library that consume them. Both of those live
> in other repositories; see [`ARCHITECTURE.md`](ARCHITECTURE.md) for the shape of this
> one.

## Purpose

`audit-contracts` holds the shared record/message types for TDC's audit trail of
operation results (SUCCESS or FAILURE only, never intent). It has no internal
dependency and is consumed, through Maven coordinates, by `audit-client` (the embedded
producer library used by TDC business services) and by the audit ingestion worker, so
both sides agree on one type definition instead of drifting copies.

(Origin repository's openspec/specs/audit-service/spec.md, Overview section; this
repository's `docs/adr/0001-standalone-contracts-library.md`.)

## Out of scope

- Producing, sending, or receiving the messages these types describe — that is
  `audit-client`'s and the ingestion worker's job.
- Persistence, the Aurora hot store, and the ETL/archive pipeline.
- Any dashboard, query API, or admin console.
- Anything specific to how a particular TDC business service calls `audit-client`.

## Ubiquitous language

Only terms that name a contract concept carried by a type in this module.

| Term | Meaning |
|---|---|
| Audit trail | Immutable record of one TDC operation's result (SUCCESS or FAILURE only, never intent or attempt). |
| Event definition | Per-event configuration (name, functionality, trigger point, base/specific data, result, identifiers, correlation id, provider, retention period) that a contract type instance is shaped by. |
| Base data | The 14 fields captured on every event: device, IP, OS, app version, location, timestamp (event-time), result, event name, identifiers, correlation id, provider, retention, schema version, content hash. |
| Specific data | Event-type-specific payload beyond the 14 base fields. Its concrete shape per event type is an open question below. |
| Content hash | Hash of the trail payload, used by the consumer to detect a divergent retry on the same identity. |
| Correlation id | Identifier carried on a trail to relate it to the business operation that produced it. |
| Provider | Event definition/base-data field identifying the originating system or channel. Its full semantics are an open question below. |
| Retention period | Parametrizable per event type; carried as a field on the event definition, not decided by this module. |
| Schema version | `AuditContractVersion.CURRENT`: the version this module's types ship at, so producers and the consumer can detect drift instead of silently deserializing a mismatched payload. |

(Origin repository's openspec/specs/audit-service/spec.md, Event Definition and Event
Capture sections.)

## Invariants

| # | Invariant | Source |
|---|---|---|
| 1 | Contract types are immutable: every field final, no setter, never edited after construction. | `AuditContractsArchitectureTest` rules C4/C5; origin spec.md R1, R5, R7 |
| 2 | No contract type carries forbidden fields (password, NIP, OTP, CVV, biometrics, full PAN); phone and email are masked, card is an internal id plus last 4 digits only, at the point a value is placed into a contract type. | origin spec.md R1/R5/R7; `docs/SECURITY.md` |
| 3 | Event configuration, and therefore the shape a given trail is validated against, changes forward only; the definition version is resolved against the one effective at the trail's `occurredAt`. | origin spec.md R2/R4/R8 |

## Consumers

| Consumer | Repository | Relationship |
|---|---|---|
| `audit-client` | separate repository (embedded producer library, plain Java, no Spring) | depends on `audit-contracts` only |
| Audit ingestion worker | separate repository (the sole deployable, Spring Boot) | depends on `audit-contracts` only, never on `audit-client` |

## Sensitive data handled

The selection determines whether this library falls within PCI DSS scope and whether
it requires mandatory human review (see [`docs/SECURITY.md`](docs/SECURITY.md)).

- [ ] PAN (full card number)
- [ ] CVV / CVV2
- [ ] Customer personal data (name, CURP, RFC, address)
- [ ] Movements or balances
- [ ] None of the above

No box above is ticked: this module ships record shapes, not the data that flows
through them at runtime. See the open questions below for why each is left open rather
than assumed.

## Open questions

| Question | Owner of the answer | Blocked item |
|---|---|---|
| Beyond masked phone and email, does captured `specificData` ever include customer personal data (name, CURP, RFC, address)? | Event definition owner (data team / TDC product) | Sensitive-data checklist (Customer personal data) |
| Can per-event `specificData` include movement or balance details? The 14 base fields do not include amounts, but `specificData`'s schema per event type is not defined in the sources read for this document. | Event definition owner | Sensitive-data checklist (Movements or balances) |
| What is the full semantics of the `provider` field beyond "originating system/channel"? The specs list it as a captured field without a definition. | Event definition owner | Ubiquitous language — provider |
