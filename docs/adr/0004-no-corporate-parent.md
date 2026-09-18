# 0004 — The project does not inherit from `m2-spring-parent`

- **Status:** Accepted
- **Date:** 2026-09-11

## Context

Jüsto maintains a corporate parent POM, `com.justo:m2-spring-parent`, published on
GitHub Packages. Inheriting from it was evaluated as a way to align the template with
the rest of the organization's Java services.

## Decision

Do not inherit it. The project inherits directly from `spring-boot-starter-parent`.

## Rationale

Inspecting the parent (version 3.2.1, last commit from July 2025) showed that:

1. **It does not provide the Spring Boot BOM.** It lacks a `<parent>` and
   `<dependencyManagement>`, so it does not import `spring-boot-dependencies`.
   Inheriting it would force manually declaring the version of every dependency,
   which is precisely the practice this repository forbids (see README §5).
2. **It pins `java.version` to 17 and `spring-boot.version` to 3.2.1**, a version out
   of support. Both would have to be overridden.
3. `java-web-api-template`, which does inherit it, overrides `spring-boot.version` to
   3.5.4 in its own POM. The parent is not used for what a parent should resolve.

The precedent supports the decision: `java-hexagonal-template`, the organization's most
recent Java template, does not inherit it either.

## Consequences

**Favorable:** the Spring Boot BOM is kept, so the only version declared by hand is the
parent's. The build requires no authentication against GitHub Packages, so anyone can
compile the freshly cloned project.

**Unfavorable:** the conventions the parent provides — Checkstyle and formatter
configuration — are not inherited. They should be incorporated by copying them if
adopted.

When the service needs a `com.justo.*` library, the GitHub Packages repository and a
`settings.xml` with an access token will need to be added. That file holds credentials
and **is not version-controlled**; the reference lives in the parent's own repository.
