# Security

This service is part of a regulated financial product. The following content is not a
generic catalog of best practices, but the set of rules applicable to this repository.

## Data excluded from the repository

The restriction covers every file, including test files, data files, and comments:

| Data | Reason |
|---|---|
| PAN (full card number) | PCI DSS scope. The exclusion covers test data too, except that documented by the provider |
| CVV / CVV2 | PCI DSS forbids storing it, even encrypted |
| Magnetic stripe | Same |
| Credentials for any environment | Managed in AWS Secrets Manager |
| AES, private, or signing keys | Managed in KMS |
| Real personal data | CURP, RFC, address, phone number |

Verification is performed by `gitleaks`, run in continuous integration (**TDC-001**)
and configured in [`.gitleaks.toml`](../.gitleaks.toml). The pre-commit hook is still
pending integration.

If a secret is accidentally published, the order of action is: **immediate rotation**,
followed by history cleanup. A secret present in a repository is considered
compromised regardless of whether it is removed in the following commit.

## Logs

Logs are emitted as JSON to stdout and collected by the platform. The following are
excluded from any log entry:

- Full PAN. The last four digits are allowed when there is a justified need
- CVV, in any form
- Authentication tokens, session cookies, `Authorization` headers
- Full request or response bodies in operations that handle card data

`GlobalExceptionHandler` enforces this rule: the response to the client includes a
`traceId` and the detail stays in the log. **Automatic scrubbing** of sensitive fields
is not implemented (**TDC-016**); until it is added, the responsibility falls on
whoever writes the log statement.

## Configuration and secrets

- Secrets are injected through an **environment variable**, sourced from AWS Secrets
  Manager.
- No default value in `application.yml` may be sensitive.
- `application-local.yml` IS version-controlled: it contains only local container
  credentials, with no protective value.

## Errors returned to the client

`GlobalExceptionHandler` does not expose the original message of an unhandled
exception. A database driver message or a stack trace can reveal table names, system
paths, or data fragments.

The pattern applied hands the client a `traceId` and keeps the detail in the log.

## CDE services

Services that decrypt or handle card data — `bank-evertec-connector` and
`bank-digital-card-service` — are subject to additional requirements:

- Dedicated namespace and node group
- AES keys live **exclusively** in those services, never in a shared library. A key
  held in a shared library pulls **all** of its consumers into PCI scope.
- Unencrypted card data is never cached: ElastiCache persists to disk and its backups
  fall within the standard's scope.
- TLS verification stays **always** on. Using `-k` or `verify=false` is acceptable for
  a manual terminal check, never in production code.

## Mandatory human review

Changes affecting any of the following areas **are not merged without review by a
person**, regardless of the code's origin:

| Area | Risk |
|---|---|
| Handling of PAN, CVV, or magnetic stripe | PCI DSS scope; a leak is a reportable incident |
| Funds flow: payments, accounting, reconciliation | Direct financial loss |
| Authorization and resource ownership | BOLA/IDOR — top category in OWASP API Security |
| Cryptography, keys, and secrets | A mistake compromises every other control |

The requirement appears as a checklist item in the pull request template and as an
explicit restriction in `AGENTS.md`.

**Rationale:** code generation has sped up considerably; its review has not. In these
four areas the cost of a mistake remains unchanged, so the control stays in place.

## Dependencies

- Trivy runs in continuous integration and breaks the build on a **CRITICAL**-severity
  CVE with a fix available.
- Dependabot, once configured for the `justomx` organization.
- Adding a dependency is an explicit decision, not an incidental change within another
  pull request.
