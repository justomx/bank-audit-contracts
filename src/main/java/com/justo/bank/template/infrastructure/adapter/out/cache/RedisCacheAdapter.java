package com.justo.bank.template.infrastructure.adapter.out.cache;

import com.justo.bank.template.application.port.out.SampleCachePort;
import com.justo.bank.template.domain.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Outbound adapter to Redis (ElastiCache on AWS).
 *
 * <p>Key format: {@code "samples::{id}"}, which replicates Spring Cache's
 * {@code cacheName::key} convention so several caches can coexist in the same
 * namespace without colliding.
 *
 * <h3>The cache must never take down the service</h3>
 * <p>If Redis is down, this adapter logs the failure and responds as if it were a
 * cache miss. The consequence is higher latency, not an error for the user: an
 * unavailable cache degrades, it does not interrupt.
 */
@Component
public class RedisCacheAdapter implements SampleCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheAdapter.class);
    private static final String KEY_PREFIX = "samples::";

    private final RedisTemplate<String, Sample> redisTemplate;
    private final Duration ttl;

    public RedisCacheAdapter(RedisTemplate<String, Sample> redisTemplate,
                             @Value("${app.cache.ttl-seconds:300}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    @Override
    public Optional<Sample> findInCache(Long id) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + id));
        } catch (RuntimeException ex) {
            log.warn("Cache unavailable on read, degrading to the database [id={}]", id, ex);
            return Optional.empty();
        }
    }

    @Override
    public void saveToCache(Sample sample) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + sample.id(), sample, ttl);
        } catch (RuntimeException ex) {
            log.warn("Cache unavailable on write, continuing without caching [id={}]", sample.id(), ex);
        }
    }
}
