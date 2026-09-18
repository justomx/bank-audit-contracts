package com.justo.bank.template.application.port.out;

import com.justo.bank.template.domain.model.Sample;

import java.util.Optional;

/**
 * Outbound port to the cache.
 *
 * <p>Framework-free interface: it does not mention Redis. The adapter
 * ({@code RedisCacheAdapter}) is the only one that knows there is a Redis underneath.
 *
 * <p>No {@code evict} method is declared: in this template's cache-aside pattern,
 * expiration is resolved by the TTL. Add explicit invalidation only when there is a
 * write operation that must be reflected immediately.
 */
public interface SampleCachePort {

    Optional<Sample> findInCache(Long id);

    void saveToCache(Sample sample);
}
