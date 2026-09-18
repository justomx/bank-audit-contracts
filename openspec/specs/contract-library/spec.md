# Contract Library Specification

## Overview

`audit-contracts` is a standalone, single-module Maven library that carries the
shared record/message types for TDC's audit trail. It has no internal dependency, no
deployable artifact, and no database schema. This spec captures the contracts-relevant
requirements carried over from the origin `bank-audit-service` reactor's
`openspec/specs/module-boundaries/spec.md`, scoped to what applies once this module is
its own repository.

### Requirement: No Internal Dependency

`audit-contracts` MUST NOT depend on any other internal Justo module or repository,
including `audit-client` and the audit ingestion worker.

#### Scenario: dependency list stays empty of internal modules

- GIVEN the module `pom.xml`
- WHEN its dependency list is inspected
- THEN it MUST NOT list `audit-client`, an audit-ingestion-worker coordinate, or any
  `org.springframework`/`org.springframework.boot` coordinate

### Requirement: Plain, Non-Runnable Jar

The module MUST produce a plain library jar and MUST NOT declare Spring Boot
packaging, a runnable `main` class, or any framework annotation in its main sources.

#### Scenario: built jar carries no Main-Class

- GIVEN a completed `./mvnw verify`
- WHEN the built jar's manifest is inspected
- THEN it MUST NOT contain a `Main-Class` entry

### Requirement: Non-Vacuous Architecture Rules

Every `@ArchTest` rule in `AuditContractsArchitectureTest` MUST match a non-empty class
set at the moment it runs, because `archRule.failOnEmptyShould=false` would let a rule
that matches nothing report green without verifying anything. This module keeps
`archRule.failOnEmptyShould=true` for that reason, with the single documented
exception of rule C5 (see below).

#### Scenario: rule is proven non-vacuous

- GIVEN an `@ArchTest` rule scoped to `com.justo.bank.audit.contracts`
- WHEN a class is added that deliberately violates the rule
- THEN the test MUST fail; reverting the violation MUST make it pass again

#### Scenario: C5 stays the sole allowEmptyShould exception

- GIVEN rule C5 (no public setter)
- WHEN the module has no public method yet
- THEN the rule MUST pass via its own `allowEmptyShould(true)`, not via a change to
  `archunit.properties`, and MUST be revisited once a public method exists (see
  `docs/tech-debt.md` TD-06)

### Requirement: Immutability

Every contract type MUST be immutable: every field final (rule C4), no public setter
(rule C5).

#### Scenario: a mutable field fails the build

- GIVEN a class in `com.justo.bank.audit.contracts` with a non-final field
- WHEN `AuditContractsArchitectureTest` runs
- THEN rule C4 MUST fail

### Requirement: Additive-Only Compatibility Within a Major Version

Within a major version, a published contract type's field MUST NOT be removed, and
its type or meaning MUST NOT change. A breaking change requires a new major version
and a documented migration for producers and the worker (see
`docs/adr/0001-standalone-contracts-library.md`).

#### Scenario: a breaking change is rejected within a major version

- GIVEN a released major version of this library
- WHEN a change proposes removing a field or altering its type or meaning
- THEN the change MUST bump the major version and document the migration, not ship as
  a patch or minor release
