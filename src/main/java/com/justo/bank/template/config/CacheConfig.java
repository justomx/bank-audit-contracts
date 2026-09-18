package com.justo.bank.template.config;

import com.justo.bank.template.domain.model.Sample;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Cache configuration.
 *
 * <p>{@code @EnableCaching} lives here and not in the application class, so that
 * each cross-cutting concern is locatable by file name.
 *
 * <p>Serialization is to JSON, not Java's binary serialization: the cache content
 * remains inspectable with {@code redis-cli} during an incident.
 *
 * <p>WARNING FOR CDE SERVICES: do not cache PAN, CVV, or unencrypted card data.
 * Redis persists to disk and its backup falls within PCI DSS scope.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisTemplate<String, Sample> sampleRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Sample> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Sample.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(
            @Value("${app.cache.ttl-seconds:300}") long ttlSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttlSeconds))
                .serializeValuesWith(
                        SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory,
                                          RedisCacheConfiguration redisCacheConfiguration) {
        return RedisCacheManager.builder(factory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}
