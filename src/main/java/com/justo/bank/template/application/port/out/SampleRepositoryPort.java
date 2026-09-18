package com.justo.bank.template.application.port.out;

import com.justo.bank.template.domain.model.Sample;

import java.util.Optional;

/**
 * Outbound port to storage.
 *
 * <p>Speaks in domain types ({@link Sample}), never in JPA entities. The adapter
 * ({@code SamplePersistenceAdapter}) is the one that translates. Thanks to that the
 * use case can be tested with a mock without starting a database.
 */
public interface SampleRepositoryPort {

    Optional<Sample> findById(Long id);

    Sample save(Sample sample);
}
