# Guide for agents

Entry index for AI assistants operating on this repository.

Conventions are not reproduced in this document: they are referenced. Multiple copies
of the same rule diverge quickly, and from that point on none of them can be trusted.

## Required reading

| Document | Content |
|---|---|
| [`CONTEXT.md`](CONTEXT.md) | Contract scope and business language |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Verifiable rules. Normative |
| [`docs/TESTING.md`](docs/TESTING.md) | Criteria for when a test is done |
| [`docs/SECURITY.md`](docs/SECURITY.md) | Sensitive data and mandatory review |
| [`docs/adr/`](docs/adr/) | Rationale behind technical decisions |
| [`docs/tech-debt.md`](docs/tech-debt.md) | Known gaps of this library. Update it in the change that opens or closes one |
| [`openspec/`](openspec/) | Changes are specified with SDD; [`openspec/specs/`](openspec/specs/) is the source of truth for behaviour. Versioned per change: `proposal.md`, `design.md`, `tasks.md`, `specs/**`; local/git-ignored: `exploration.md`, `apply-progress.md`, `verify-report.md`, `archive-report.md`, `state.yaml`. See [`openspec/config.yaml`](openspec/config.yaml) for rules |

## Language

All artifacts in this repository are written in English: code, identifiers, comments,
tests and test names, configuration comments, and documentation. Business terms with
no accepted English equivalent are documented in `CONTEXT.md` and may be used
verbatim. Agents and contributors must not introduce another language, regardless of
the language of the conversation or ticket.

## Structure

Single-module library, base package `com.justo.bank.audit.contracts`. It ships no
deployable and owns no schema; it exists to be consumed, through Maven coordinates, by
`audit-client` (the embedded producer library) and the audit ingestion worker, both of
which live in other repositories. See
[docs/adr/0001-standalone-contracts-library.md](docs/adr/0001-standalone-contracts-library.md).

## Rules that break the build

Documented in [`ARCHITECTURE.md`](ARCHITECTURE.md) and verified by
`AuditContractsArchitectureTest` (rules C2, C4, C5) plus the `maven-enforcer-plugin` ban on
`audit-client`, `audit-ingestion-worker`, and any `org.springframework(.boot)`
coordinate. Check before considering a change complete: `make arch`.

## Dependency versions

There is no Spring BOM here: this is a plain-JDK library with no parent POM. Every
plugin and test-dependency version is pinned by hand in `pom.xml`, and each one
mirrors the version the Spring Boot 3.5.16 line previously resolved for this module
when it lived in the `bank-audit-service` reactor, so the toolchain stays consistent
with its consumers without inheriting a Spring Boot parent this library is not.

## Verifying a change

```bash
make test        # unit tests and architecture rules
make verify      # adds the coverage threshold
make docs-check  # AGENTS.md, ARCHITECTURE.md, README.md, CONTEXT.md, and docs/**
                  # have no broken links or stale paths
```

## Review before proposing a push

An agent does not propose pushing a branch or opening a pull request until it has
reviewed its own change. The review is a separate pass over the final diff, not a
recollection of what was written:

1. `make verify` passes. A failing test is information, never something to disable.
2. Read the complete diff, file by file. Anything not required by the ticket is
   removed: leftover debugging, commented-out code, unrelated reformatting.
3. No secrets, keys, real card data, or personal data — including in tests.
4. No contract type carries PAN, CVV, NIP, OTP, credentials or keys.
5. The change respects the rules of `ARCHITECTURE.md` that break the build.
6. Any area listed under "Mandatory human review" below is flagged explicitly in the
   pull request description, so the reviewer does not have to discover it.

The findings of this pass are reported to the person. **The agent does not approve its
own work**: this review does not replace the human reviewer, it is what makes that
reviewer's time useful.

## Restrictions

The following actions require an explicit request from a person:

- Adding a dependency to `pom.xml`.
- Modifying `.github/workflows/`, `CODEOWNERS`, or `.gitleaks.toml`.
- Adding build or deployment workflows in GitHub Actions: Justo's continuous
  integration runs in Jenkins. See `.github/workflows/README.md`.
- Introducing credentials, cryptographic keys, or real card data into any file,
  including test files.
- Disabling or marking as `@Disabled` a failing test. A test failure is relevant
  information.
- Removing or changing the type or meaning of a published contract field, or any
  other breaking change (see [docs/adr/0001-standalone-contracts-library.md](docs/adr/0001-standalone-contracts-library.md)).
- Publishing to the registry — done only by DevOps via Jenkins.

## Mandatory human review

Regardless of the apparent quality of the change, modifications affecting the
following areas are not merged without review by a person (see
[`docs/SECURITY.md`](docs/SECURITY.md)):

- Handling of PAN, CVV, or magnetic stripe — PCI DSS scope
- Funds flow: payments, accounting, reconciliation
- Authorization and resource ownership verification
- Cryptography, key management, and secrets

This library carries data for a regulated financial product. The speed of code
generation does not change the cost of a mistake in these areas.

## Branches and commits

Trunk-based development. `main` is the trunk and the only long-lived branch; there is
no `develop`. Branches start from `main`, stay short-lived, and merge back into `main`.
A `release/*` branch is cut from `main` for production and accepts stabilization only.
The restriction is enforced by `.github/workflows/branch-name-gate.yml`.

Commit message format: `<TICKET> <type>: <description>`, for example
`AJTC-110 feat: add the audit event record`. `<type>` is a conventional-commit type
(`feat`, `fix`, `docs`, `test`, `build`, `chore`, `refactor`). The description is written
in English.
