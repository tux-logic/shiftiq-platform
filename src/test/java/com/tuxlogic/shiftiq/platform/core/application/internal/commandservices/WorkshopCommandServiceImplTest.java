package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.WorkshopSpecialtyCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateWorkshopCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.MileageIntervalConfig;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.TaxId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.OwnerRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkshopCommandServiceImplTest {

    @Mock
    private WorkshopRepository workshopRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private WorkshopSpecialtyCommandService workshopSpecialtyCommandService;

    private WorkshopCommandServiceImpl service;

    private final OwnerId ownerId = new OwnerId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        service = new WorkshopCommandServiceImpl(workshopRepository, ownerRepository, workshopSpecialtyCommandService);
    }

    @Test
    @DisplayName("creating a workshop seeds its default specialty catalog (H6a)")
    void createWorkshopSeedsDefaultSpecialties() {
        when(ownerRepository.existsById(ownerId)).thenReturn(true);
        when(workshopRepository.save(any(Workshop.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var command = new CreateWorkshopCommand(
                ownerId, "Taller Central", "TC Motors", new TaxId("12345678901"), new MileageIntervalConfig(1000));

        var result = service.handle(command);

        assertThat(result).isPresent();
        verify(workshopSpecialtyCommandService).seedDefaultSpecialties(result.get().getId());
    }
}
