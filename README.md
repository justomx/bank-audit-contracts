# bank-template-vertical-bank

Backend microservices template for Jüsto's **Bank vertical** (TDC program).

Java 21 (LTS) · Spring Boot 3.5.16 · PostgreSQL 17 · Redis · Hexagonal architecture

> **Status:** verified in execution. `./mvnw verify` runs 23 tests (21 unit
> and 2 integration against a real PostgreSQL 17) with 84.2% line coverage.
> The flat jar starts via `mainClass` using `conf/local` as its only configuration, the
> same way the platform's runner image does. See §12.

---

## 1. Creating a service from the template

This repository is configured on GitHub as a **Template repository**, which enables
the *"Use this template"* button. That mechanism creates a new repository **without
the template's commit history**.

> **Must not be cloned.** Cloning drags the entire template history into the service.

| Step | Action |
|---|---|
| 1 | On GitHub: *"Use this template"* → *"Create a new repository"*. Owner `justomx`, name `bank-<service>` |
| 2 | `git clone git@github.com:justomx/bank-<service>.git` |
| 3 | `make init` — renames the package, `pom.xml`, main class, application name, and ECR repository |
| 4 | Write `CONTEXT.md` with the service's purpose and business language |
| 5 | On GitHub: add the topics and configure branch protection — see §8 |
| 6 | `make infra-up && make verify` to confirm the project builds and passes the tests |

### Scope of `make init`

A manual rename leaves residual references: the package in the architecture test, the
`artifactId`, the ECR repository name. The script updates every one of those points at
once and **verifies at the end that no traces remain**, listing any that are left.

Example run:

```
$ make init
> card-account-service

  Repository     : bank-card-account-service
  artifactId     : bank-card-account-service
  Java package   : com.justo.bank.cardaccount
  Main class     : CardAccountApplication

>> Moving the Java package...
>> Updating references...
>> Verifying that no references to the template remain...
   No traces of the template left.
```

The root documents — `AGENTS.md`, `CONTEXT.md`, `ARCHITECTURE.md` — and the `docs/`
directory **remain in the generated repository**: they are part of the service, not of
the template. `init` does not delete them; it only updates the name references.

The script deletes itself after use: `git rm scripts/init-repo.sh`.

### Naming convention agreed with DevOps

| Element | Rule | Example |
|---|---|---|
| Organization | `justomx` | — |
| Repository name | **`bank-`** prefix | `bank-card-account-service` |
| Repository topics | `squad-tdc` · `backend` · `vertical-bank` | — |
| Java package | `com.justo.bank.<service>` | `com.justo.bank.cardaccount` |

Creating repositories is the development team's responsibility, not DevOps's.

---

## 2. Requirements

| Tool | Version | Note |
|---|---|---|
| JDK | **21** | LTS. Aligned with the platform's base images |
| Maven | — | No installation required: the repository includes `./mvnw` |
| Docker | recent | Needed for PostgreSQL, Redis, and Testcontainers |

### Installing JDK 21 on macOS

The recommended route is Homebrew:

```bash
brew install --cask temurin@21
```

If the installer lacks administrator permissions, the compressed archive can be
installed in the user directory, without `sudo`:

```bash
mkdir -p ~/Library/Java/JavaVirtualMachines
curl -fsSL -o /tmp/jdk25.tar.gz \
  "https://api.adoptium.net/v3/binary/latest/21/ga/mac/aarch64/jdk/hotspot/normal/eclipse"
tar -xzf /tmp/jdk25.tar.gz -C ~/Library/Java/JavaVirtualMachines
/usr/libexec/java_home -v 21    # confirms macOS detects it
```

To pin the version in each session:

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
```

> **Note on SDKMAN.** The repository includes a `.sdkmanrc`, but SDKMAN requires bash 4
> or higher and macOS ships bash 3.2. Using it requires first installing a recent
> version of bash (`brew install bash`). The file is kept because it is useful on
> Linux and in environments that already have SDKMAN.

---

## 3. Running locally

```bash
make infra-up     # PostgreSQL + Redis in containers
make run          # application with the local profile
```

Check:

```bash
curl -s localhost:8080/api/health/readiness
curl -s -X POST localhost:8080/api/v1/samples \
     -H 'Content-Type: application/json' \
     -d '{"name":"test","description":"example"}'
curl -s localhost:8080/api/v1/samples/1
```

Interactive API documentation: <http://localhost:8080/api/swagger-ui.html>

All of the service's routes hang off `/api` (`server.servlet.context-path`), a
platform convention. Health lives at `/api/health`, with `/api/health/liveness` and
`/api/health/readiness` for the Kubernetes probes.

### Available commands

`make help` lists the complete set. The commonly used ones:

| Command | Description |
|---|---|
| `make init` | Renames the template to the target service. Run once |
| `make run` | Starts the application with the local profile |
| `make test` | Unit tests and architecture rules. No Docker required |
| `make arch` | Only the architecture rules |
| `make verify` | Full set: unit, integration, and coverage threshold |
| `make infra-up` | Starts PostgreSQL and Redis |
| `make deps-tree` | Shows the versions resolved by the Spring Boot BOM |

---

## 4. Code organization

Hexagonal architecture. Dependencies have a single allowed direction:

```
infrastructure  ──────►  application  ──────►  domain
(adapters)               (use cases)           (rules)
```

```
src/main/java/com/justo/bank/<service>/
├── domain/
│   ├── model/               Entities. Pure Java, no framework annotations
│   └── exception/           Business exceptions, no HTTP status codes
├── application/
│   ├── port/in/             Use cases (interfaces) and commands
│   ├── port/out/            External dependencies of the use case
│   └── service/             Implementation. Where @Service and @Transactional live
├── infrastructure/
│   └── adapter/
│       ├── in/rest/         Controllers, DTOs, mappers, error handling
│       ├── out/persistence/ JPA entities and adapters
│       └── out/cache/       Redis
└── config/                  Cross-cutting configuration
```

**One repository corresponds to one service, one deployable, and one bounded
context.** The structure does not include a `modules/` level.

The template includes a complete example (`Sample`) that runs through all three
layers: REST → inbound port → service → outbound port → JPA and Redis. It is the
implementation reference. `make init` offers to delete it.

### Architecture rules

Documented in **[`ARCHITECTURE.md`](ARCHITECTURE.md)** and verified by
[`HexagonalArchitectureTest`](src/test/java/com/justo/bank/template/architecture/HexagonalArchitectureTest.java)
on every integration. Breaking them **causes a build failure** that identifies the
responsible class.

The four most frequently broken:

1. `domain/` does not accept framework annotations: not JPA, not Jackson, not Spring.
2. `@Transactional` is declared exclusively in `application/service/`.
3. Controllers depend on the inbound port, never on the service implementation.
4. Injection is by constructor. `@Autowired` and `@RequiredArgsConstructor` are not
   allowed.

> A convention that no mechanism verifies degrades within weeks. That is why the rules
> are expressed as executable tests.

---

## 5. Dependency version management

**The only version declared manually in `pom.xml` is the parent's (Spring Boot).**
Everything else — Spring Data JPA, Hibernate, Jackson, HikariCP, the PostgreSQL driver,
Tomcat — is resolved by the Spring Boot **BOM**, a set validated by the Spring team.

```xml
<parent>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.16</version>         <!-- only version declared -->
</parent>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
    <!-- no <version>: inherited from the BOM -->
</dependency>
```

Declaring `<hibernate.version>` by hand overrides that validated combination. The
resulting failure does not show up at compile time but at runtime, usually as a
`NoSuchMethodError` when a specific code path is invoked.

To check the versions actually resolved: `make deps-tree`.

The exceptions are libraries that Spring Boot does not manage. All of them are
declared and verified against Boot 3.5.16:

| Library | Version | Verification |
|---|---|---|
| springdoc-openapi | 2.9.1 | 2.x line, compatible with Spring Boot 3.x |
| spring-cloud-aws | 3.4.2 | 3.4.x line, compatible with Spring Boot 3.5 |
| archunit | 1.5.0 | `test` scope |
| jacoco | 0.8.15 | Build plugin, outside the BOM |

### The second exception: advancing a security patch

There is a second case where the BOM is overridden, and `pom.xml` documents it:

```xml
<tomcat.version>10.1.59</tomcat.version>
<netty.version>4.1.138.Final</netty.version>
```

Spring Boot 3.5.16 pins Tomcat to 10.1.55, affected by three CRITICAL-severity CVEs
(CVE-2026-65182, CVE-2026-65905, and CVE-2026-68525), fixed starting at 10.1.58. And it
pins Netty to 4.1.135.Final, affected by CVE-2026-75595, fixed in 4.1.137.Final; Netty
arrives transitively through Lettuce, the Redis client.

The difference from overriding Hibernate by hand comes down to two points, and both
must hold for the exception to be admissible:

1. **The BOM declares the property.** `tomcat.version` exists precisely to allow this
   kind of advance; it is the mechanism Spring Boot documents in *Customizing Managed
   Dependencies*.
2. **The jump is a patch within the same minor version** (10.1.55 → 10.1.59), not a
   generational change. It does not alter the validated combination, it only brings in
   the fix.

An override of this kind is temporary by definition: **it is removed as soon as the
next Spring Boot version incorporates the patch**. From that point on it stops
advancing anything and only adds noise to `pom.xml`.

### Spring Boot support cycle

Spring Boot does not use the "LTS" label. Its policy defines support windows:

- The 3.5 line is **the last minor version of the 3.x branch**, so it receives the
  five-year enterprise support extension the policy reserves for that case.
- It is also the line the platform's base images (`justo-java21-spring3x-runner`) and
  the organization's internal libraries are built on.

**Operational consequence:** within the 3.5 line, updates are patch-level and
low-risk. The jump to Spring Boot 4 is conditioned on the platform having base images
and internal libraries for that generation.

---

## 6. Database

- **One schema per service.** Access to another service's data goes through its API.
- The schema is governed by **Flyway** (`src/main/resources/db/migration/`).
- `ddl-auto` stays at `validate`.
- A migration that has already been applied **is not modified**. The fix is introduced
  in a new one.
- Naming: `V<n>__<description_in_snake_case>.sql`
- Destructive operations (`DROP COLUMN`) are isolated in their own migration, deployed
  once no active version uses the column any more.

---

## 7. Error format: RFC 7807

All errors are emitted as `application/problem+json` from
[`GlobalExceptionHandler`](src/main/java/com/justo/bank/template/infrastructure/adapter/in/rest/GlobalExceptionHandler.java),
the single point where an exception is translated into an HTTP status code.

```json
{
  "type": "https://errors.justo.mx/not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "Sample with id 999 not found",
  "instance": "/api/v1/samples/999",
  "timestamp": "2026-09-10T22:31:39.430156Z"
}
```

A different error format per service would force every consumer — BFF, mobile app,
back office — to implement a separate parser for each of the program's 24 services.

Faced with an unhandled exception, the response includes a `traceId` and never the
original message: a stack trace can expose table names, system paths, or data.

---

## 8. Continuous integration

**Jüsto's continuous integration and deployment run in Jenkins.** This repository
contains no build or deployment workflows in GitHub Actions.

Jenkins discovers and classifies the repository through its *topics*, which are the
pipeline's wiring, not informational metadata:

```
squad-tdc  ·  backend  ·  vertical-bank
```

Without those topics the repository does not enter any pipeline. The repository
**carries no `Jenkinsfile`**: the configuration lives on the platform side, associated
by topics.

The only Actions workflow that remains is `branch-name-gate.yml`, which validates
branch names on pull requests. It comes from Jüsto's previous template and does not
take part in building or deploying.

### Quality gates do not depend on the runner

This is the property that makes it irrelevant which tool runs the pipeline: the gates
live in the Maven project and apply wherever the build is invoked.

| Gate | Where it is defined | How it runs |
|---|---|---|
| 13 architecture rules | `HexagonalArchitectureTest` (ArchUnit) | `./mvnw test` |
| Coverage threshold | `pom.xml`, JaCoCo plugin | `./mvnw verify` |
| Integration tests with real PostgreSQL | Testcontainers | `./mvnw verify` |
| Secret detection | `.gitleaks.toml` | `.githooks/pre-commit` |

**A Jenkins job that invokes `./mvnw verify` applies the first three in full**, with no
additional configuration.

The platform has confirmed that its jobs additionally incorporate secret scanning and
dependency vulnerability analysis. A joint review of the detail remains pending: it is
worth verifying that the severity threshold is equivalent, given that the discovery of
the three CRITICAL CVEs in Tomcat (see §5) came from that analysis.

### Branching model: trunk-based development

```
feature/*  ──►  main  ──►  test environments
fix/*      ──►  main
chore/*    ──►  main
                main  ──►  release/*  ──►  production
                             hotfix/*  ──►  release/*
```

**`main` is the trunk and the only long-lived branch. There is no `develop`.**
Everything merges into `main`, and `main` is what deploys to the test environments.
A `release/*` branch is cut from `main` when something goes to production; from that
point it takes stabilization only, never new functionality.

Branches off `main` are short-lived by design: the value of the model comes from
merging back quickly, not from the prefix.

`branch-name-gate.yml` enforces this. It rejects a pull request into `release/*` that
does not come from `hotfix/*`, `fix/*`, `bugfix/*`, or a revert.

Commit messages follow Conventional Commits, as the repository history does:
`chore: <imperative description>`. The description is written in English.

### Review scheme

Two gates, one before the pull request and one after.

**Before:** whoever produced the change reviews it against the final diff. When an
agent produced it, that pass is mandatory and its content is defined in
[`AGENTS.md`](AGENTS.md). An agent never approves its own work; the pass exists so the
human reviewer spends their time on the decisions, not on leftover debugging.

**After: one required reviewer per pull request.** Not two: a second mandatory approval
on a team of this size turns review into a queue, and a reviewer who knows the approval
is redundant reads less carefully than one who knows it is the only one.

Who that reviewer is depends on the paths touched, and is resolved by
[`.github/CODEOWNERS`](.github/CODEOWNERS):

| Path | Owner |
|---|---|
| Everything by default | `@justomx/squad-tdc` |
| `src/main/resources/db/migration/` | `@justomx/bank-leaders` |
| `.github/` | `@justomx/bank-leaders` |
| `pom.xml` | `@justomx/bank-leaders` |
| `ARCHITECTURE.md` | `@justomx/bank-leaders` |

Each path names **a single team**. A path listing two teams would require an approval
from each one. Sensitive paths are not reviewed by more people; they are reviewed by
different people.

Neither team exists yet in the `justomx` organisation and both have to be requested.
A team that does not exist, or that lacks write access to the repository, is ignored
without any warning. Until they are created, the required approval still works: it is
`Required approving reviews: 1` that enforces it, and that setting does not depend on
`CODEOWNERS`. What `CODEOWNERS` adds is **who** gets asked.

### Branch protection

Configured on GitHub when the repository is created, on `main` and on `release/*`. It
is not a file in the repository, which is why it is written down here:

| Setting | Value |
|---|---|
| Require a pull request before merging | Yes |
| Required approving reviews | **1** |
| Require review from Code Owners | Yes |
| Dismiss stale approvals when new commits are pushed | Yes |
| Require conversation resolution before merging | Yes |
| Require status checks to pass | Yes — `branch-name-gate` and the Jenkins check |
| Allow force pushes / deletions | No |

The mandatory human review of §10 is a separate matter: it does not add a second
approver, it determines **who** the single approver must be. A change touching card
data, funds flow, authorization, or cryptography is approved by a person qualified in
that area, never on the strength of a green build alone.

---

## 9. Contract with the platform

**This repository produces a container image.** It contains no Terraform, Helm, or
Kubernetes manifests: those artifacts live in the infrastructure repository, under
DevOps's responsibility.

```
APPLICATION REPOSITORY            INFRASTRUCTURE REPOSITORY
─────────────────────────         ──────────────────────────────
.docker/Dockerfile.api  ─────►    Jenkins builds the image
src/, pom.xml           ─────►    Jenkins publishes to ECR
                                  namespace, NetworkPolicy
                                  IRSA, ArgoCD Application
                                  RDS schema and permissions
                                  secrets in Secrets Manager
```

The interface between the two consists of four points, resolved in this template:

| # | Requirement | Implementation |
|---|---|---|
| 1 | Separate probes: `/api/health/liveness` and `/api/health/readiness` | `conf/<environment>/application.yml` |
| 2 | Complete per-environment configuration; secrets via environment variable | `conf/<environment>/application.yml` |
| 3 | Event logging in JSON to stdout | `conf/<environment>/logback.xml` |
| 4 | Image built on the platform's base images | `.docker/Dockerfile.api` |

### Platform files

| File | Purpose |
|---|---|
| `justo-app.properties` | Service metadata for the catalog: team, criticality (`tier`), owner, and links to SigNoz, Sonar, and OpenAPI |
| `.docker/Dockerfile.api` | Image recipe built on `justo-java21-builder` and `justo-java21-spring3x-runner` (platform ECR, arm64) |
| `conf/<environment>/application.yml` | **Complete** configuration for the environment. The runner copies it to `application/` and uses it as configuration |
| `conf/<environment>/deploy-properties.json` | Startup instructions for the runner: `mainClass`, JVM arguments, JMX |
| `conf/<environment>/logback.xml` | Environment logging configuration |

Environments: `local`, `staging`, and `production`. In `staging` and `production` the
database and Redis connections **have no default values**: if an environment variable
is missing, the application does not start, which is preferable to starting against
the wrong target.

#### Why the jar is flat

The runner image has no entry point of its own for the service: it starts the
application with the class named in `deploy-properties.json`. That requires a **flat**
jar, generated with `maven-shade-plugin`. Spring Boot's usual packaging nests the
classes inside `BOOT-INF/classes`, and startup by `mainClass` would fail with
`ClassNotFoundException`.

The *shade* configuration is not written in `pom.xml`: it is provided by
`spring-boot-starter-parent`, and the project only activates it and sets the
`start-class` property. Redeclaring its *transformers* breaks the build, because Maven
merges the parent's and child's lists by position.

#### Building the image locally

Requires access to the platform's ECR, requested from DevOps as a named AWS profile:

```bash
aws ecr get-login-password --profile justo-prod --region us-east-1 \
  | docker login --username AWS --password-stdin 849786826922.dkr.ecr.us-east-1.amazonaws.com
make docker-build
```

### Requests to DevOps

- **ECR** repository for the service.
- Database **schema** and user with permissions restricted to that schema.
- **IRSA** role if the service consumes SQS, SNS, DynamoDB, or Secrets Manager.
- The correct *topics* on the repository: without them Jenkins does not discover it.
- Registering the repository in Jenkins, which the platform configures based on the
  topics.

---

## 10. Security

The complete detail is in **[`docs/SECURITY.md`](docs/SECURITY.md)**. Minimum
requirements:

- Not added to the repository: PAN, CVV, credentials for any environment,
  cryptographic keys, or real personal data. The restriction covers tests, data files,
  and comments.
- Logs do not include PAN, CVV, tokens, or full request bodies that handle card data.
- Secrets are injected via environment variable from AWS Secrets Manager.
- If a secret is accidentally published: immediate rotation, followed by history
  cleanup.

### Mandatory human review

Changes affecting the following areas require review by a person, regardless of the
code's origin:

| Area | Risk |
|---|---|
| PAN, CVV, magnetic stripe | PCI DSS scope; a leak is a reportable incident |
| Funds flow | Direct financial loss |
| Authorization and resource ownership | BOLA/IDOR — top category in OWASP API Security |
| Cryptography, keys, and secrets | A mistake compromises every other control |

The requirement appears as a checklist item in the pull request template and as an
explicit restriction in [`AGENTS.md`](AGENTS.md). It does not add a second approver:
it determines who the single required reviewer must be. See "Review scheme" in §8.

---

## 11. Working with AI agents

[`AGENTS.md`](AGENTS.md) is the entry index. **It does not duplicate the conventions:
it references them.** Multiple copies of the same rule diverge quickly, at which point
none of them can be trusted.

The elements that let an agent operate correctly match the ones that help a person
onboard: `CONTEXT.md` describes the domain, `ARCHITECTURE.md` the structural
conventions, the included example shows the implementation pattern, and
`docs/TESTING.md` defines the completion criteria.

---

## 12. Verification performed

Run on September 14, 2026 on macOS (aarch64) with Temurin 21.0.12.1 LTS and
Spring Boot 3.5.16.

| Check | Result |
|---|---|
| `./mvnw verify` | 23 tests green — 4 service, 4 controller, 13 architecture, and 2 integration with real PostgreSQL 17 |
| Line coverage | 84.2% against a 70% threshold |
| Packaging | Flat jar (67 MB): no `BOOT-INF/`, main class at the root, correct `Main-Class`, 298 autoconfigurations merged |
| Startup the way the runner does it | `java -cp <jar> <mainClass>` with `conf/local/application.yml` as the **only** configuration: starts in ~6 s |
| Probes | `/api/health`, `/api/health/liveness`, and `/api/health/readiness` respond 200 |
| Functional cycle | `POST /api/v1/samples` 201 · `GET` 200 · nonexistent resource 404 in `application/problem+json` |
| Migrations | Flyway applies and validates the schema at startup |
| Service generated with `make init` | `bank-card-account-service`: no traces of the template, jar and `mainClass` renamed, starts the same way via its `mainClass` |
| Vulnerabilities | Trivy: 0 of CRITICAL severity (Tomcat 10.1.59 and Netty 4.1.138 patched) |
| Secrets | gitleaks: no findings |

**Not verified in this environment:** building the image with `.docker/Dockerfile.api`,
which requires access to the platform's ECR, and running the Jenkins pipeline. What
that image does with the jar — starting it via `mainClass` with the `conf/`
configuration — is reproduced in the table above.

## 13. Pending work

None of the following items block starting a service, though they should be resolved
before the full set of services is in production.

| # | Item | Ticket | Justification |
|---|---|---|---|
| 1 | Shared libraries: `Authorization`, `Observability`, `Http`, `Errors`, `Idempotency`, `Events`, `Crypto` | — | Their absence leads to 24 divergent implementations of the same functionality |
| 2 | Resilient HTTP client: timeout, retry with backoff, circuit breaker | TDC-011 | Calls to Evertec take between 540 and 700 ms |
| 3 | Idempotency store (DynamoDB with TTL) | TDC-008 | A retry must not produce a duplicate charge |
| 4 | Automatic scrubbing of sensitive data in logs and traces | TDC-016 | Currently depends on the judgment of whoever writes the log statement |
| 5 | Distributed tracing (OpenTelemetry) | — | Needed to diagnose chains across several services |
| 6 | Variant for CDE services | — | `evertec-connector` and `digital-card-service` require a differentiated profile |
| 7 | Review the detail of the Jenkins gates with the platform | TDC-001 | Confirmed to exist; still need to verify the severity threshold is equivalent to the one that detected the Tomcat CVEs |

---

## 14. Documentation

| Document | Content |
|---|---|
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Layers and verifiable rules. Normative |
| [`CONTEXT.md`](CONTEXT.md) | Domain and ubiquitous language. Requires per-service writing |
| [`AGENTS.md`](AGENTS.md) | Index for AI assistants |
| [`docs/TESTING.md`](docs/TESTING.md) | Testing criteria |
| [`docs/SECURITY.md`](docs/SECURITY.md) | Sensitive data and mandatory review |
| [`docs/adr/`](docs/adr/) | Architecture decision records |

### Recorded decisions

| Decision | ADR |
|---|---|
| Hexagonal architecture, one service per repository | [0001](docs/adr/0001-hexagonal-architecture.md) |
| Spring MVC with virtual threads instead of WebFlux | [0002](docs/adr/0002-mvc-virtual-threads.md) |
| Lombok excluded | [0003](docs/adr/0003-no-lombok.md) |
| Not inheriting from the corporate parent `m2-spring-parent` | [0004](docs/adr/0004-no-corporate-parent.md) |
