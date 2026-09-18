package com.justo.bank.template.domain.model;

/**
 * Domain model.
 *
 * <p>HARD RULE: this class carries NO framework annotations. Not JPA
 * ({@code @Entity}, {@code @Column}), not Jackson ({@code @JsonProperty}), not Spring.
 * It is pure Java and compiles without any external dependency.
 *
 * <p>The reason is not purism: it is that a Hibernate or Jackson version bump must
 * not force changes to the business rules. The persistent counterpart lives in
 * {@code infrastructure/adapter/out/persistence/SampleJpaEntity} and is translated
 * with an explicit mapper.
 *
 * <p>A {@code record} is used because the model is immutable. When the aggregate has
 * invariants to validate, switch to a class with a private constructor and a static
 * factory — but the "no annotations" rule does not change.
 */
public record Sample(
        Long id,
        String name,
        String description
) {
}
