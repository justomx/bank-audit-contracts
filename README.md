# audit-contracts

Shared audit event/record types for Justo's TDC audit trail. Pure JDK, no internal
dependency, no deployable. See [`CONTEXT.md`](CONTEXT.md) for the domain and
[`ARCHITECTURE.md`](ARCHITECTURE.md) for the verifiable rules.

## Requirements

| Tool | Version | Note |
|---|---|---|
| JDK | **21** | See `.sdkmanrc` |
| Maven | — | No installation required: the repository includes `./mvnw` |

## Build and verify

```bash
make test        # unit tests and architecture rules
make verify       # adds the coverage threshold
make docs-check   # docs-freshness gate
make build        # packages the jar
```

Equivalent Maven commands are documented in [`AGENTS.md`](AGENTS.md).

## Consuming this library

Once DevOps publishes a release, a consumer resolves it from GitHub Packages:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/justomx/bank-audit-contracts</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.justo.bank</groupId>
    <artifactId>audit-contracts</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

GitHub Packages requires authentication even for reads. Each developer and Jenkins
itself needs a token with `read:packages` scope, configured in **their own**
`~/.m2/settings.xml` under server id `github` — never committed to this repository:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>${env.GITHUB_ACTOR}</username>
            <password>${env.GITHUB_TOKEN}</password>
        </server>
    </servers>
</settings>
```

### Local development across repositories

Before a release is published, a consumer under active local development can pick up
uncommitted changes by installing this library into the local Maven repository:

```bash
./mvnw install
```

### Publishing

**Publishing is done only by DevOps, via Jenkins.** This repository never publishes:
no credentials for GitHub Packages exist here, in any form. See
[`distributionManagement`](pom.xml) for the target coordinates and
`docs/adr/0001-standalone-contracts-library.md` for the decision.

### Versioning

Semantic versioning. Within a major version, changes are additive-only: no field
removal, no change to a field's type or meaning. A breaking change requires a new
major version. See
[docs/adr/0001-standalone-contracts-library.md](docs/adr/0001-standalone-contracts-library.md).

## Documentation

| Document | Content |
|---|---|
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Verifiable rules. Normative |
| [`CONTEXT.md`](CONTEXT.md) | Domain and ubiquitous language |
| [`AGENTS.md`](AGENTS.md) | Index for AI assistants |
| [`docs/TESTING.md`](docs/TESTING.md) | Testing criteria |
| [`docs/SECURITY.md`](docs/SECURITY.md) | Sensitive data and mandatory review |
| [`docs/adr/`](docs/adr/) | Architecture decision records |
| [`docs/tech-debt.md`](docs/tech-debt.md) | Known gaps of this library |
