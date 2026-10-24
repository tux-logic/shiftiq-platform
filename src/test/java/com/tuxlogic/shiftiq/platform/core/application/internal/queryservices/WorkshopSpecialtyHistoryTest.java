package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.entities.WorkshopSpecialtyRevision;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtyHistoryQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopSpecialtyHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkshopSpecialtyHistoryTest {

    @Mock
    private WorkshopSpecialtyHistoryRepository repository;

    private WorkshopSpecialtyHistoryQueryServiceImpl service;

    private final WorkshopId workshopId = new WorkshopId(UUID.randomUUID());
    private final WorkshopSpecialtyId specialtyId = new WorkshopSpecialtyId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        service = new WorkshopSpecialtyHistoryQueryServiceImpl(repository);
    }

    @Test
    @DisplayName("query with specialtyId returns specific revisions list")
    void queryWithSpecialtyIdReturnsRevisions() {
        var revision = new WorkshopSpecialtyRevision(
                UUID.randomUUID(), specialtyId, workshopId, "Frenos y Suspensión",
                "BRAKES", "Mantenimiento del sistema de frenos", true, "UPDATED",
                UUID.randomUUID(), Instant.now()
        );

        when(repository.findByWorkshopIdAndSpecialtyId(workshopId, specialtyId))
                .thenReturn(List.of(revision));

        var query = new GetWorkshopSpecialtyHistoryQuery(workshopId, specialtyId);
        var result = service.handle(query);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("BRAKES");
        verify(repository).findByWorkshopIdAndSpecialtyId(workshopId, specialtyId);
    }

    @Test
    @DisplayName("query without specialtyId returns all revisions for workshop")
    void queryWithoutSpecialtyIdReturnsAllWorkshopRevisions() {
        var revision = new WorkshopSpecialtyRevision(
                UUID.randomUUID(), specialtyId, workshopId, "Frenos y Suspensión",
                "BRAKES", "Mantenimiento del sistema de frenos", true, "SEEDED",
                UUID.randomUUID(), Instant.now()
        );

        when(repository.findByWorkshopId(workshopId)).thenReturn(List.of(revision));

        var query = new GetWorkshopSpecialtyHistoryQuery(workshopId, null);
        var result = service.handle(query);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).changeType()).isEqualTo("SEEDED");
        verify(repository).findByWorkshopId(workshopId);
    }

    @Test
    @DisplayName("query with null workshopId throws IllegalArgumentException")
    void queryWithNullWorkshopIdThrows() {
        assertThatThrownBy(() -> new GetWorkshopSpecialtyHistoryQuery(null, specialtyId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
