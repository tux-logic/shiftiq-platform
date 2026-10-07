package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopSpecialtyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkshopSpecialtyCommandServiceImplTest {

    @Mock
    private WorkshopSpecialtyRepository specialtyRepository;

    @Mock
    private WorkshopRepository workshopRepository;

    private WorkshopSpecialtyCommandServiceImpl service;

    private final WorkshopId workshopId = new WorkshopId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        service = new WorkshopSpecialtyCommandServiceImpl(specialtyRepository, workshopRepository);
    }

    @Test
    @DisplayName("seeding creates the five default specialties of the catalog (H6a)")
    void seedCreatesDefaultSpecialties() {
        when(specialtyRepository.existsByWorkshopIdAndCode(eq(workshopId), anyString())).thenReturn(false);

        service.seedDefaultSpecialties(workshopId);

        var captor = ArgumentCaptor.forClass(WorkshopSpecialty.class);
        verify(specialtyRepository, times(5)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(WorkshopSpecialty::getWorkshopId, WorkshopSpecialty::getCode)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(workshopId, "GENERAL_MECHANIC"),
                        org.assertj.core.groups.Tuple.tuple(workshopId, "ELECTRICIAN"),
                        org.assertj.core.groups.Tuple.tuple(workshopId, "BODYWORK_PAINT"),
                        org.assertj.core.groups.Tuple.tuple(workshopId, "DIAGNOSTIC"),
                        org.assertj.core.groups.Tuple.tuple(workshopId, "TIRE_ALIGNMENT"));
        assertThat(captor.getAllValues()).allMatch(WorkshopSpecialty::isActive);
    }

    @Test
    @DisplayName("seeding skips codes that already exist in the workshop catalog")
    void seedSkipsExistingCodes() {
        when(specialtyRepository.existsByWorkshopIdAndCode(eq(workshopId), anyString())).thenReturn(true);

        service.seedDefaultSpecialties(workshopId);

        verify(specialtyRepository, never()).save(any(WorkshopSpecialty.class));
    }

    @Test
    @DisplayName("seeding is a no-op without a workshop id")
    void seedIgnoresNullWorkshopId() {
        service.seedDefaultSpecialties(null);

        verify(specialtyRepository, never()).save(any(WorkshopSpecialty.class));
    }

    @Test
    @DisplayName("seeded codes are unique and upper cased")
    void seededCodesAreUniqueAndUpperCase() {
        when(specialtyRepository.existsByWorkshopIdAndCode(eq(workshopId), anyString())).thenReturn(false);

        service.seedDefaultSpecialties(workshopId);

        var captor = ArgumentCaptor.forClass(WorkshopSpecialty.class);
        verify(specialtyRepository, times(5)).save(captor.capture());
        List<String> codes = captor.getAllValues().stream().map(WorkshopSpecialty::getCode).toList();
        assertThat(codes).doesNotHaveDuplicates().allMatch(code -> code.equals(code.toUpperCase()));
    }
}
