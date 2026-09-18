package com.justo.bank.template.application.port.in;

import com.justo.bank.template.domain.model.Sample;

/**
 * Inbound port (use case).
 *
 * <p>Inbound adapters — REST controllers, messaging listeners, jobs — depend ONLY
 * on this interface, never directly on {@code SampleService}. That is the rule that
 * allows adding a new transport without touching the application layer.
 *
 * <p>Implementation: {@code SampleService}.
 */
public interface CreateSampleUseCase {

    /**
     * Creates a new {@link Sample}.
     *
     * @param command creation data
     * @return the persisted sample, with the id assigned by the database
     */
    Sample create(CreateSampleCommand command);
}
