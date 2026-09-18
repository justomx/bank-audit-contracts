package com.justo.bank.template.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository.
 *
 * <p>It is an infrastructure detail, and that is why the application layer does NOT
 * inject it: it injects {@code SampleRepositoryPort}. This repository is consumed
 * only by {@code SamplePersistenceAdapter}.
 */
@Repository
public interface SampleJpaRepository extends JpaRepository<SampleJpaEntity, Long> {
}
