package com.justo.bank.template.infrastructure.adapter.out.persistence;

import com.justo.bank.template.application.port.out.SampleRepositoryPort;
import com.justo.bank.template.domain.model.Sample;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Outbound adapter: implements the repository port against JPA.
 *
 * <p>Receives and returns DOMAIN types. The JPA entity never crosses upward: if it
 * did, the application layer would end up tied to Hibernate and its managed entity
 * lifecycle.
 */
@Component
public class SamplePersistenceAdapter implements SampleRepositoryPort {

    private final SampleJpaRepository repository;
    private final SamplePersistenceMapper mapper;

    public SamplePersistenceAdapter(SampleJpaRepository repository, SamplePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Sample> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Sample save(Sample sample) {
        SampleJpaEntity saved = repository.save(mapper.toEntity(sample));
        return mapper.toDomain(saved);
    }
}
