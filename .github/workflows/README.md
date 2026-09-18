# GitHub Actions workflows

**Jüsto's continuous integration and deployment run in Jenkins, not here.**

Jenkins discovers and classifies the repository through its *topics*, which are
therefore not informational metadata but the pipeline's wiring:

```
squad-tdc  ·  backend  ·  vertical-bank
```

Without those topics, the repository does not enter any pipeline.

**This repository does not carry a `Jenkinsfile`.** The pipeline configuration lives on
the platform side; DevOps associates it with the repository based on the topics.

## What must NOT be added here

- Image building or publishing to ECR
- Deployment to any environment
- Compiling and running tests

All of the above belongs to Jenkins jobs. Duplicating it in Actions produces two
sources of truth that diverge.

## What does stay

`branch-name-gate.yml` validates branch names on pull requests, in line with the
GitFlow model. It comes from Jüsto's previous template and does not take part in
building or deploying.

## Where the quality gates live

The gates never depended on GitHub Actions: they live in the Maven project and run
wherever the build is invoked, whether that is Jenkins, a local team, or a hook.

| Gate | Where it is defined | How it runs |
|---|---|---|
| Module-boundary architecture rules | `AuditContractsArchitectureTest` (ArchUnit) | `./mvnw test` |
| Dependency bans (`audit-client`, worker, Spring) | `maven-enforcer-plugin` `bannedDependencies` | bound at the `validate` phase, so `./mvnw test`/`verify` |
| Docs freshness (broken links, stale paths) | `DocsFreshnessTest` | `./mvnw test -Dtest=DocsFreshnessTest` |
| Coverage threshold | `pom.xml`, JaCoCo plugin | `./mvnw verify` |
| Secret detection | `.gitleaks.toml` | `.githooks/pre-commit` and the gitleaks binary |

It is enough for the Jenkins job to invoke `./mvnw verify` for all of the above except
secret detection to apply in full.

The platform has confirmed that its jobs additionally incorporate secret scanning and
dependency vulnerability analysis, so removing the Actions workflows does not mean a
loss of coverage. A joint review of the detail of those gates remains pending.
