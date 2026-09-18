package com.justo.bank.template.infrastructure.adapter.out.persistence;

import com.justo.bank.template.domain.model.Sample;
import org.springframework.stereotype.Component;

/** JPA entity {@code <->} domain translation. Explicit, like the REST mapper. */
@Component
public class SamplePersistenceMapper {

    public Sample toDomain(SampleJpaEntity entity) {
        return new Sample(entity.getId(), entity.getName(), entity.getDescription());
    }

    public SampleJpaEntity toEntity(Sample sample) {
        return new SampleJpaEntity(sample.id(), sample.name(), sample.description());
    }
}
