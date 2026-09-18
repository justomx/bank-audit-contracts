# Testing

Criteria that define when a change is considered tested in this repository.

## Categories

| Category | Suffix | Runner | Applies to |
|---|---|---|---|
| Unit / architecture | `*Test.java` | surefire (`mvn test`) | Always |

This library has no outbound adapter and no Docker dependency: there is no
integration-test category here.

## Architecture rules

`AuditContractsArchitectureTest` (rules C2, C4, C5, see `ARCHITECTURE.md`) must stay
non-vacuous: `archRule.failOnEmptyShould=true` in
`src/test/resources/archunit.properties`, except rule C5, which sets
`allowEmptyShould(true)` on itself because no public method exists yet in this module.
A rule whose `that()` predicate matches zero classes anywhere else is a defect, not an
expected state, and must be rescoped or removed.

## Coverage

The threshold is defined in `pom.xml` through the `jacoco.line.coverage` property
(currently `0.70`). Falling short of it breaks the build. The threshold is raised as
the module matures and never lowered.

Until the first real contract type exists the check passes vacuously: JaCoCo finds
nothing to instrument. See TD-07 in [`tech-debt.md`](tech-debt.md).

## Naming

Tests are named after the observable behavior, not after the method invoked:

```java
@Test
void c4_fields_are_final() { ... }
```

When continuous integration fails, the test name is the first piece of information
available for diagnosis.

## Verification before a pull request

```bash
make verify
```

Runs unit tests, architecture rules, docs freshness, and the coverage threshold.
