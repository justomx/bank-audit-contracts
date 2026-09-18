# 0001 — Hexagonal architecture, one service per repository

- **Status:** Accepted
- **Date:** 2026-09-10

## Context

The TDC program starts out with 24 services. In the absence of a common structure, each
repository adopts the conventions of whoever starts it, and harmonizing them afterward
becomes unworkable.

Jüsto's previous template (`java-hexagonal-template`) already used hexagonal, but with
a `modules/` level that hosted two sample modules within the same repository.

## Decision

The hexagonal architecture is kept, with the structure
`application/port/{in,out}` · `application/service` · `domain/{model,exception}` ·
`infrastructure/adapter/{in,out}`, including the `adapter/` level.

**The `modules/` level is removed.** One repository holds one service, one
deployable, one bounded context, and a single hexagon.

Services with several aggregates organize them as subpackages within
`domain/model/`, not as parallel hexagons.

## Alternatives considered

**Keep `modules/`.** Rejected. Its purpose was to show two illustrative examples within
the same repository. Keeping it encourages adding a second module, a situation in which
a deployable would end up holding two bounded contexts over the same database, with the
consequent loss of the ownership that motivates splitting into services.

**Organize by feature instead of by layer** (`account/domain`, `account/rest`).
Rejected for consistency: the previous template was organized by layer and the team
knows it.

## Consequences

**Favorable:** the architecture test is expressed over fixed paths, which simplifies
it. The structure matches the reference provided by the team.

**Unfavorable:** a service that evolves toward two bounded contexts will need to be
split into two repositories. The outcome is the right one, though it carries work.
