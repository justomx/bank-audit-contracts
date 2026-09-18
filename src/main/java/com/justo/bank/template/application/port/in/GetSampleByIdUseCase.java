package com.justo.bank.template.application.port.in;

import com.justo.bank.template.domain.model.Sample;

/** Inbound port: query by identifier. */
public interface GetSampleByIdUseCase {

    /**
     * @param id sample identifier
     * @return the sample found
     * @throws com.justo.bank.template.domain.exception.SampleNotFoundException if it does not exist
     */
    Sample getById(Long id);
}
