# Architecture

> **Normative** document. Its content is verified by
> [`HexagonalArchitectureTest`](src/test/java/com/justo/bank/template/architecture/HexagonalArchitectureTest.java),
> which runs on every pull request. Any change to a rule in this document requires the
> corresponding change in the test.

## Model

Hexagonal architecture (ports and adapters). Three layers with a single allowed
dependency direction:

```
        infrastructure  ──────►  application  ──────►  domain
        (adapters)               (use cases)           (rules)

        The reverse direction is not allowed.
```

```
src/main/java/com/justo/bank/<service>/
├── domain/
│   ├── model/               Entities and value objects. Pure Java
│   └── exception/           Business exceptions, no HTTP status codes
├── application/
│   ├── port/in/             Use cases (interfaces) and commands
│   ├── port/out/            External dependencies of the use case
│   └── service/             Implementation. Where @Service and @Transactional live
├── infrastructure/
│   └── adapter/
│       ├── in/rest/         Controllers, DTOs, mappers, error handling
│       ├── in/messaging/    Queue consumers
│       ├── out/persistence/ JPA entities, repositories, adapters
│       ├── out/cache/       Redis
│       └── out/client/      HTTP calls to other services or providers
└── config/                  Cross-cutting configuration
```

**One repository corresponds to one service, one deployable, and one bounded
context.** The structure does not include a `modules/` level.

Services with several aggregates organize them as subpackages within
`domain/model/` — for example `domain/model/account/` and `domain/model/card/` — and
not as parallel hexagons.

## Verifiable rules

Each rule has a test that enforces it. The right-hand column identifies the
corresponding method in `HexagonalArchitectureTest`.

| # | Rule | Test |
|---|---|---|
| 1 | `domain/` does not depend on `application/` or `infrastructure/` | `the_domain_does_not_depend_on_anyone` |
| 2 | `application/` does not depend on `infrastructure/` | `the_application_does_not_depend_on_infrastructure` |
| 3 | `domain/` does not import Spring, JPA, Jackson, or Swagger | `the_domain_does_not_know_about_frameworks` |
| 4 | Ports do not import frameworks | `ports_do_not_know_about_frameworks` |
| 5 | `@Transactional` on methods, only in `application/service/` | `method_level_transactional_only_in_application_services` |
| 6 | `@Transactional` on classes, only in `application/service/` | `class_level_transactional_only_in_application_services` |
| 7 | `@Entity` only in `infrastructure/adapter/out/persistence/` | `jpa_entities_only_in_the_persistence_adapter` |
| 8 | `@RestController` only in `infrastructure/adapter/in/rest/` | `controllers_only_in_the_rest_adapter` |
| 9 | Inbound adapters depend on ports, not on services | `incoming_adapters_do_not_use_application_services_directly` |
| 10 | No `@Autowired` on fields | `no_autowired_on_fields` |
| 11 | No `@Autowired` on setter methods | `no_autowired_on_setters` |
| 12 | No `System.out` or `System.err` | `no_console_output` |
| 13 | No `java.util.logging` | `no_java_util_logging` |

Rules 5 and 6 are formulated separately because `@Transactional` can be declared both
on a class and on a method, and a single expression does not cover both cases. The
same consideration applies to rules 10 and 11 regarding `@Autowired`.

### Behavior when there are no matches

`src/test/resources/archunit.properties` sets `archRule.failOnEmptyShould=false`.
Without that setting, a rule whose selection clause matches no class would cause a
failure. In a freshly generated service, after removing the sample with `make init`,
there are no classes annotated with `@Entity`, `@RestController`, or `@Transactional`,
and the corresponding rules would break the build before a single line is written.

The trade-off is that a typo in a package name does not show up on its own. Any change
to `BASE_PACKAGE` must be accompanied by checking that the rules are still evaluating
classes.

### Rationale for rule 3

Adding `@Entity` to the domain model avoids writing a mapper. The cost shows up later,
when a Hibernate upgrade changes an annotation's behavior and forces intervention in
the business rules to fix a persistence problem. In a regulated financial product, that
intervention means recertifying logic that has undergone no functional change.

Writing a mapper is low cost. Rewriting the domain is not.

## Naming conventions

| Element | Pattern | Example |
|---|---|---|
| Inbound port | `<Action><Noun>UseCase` | `CreateSampleUseCase` |
| Command | `<Action><Noun>Command` | `CreateSampleCommand` |
| Outbound port | `<Noun><Role>Port` | `SampleRepositoryPort` |
| Service | `<Noun>Service` | `SampleService` |
| Outbound adapter | `<Technology><Noun>Adapter` | `RedisCacheAdapter` |
| JPA entity | `<Noun>JpaEntity` | `SampleJpaEntity` |
| Inbound DTO | `<Action><Noun>Request` | `CreateSampleRequest` |
| Outbound DTO | `<Noun>Response` | `SampleResponse` |
| Unit test | `<Class>Test` | `SampleServiceTest` |
| Integration test | `<Class>IT` | `SamplePersistenceAdapterIT` |

All identifiers, comments, and test names are in English (see `AGENTS.md`, Language).

The test suffix determines which plugin runs it: surefire processes `*Test` and
failsafe processes `*IT`. An integration test named `...Test` will run without Docker
available and fail.

## Persistence

- **One schema per service.** Access to another service's data goes through its API.
- The service's database user has permissions limited to its own schema.
- The schema is governed by **Flyway**. `ddl-auto` stays at `validate`.
- A migration that has already been applied is not modified; the fix is introduced in
  a new one.
- Destructive operations (`DROP COLUMN`) are isolated in their own migration, deployed
  once no active version uses the column any more.

## Errors

All errors are emitted according to **RFC 7807** (`application/problem+json`) from
`GlobalExceptionHandler`, the single point where an exception is translated into an
HTTP status code.

A per-service error format would force every consumer — BFF, mobile app, back office —
to implement a separate parser for each of the program's services.

## Scope outside this repository

| Topic | Location |
|---|---|
| Namespace, NetworkPolicy, IRSA | Infrastructure repository (DevOps) |
| Kubernetes manifests, ArgoCD | Infrastructure repository (DevOps) |
| Terraform | Infrastructure repository (DevOps) |
| WAF and ingress rules | Infrastructure repository (DevOps) |

This repository produces a container image. The interface with the platform is
documented in the README, section 9.
