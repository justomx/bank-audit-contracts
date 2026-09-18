# Guide for agents

Entry index for AI assistants operating on this repository.

Conventions are not reproduced in this document: they are referenced. Multiple copies
of the same rule diverge quickly, and from that point on none of them can be trusted.

## Required reading

| Document | Content |
|---|---|
| [`CONTEXT.md`](CONTEXT.md) | Service purpose and business language |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Layers and verifiable rules. Normative |
| [`docs/TESTING.md`](docs/TESTING.md) | Criteria for when a test is done |
| [`docs/SECURITY.md`](docs/SECURITY.md) | Sensitive data and mandatory review |
| [`docs/adr/`](docs/adr/) | Rationale behind technical decisions |

## Language

All artifacts in this repository are written in English: code, identifiers, comments,
tests and test names, migrations, configuration comments, log and error messages,
commit messages, and documentation. Business terms with no accepted English equivalent
are documented in `CONTEXT.md` and may be used verbatim. Agents and contributors must
not introduce another language, regardless of the language of the conversation or
ticket.

## Structure

Hexagonal architecture, a single service, no `modules/` level. The included example
(`Sample`) runs through all three layers: REST → inbound port → service → outbound port
→ JPA and Redis. It is the implementation reference and takes precedence over any
alternative structure.

## Rules that break the build

Documented in [`ARCHITECTURE.md`](ARCHITECTURE.md) and verified by
`HexagonalArchitectureTest`. The four most frequently broken:

1. `domain/` does not accept framework annotations: not JPA, not Jackson, not Spring.
2. `@Transactional` is declared exclusively in `application/service/`.
3. Controllers depend on the inbound port, never on the service implementation.
4. Injection is by constructor. `@Autowired` and `@RequiredArgsConstructor` are not
   allowed.

Check before considering a change complete: `make arch`.

## Dependency versions

No versions are declared in `pom.xml`. The Spring Boot BOM governs them. Declaring
`<hibernate.version>` by hand overrides a validated combination and produces failures
at runtime, not at compile time. The exceptions — springdoc, spring-cloud-aws,
archunit, and jacoco — are already declared and documented.

## Verifying a change

```bash
make test     # unit tests and architecture rules
make verify   # adds integration tests and the coverage threshold
```

## Review before proposing a push

An agent does not propose pushing a branch or opening a pull request until it has
reviewed its own change. The review is a separate pass over the final diff, not a
recollection of what was written:

1. `make verify` passes. A failing test is information, never something to disable.
2. Read the complete diff, file by file. Anything not required by the ticket is
   removed: leftover debugging, commented-out code, unrelated reformatting.
3. No secrets, keys, real card data, or personal data — including in tests.
4. No PAN, CVV, tokens, or full bodies in any added log line.
5. The change respects the four rules of `ARCHITECTURE.md` that break the build.
6. Any area listed under "Mandatory human review" below is flagged explicitly in the
   pull request description, so the reviewer does not have to discover it.

The findings of this pass are reported to the person. **The agent does not approve its
own work**: this review does not replace the human reviewer, it is what makes that
reviewer's time useful.

## Restrictions

The following actions require an explicit request from a person:

- Modifying a Flyway migration that has already been applied. The fix is introduced in
  a new one.
- Changing the value of `ddl-auto`, fixed at `validate`.
- Adding a dependency to `pom.xml`.
- Modifying `.github/workflows/`, `CODEOWNERS`, or `.gitleaks.toml`.
- Adding build or deployment workflows in GitHub Actions: Justo's continuous
  integration runs in Jenkins. See `.github/workflows/README.md`.
- Introducing credentials, cryptographic keys, or real card data into any file,
  including test files.
- Disabling or marking as `@Disabled` a failing test. A test failure is relevant
  information.

## Mandatory human review

Regardless of the apparent quality of the change, modifications affecting the
following areas are not merged without review by a person (see
[`docs/SECURITY.md`](docs/SECURITY.md)):

- Handling of PAN, CVV, or magnetic stripe — PCI DSS scope
- Funds flow: payments, accounting, reconciliation
- Authorization and resource ownership verification
- Cryptography, key management, and secrets

The service is part of a regulated financial product. The speed of code generation
does not change the cost of a mistake in these areas.

## Branches and commits

Trunk-based development. `main` is the trunk and the only long-lived branch; there is
no `develop`. Branches start from `main`, stay short-lived, and merge back into `main`.
A `release/*` branch is cut from `main` for production and accepts stabilization only.
The restriction is enforced by `.github/workflows/branch-name-gate.yml`.

Commit messages follow Conventional Commits: `chore: <imperative description>`. The
description is written in English.
