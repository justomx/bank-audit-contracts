package com.justo.bank.template.application.service;

import com.justo.bank.template.application.port.in.CreateSampleCommand;
import com.justo.bank.template.application.port.in.CreateSampleUseCase;
import com.justo.bank.template.application.port.in.GetSampleByIdUseCase;
import com.justo.bank.template.application.port.out.SampleCachePort;
import com.justo.bank.template.application.port.out.SampleRepositoryPort;
import com.justo.bank.template.domain.exception.SampleNotFoundException;
import com.justo.bank.template.domain.model.Sample;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case implementation.
 *
 * <p>HARD RULE: {@code @Service} and {@code @Transactional} live ONLY in this layer.
 * Not in the domain (which does not know about Spring), nor in the adapters (which do
 * not decide transactional boundaries).
 *
 * <p>HARD RULE: explicit constructor injection. No {@code @Autowired} and no
 * {@code @RequiredArgsConstructor}. A constructor with too many parameters is a
 * useful signal that the class does too much; hiding it with an annotation silences
 * that signal.
 */
@Service
public class SampleService implements GetSampleByIdUseCase, CreateSampleUseCase {

    private final SampleRepositoryPort repository;
    private final SampleCachePort cache;

    public SampleService(SampleRepositoryPort repository, SampleCachePort cache) {
        this.repository = repository;
        this.cache = cache;
    }

    /**
     * Read using the cache-aside pattern: the cache is queried, and only on a miss
     * does it go to the database and repopulate the cache.
     */
    @Override
    @Transactional(readOnly = true)
    public Sample getById(Long id) {
        return cache.findInCache(id)
                .orElseGet(() -> {
                    Sample loaded = repository.findById(id)
                            .orElseThrow(() -> new SampleNotFoundException(id));
                    cache.saveToCache(loaded);
                    return loaded;
                });
    }

    @Override
    @Transactional
    public Sample create(CreateSampleCommand command) {
        Sample toPersist = new Sample(null, command.name(), command.description());
        return repository.save(toPersist);
    }
}
