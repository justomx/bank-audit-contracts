package com.justo.bank.template.application.service;

import com.justo.bank.template.application.port.in.CreateSampleCommand;
import com.justo.bank.template.application.port.out.SampleCachePort;
import com.justo.bank.template.application.port.out.SampleRepositoryPort;
import com.justo.bank.template.domain.exception.SampleNotFoundException;
import com.justo.bank.template.domain.model.Sample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for the use case.
 *
 * <p>No Spring and no database: the ports are replaced by mocks. That is possible
 * precisely because the service depends on interfaces and not on adapters. If this
 * test ever needed {@code @SpringBootTest} to pass, it would be the signal that the
 * hexagon broke.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SampleService")
class SampleServiceTest {

    @Mock
    private SampleRepositoryPort repository;

    @Mock
    private SampleCachePort cache;

    @InjectMocks
    private SampleService service;

    @Test
    @DisplayName("returns from the cache without touching the database on a hit")
    void returnsFromCacheOnHit() {
        Sample cached = new Sample(1L, "from-cache", "d");
        when(cache.findInCache(1L)).thenReturn(Optional.of(cached));

        Sample result = service.getById(1L);

        assertThat(result).isEqualTo(cached);
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("reads from the database and repopulates the cache on a miss")
    void readsFromDatabaseOnMissAndRepopulatesCache() {
        Sample fromDatabase = new Sample(1L, "from-database", "d");
        when(cache.findInCache(1L)).thenReturn(Optional.empty());
        when(repository.findById(1L)).thenReturn(Optional.of(fromDatabase));

        Sample result = service.getById(1L);

        assertThat(result).isEqualTo(fromDatabase);
        verify(cache).saveToCache(fromDatabase);
    }

    @Test
    @DisplayName("throws SampleNotFoundException when not found in cache or database")
    void throwsWhenNotFoundInCacheOrDatabase() {
        when(cache.findInCache(99L)).thenReturn(Optional.empty());
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(SampleNotFoundException.class)
                .hasMessageContaining("99");

        verify(cache, never()).saveToCache(any());
    }

    @Test
    @DisplayName("creates with null id and returns the id assigned by the database")
    void createsAndReturnsAssignedId() {
        CreateSampleCommand command = new CreateSampleCommand("new", "desc");
        when(repository.save(new Sample(null, "new", "desc")))
                .thenReturn(new Sample(7L, "new", "desc"));

        Sample created = service.create(command);

        assertThat(created.id()).isEqualTo(7L);
    }
}
