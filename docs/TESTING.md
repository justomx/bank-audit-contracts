# Testing

Criteria that define when a change is considered tested in this repository.

## Categories

| Category | Suffix | Runner | Docker | Applies to |
|---|---|---|---|---|
| Unit | `*Test.java` | surefire (`mvn test`) | No | Always |
| Integration | `*IT.java` | failsafe (`mvn verify`) | Yes | Outbound adapters |

The suffix is not a cosmetic convention: it determines which plugin runs the test. An
integration test named `...Test` will run without Docker available and fail. A unit
test named `...IT` will be excluded from the fast cycle and its coverage will
disappear from the regular verification.

## Strategy by layer

| Layer | Mechanism | Example |
|---|---|---|
| `domain/` | Unit test with no doubles. Contains rules, not collaborations | — |
| `application/service/` | Unit test with ports replaced by doubles. No Spring context | `SampleServiceTest` |
| `adapter/in/rest/` | `@WebMvcTest` with ports replaced | `SampleControllerTest` |
| `adapter/out/persistence/` | `@SpringBootTest` with Testcontainers | `SamplePersistenceAdapterIT` |
| Architecture | ArchUnit | `HexagonalArchitectureTest` |

### Structural degradation indicator

If `SampleServiceTest` ever needed `@SpringBootTest` to pass, the layer separation
would have been lost. A use case that depends on interfaces is tested with doubles and
without an inversion-of-control container. Losing that property indicates a concrete
dependency was introduced into the application layer.

## Testcontainers versus in-memory databases

Integration tests use real PostgreSQL through Testcontainers.

H2 accepts SQL statements that PostgreSQL rejects. A test that passes against H2 and
fails in production is more harmful than having no test, because of the unfounded
confidence it creates. Additionally, this mechanism validates the Flyway migrations
against the target engine.

The associated cost is a dependency on Docker and a longer execution time.

## Coverage

The threshold is defined in `pom.xml` through the `jacoco.line.coverage` property.
Falling short of it breaks the build.

The `*Application` class and the `config/` package are excluded, since their coverage
inflates the metric without adding information.

The threshold is raised as the service matures and never lowered. When a change falls
short of the set value, the missing test should be added.

## Elements excluded from testing

Writing tests with no value creates maintenance cost and degrades the signal:

- Accessor methods and `record`s with no logic.
- Direct-correspondence mappers: they are covered by their consumers' tests.
- Spring configuration. The application startup already exercises it.
- Third-party libraries.

## Naming

Tests are named after the observable behavior, not after the method invoked:

```java
@Test
@DisplayName("returns from the cache without touching the database on a hit")
void returnsFromCacheOnHit() { ... }
```

Good name: `returnsEmptyWhenNotFound`.
Bad name: `testFindById2`.

When continuous integration fails, the test name is the first piece of information
available for diagnosis.

## Verification before a pull request

```bash
make verify
```

Runs unit tests, architecture rules, integration tests, and the coverage threshold,
matching the continuous integration configuration.
