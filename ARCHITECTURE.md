# Architecture

> **Normative** document. Its content is verified by
> [`AuditContractsArchitectureTest`](src/test/java/com/justo/bank/audit/contracts/architecture/AuditContractsArchitectureTest.java),
> which runs on every pull request. Any change to a rule in this document requires the
> corresponding change in the test.

## Shape

A plain, non-runnable jar. No Spring, no framework annotations, no `main` class. It is
a records-of-fact library: consumers depend on it through Maven coordinates and it is
never itself deployed.

This repository is the narrow carve-out the origin service's ADR-0005 (library modules
alongside a single deployable) anticipated splitting out into its own repository once
an artifact registry existed. See
[docs/adr/0001-standalone-contracts-library.md](docs/adr/0001-standalone-contracts-library.md)
for the decision to make that split.

## Package layout

```
src/main/java/com/justo/bank/audit/contracts/
└── AuditContractVersion.java   Current schema version anchor for this module
```

Contract types are added as plain `record`s (or, where a private constructor plus
constants is needed, following this module's existing `AuditContractVersion` pattern)
directly under the base package or a topic subpackage. There is no `domain/`,
`application/`, or `infrastructure/` split here: those layers belong to the worker that
consumes this library, not to the library itself.

## Verifiable rules

Each rule has a test that enforces it, in `AuditContractsArchitectureTest`.
`archRule.failOnEmptyShould=true` for this module (see
`src/test/resources/archunit.properties`): every rule is scoped to a package
guaranteed non-empty by the `AuditContractVersion` anchor, so an empty match is a build
failure, not a silent pass — except rule C5, which turns `allowEmptyShould` back on for
itself alone, documented below.

| # | Rule | Test method |
|---|---|---|
| C1 | No dependency on `audit-client` or `audit-ingestion-worker` | `c1_no_dependency_on_client_or_worker` |
| C2 | Only JDK types and this module's own package — no external dependency at all | `c2_only_jdk_and_own_package` |
| C3 | No `System.out`/`System.err` — a library has no logging framework of its own to route through | `c3_no_console_output` |
| C4 | Every field is final — a contract type is a record of fact and must not be mutated after construction | `c4_fields_are_final` |
| C5 | No public setter (`set[A-Z]...`) | `c5_no_setters` |

C5 uses `allowEmptyShould(true)`: it is scoped to public methods, which do not exist
yet in this module (only the `AuditContractVersion` anchor's field and private
constructor do), so an empty match today is the expected starting state, not a typo —
unlike C1-C4, which are scoped to the package itself and stay non-vacuous because of
the anchor type.

Enforced in parallel by `maven-enforcer-plugin`'s `bannedDependencies` (execution
`enforce-module-boundaries`, bound at `validate`), which bans `audit-client`,
`audit-ingestion-worker`, and any `org.springframework`/`org.springframework.boot`
coordinate at the dependency-resolution level, independent of what the source code
imports.

## Compatibility

Within a major version, changes to published contract types are **additive-only**: no
field removal, no change to a field's type or meaning. A breaking change requires a new
major version and a documented migration for producers and the worker. See
[docs/adr/0001-standalone-contracts-library.md](docs/adr/0001-standalone-contracts-library.md).
