# 0002 — Spring MVC with virtual threads, not WebFlux

- **Status:** Accepted
- **Date:** 2026-09-10

## Context

Using Spring WebFlux in the BFF layer was proposed for "high transactionality".

The argument was valid **before Java 21**: one operating-system thread per request did
not scale when the load consists of I/O waits. WebFlux was conceived to solve that
limitation.

Java 21 incorporates virtual threads as a final feature (JEP 444).

## Decision

Spring MVC with `spring.threads.virtual.enabled=true`, which already comes enabled in
`application.yml`. `StructuredTaskScope` is used for calls in parallel.

## Alternatives considered

**Spring WebFlux.** Rejected because of its cost, which falls on the whole team: stack
traces of little diagnostic use in production, incompatibility with blocking JDBC
— it would require R2DBC —, the propagation of the model to the entire chain of
libraries, where a single blocking call compromises the event loop, and a considerable
learning curve.

## The data that decided it

Measuring the Evertec environment, each call takes between **540 and 700 ms**. For a
screen that needs four calls:

| Approach | Total | What achieves it |
|---|---|---|
| Sequential | ~2.4 s | — |
| Parallel | ~0.7 s | virtual threads **or** WebFlux, either one |
| With cache | ~0.05 s | Redis with a reduced TTL |

The substantial improvement comes from **parallelism and caching**, not from the
threading model. Parallelism is obtained just as well through structured concurrency,
over conventional, readable code.

## Consequences

**Favorable:** sequential, diagnosable code; JDBC and the Spring ecosystem operate
without changes; one configuration line instead of a paradigm shift.

**Unfavorable:** faced with an actual requirement for continuous streaming — SSE,
websockets, sustained notifications — WebFlux would be the right model and the
decision would need to be revisited. The BFF is the system's smallest-footprint layer,
with no data or rules of its own, so rewriting it carries a low cost.

## Review conditions

- Measurement confirms that virtual threads do not cover the target concurrency.
- A continuous-streaming requirement appears.
