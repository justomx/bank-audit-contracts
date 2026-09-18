package com.justo.bank.template.infrastructure.adapter.in.rest.mapper;

import com.justo.bank.template.application.port.in.CreateSampleCommand;
import com.justo.bank.template.domain.model.Sample;
import com.justo.bank.template.infrastructure.adapter.in.rest.dto.CreateSampleRequest;
import com.justo.bank.template.infrastructure.adapter.in.rest.dto.SampleResponse;
import org.springframework.stereotype.Component;

/**
 * HTTP DTO {@code <->} domain translation.
 *
 * <p>Hand-written, not generated. With explicit mapping, adding a field to the domain
 * does NOT automatically expose it in the API: it has to be decided here. In a
 * service that handles card data, that friction is a protection, not a nuisance.
 */
@Component
public class SampleRestMapper {

    public SampleResponse toResponse(Sample sample) {
        return new SampleResponse(sample.id(), sample.name(), sample.description());
    }

    public CreateSampleCommand toCommand(CreateSampleRequest request) {
        return new CreateSampleCommand(request.name(), request.description());
    }
}
