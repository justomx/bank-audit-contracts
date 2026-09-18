# 0001 — Standalone contracts library

- **Status:** Proposed (pending human review)
- **Date:** 2026-09-18

## Context

DevOps requires libraries to be published to an artifact registry and each deployable
to live in its own repository. `audit-contracts` previously lived as a module of the
`bank-audit-service` Maven reactor, alongside its sibling library `audit-client` and
the single deployable `audit-ingestion-worker`.

`bank-audit-service`'s own ADR-0005 ("Library modules alongside the single
deployable") accepted that arrangement as a **narrow, temporary carve-out**: at the
time it was written, no Maven artifact registry existed at Justo, so a separate
repository could not have been consumed by anything. That ADR explicitly said the
carve-out "does not block a later repository split" once that platform capability
existed.

That capability — GitHub Packages — now exists. This ADR records the split.

## Decision

`audit-contracts` becomes its own repository:

- No parent POM. Every plugin and dependency version is pinned by hand in `pom.xml`.
- No Spring, no deployable, no database schema — unchanged from before the split.
- Published to GitHub Packages, under Maven coordinates
  `com.justo.bank:audit-contracts`, by DevOps via Jenkins. This repository never
  publishes itself; no publishing credentials exist here in any form.
- Semantic versioning. **Within a major version, changes are additive-only**: no field
  removal, no change to a field's type or meaning. A breaking change requires a new
  major version and a documented migration for producers (`audit-client`) and the
  consumer (the audit ingestion worker).

This ADR supersedes, for this library, ADR-0005 of the origin repository
(`bank-audit-service`), which is not present in this repository.

## Alternatives considered

**Keep it in the `bank-audit-service` reactor.** Rejected: that arrangement was
explicitly documented as temporary, conditioned on the absence of an artifact
registry. With GitHub Packages available, keeping the carve-out only postpones the
split its own ADR anticipated.

**Inherit a shared parent POM.** Rejected for the same reason the origin repository
rejected inheriting `m2-spring-parent`: no shared parent at Justo currently provides
useful dependency management for a pure-JDK library, and pinning every version by hand
keeps the build reproducible without requiring registry authentication just to
compile.

## Consequences

**Favorable:** the library can be versioned, released, and consumed independently of
the ingestion worker's release cadence. A producer or the worker resolves a specific,
immutable version instead of a reactor-relative snapshot.

**Unfavorable:** a change spanning both this library and a consumer is no longer an
atomic cross-repository commit; it requires a coordinated release and version bump on
each side. Version skew between a producer, the worker, and this library's released
version becomes a correctness risk for an audit trail specifically — a producer and
the consumer disagreeing on a contract's shape is not yet mechanically checked (see
`docs/tech-debt.md` TD-01).
