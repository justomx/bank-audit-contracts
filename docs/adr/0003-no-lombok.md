# 0003 — The template does not use Lombok

- **Status:** Accepted
- **Date:** 2026-09-10

## Context

The previous template used Lombok in exactly one place: the JPA entity, with
`@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`.

The rest of the code uses `record` for immutable models and explicit constructors for
injection, so Lombok's usage surface was minimal.

## Decision

Do not use Lombok. The JPA entity carries hand-written accessors and a `protected`
no-argument constructor, which is what JPA requires.

## Reasons

1. **It is an annotation processor on top of a recently released JDK.** Lombok
   manipulates the compiler's syntax tree, behaving differently from a conventional
   library, and historically ends up being the first component affected by a Java
   version change. The template adopts Java 21 and Spring Boot 3.5.16.

2. **The combination of `@Setter` and `@Builder` on a managed entity is
   inadvisable.** It makes it easy to mutate the entity outside the transactional
   boundary, a common source of persistence defects that are hard to diagnose.

3. **The savings were limited to about twenty lines in a single class.**

## Alternatives considered

**Keep it.** Matches the previous template's criteria and, presumably, that of other
repositories in the organization. Rejected for the reasons above, though the decision
is reversible at no cost.

## Consequences

**Favorable:** one fewer annotation processor; JPA entities with no public setter
methods.

**Unfavorable:** longer entities and a style divergence from organization repositories
that use Lombok.

## How to revert it

Add the `org.projectlombok:lombok` dependency (version managed by the BOM) with
`<optional>true</optional>`, plus its `annotationProcessorPath` in the
`maven-compiler-plugin`. It is a fifteen-line change in `pom.xml`.

If reverted, the `ARCHITECTURE.md` rule remains in force: `@RequiredArgsConstructor`
for injection is not allowed; the constructor is declared explicitly.
