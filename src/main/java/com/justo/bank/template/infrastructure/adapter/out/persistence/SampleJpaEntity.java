package com.justo.bank.template.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity — the persistent reflection of the domain.
 *
 * <p>It is a class DISTINCT from {@code Sample}, not the same one with annotations.
 * That separation is what allows a Hibernate migration to not touch the domain.
 *
 * <p>No Lombok, on purpose. The template does not use Lombok anywhere (see
 * {@code docs/adr/0003-no-lombok.md}): it is an extra annotation processor on top of
 * a freshly released JDK, and {@code @Setter} + {@code @Builder} on a managed entity
 * invites mutating it outside the transactional boundary.
 *
 * <p>The no-argument constructor is {@code protected} because JPA requires it, but it
 * must not be used from application code.
 */
@Entity
@Table(name = "samples")
public class SampleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    /** Required by JPA. Do not use directly. */
    protected SampleJpaEntity() {
    }

    public SampleJpaEntity(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
