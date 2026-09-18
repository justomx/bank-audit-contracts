package com.justo.bank.template.infrastructure.adapter.out.persistence;

import com.justo.bank.template.domain.model.Sample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the persistence adapter.
 *
 * <p>{@code IT} suffix: run by failsafe in {@code mvn verify}, not by surefire in
 * {@code mvn test}. Requires Docker to be running.
 *
 * <p>Uses a real Postgres via Testcontainers instead of H2, for a concrete reason:
 * H2 accepts SQL that Postgres rejects, and here the Flyway migrations are also
 * validated against the real engine. A test that passes against H2 and fails in
 * production is worse than having no test.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("SamplePersistenceAdapter (integration)")
class SamplePersistenceAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private SamplePersistenceAdapter adapter;

    @Test
    @DisplayName("saves and retrieves returning domain types")
    void savesAndRetrieves() {
        Sample saved = adapter.save(new Sample(null, "integration", "from testcontainers"));

        assertThat(saved.id()).isNotNull();

        Optional<Sample> retrieved = adapter.findById(saved.id());

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().name()).isEqualTo("integration");
    }

    @Test
    @DisplayName("returns an empty Optional when the id does not exist")
    void returnsEmptyWhenNotFound() {
        assertThat(adapter.findById(999_999L)).isEmpty();
    }
}
