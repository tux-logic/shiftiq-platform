package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.WorkshopSpecialtyCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateWorkshopCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.MileageIntervalConfig;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.TaxId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.OwnerRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.CompanyIdentityResult;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.IdentityValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    private IdentityValidationService identityValidationService;

    private WorkshopCommandServiceImpl service;

    private final OwnerId ownerId = new OwnerId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        service = new WorkshopCommandServiceImpl(workshopRepository, ownerRepository, workshopSpecialtyCommandService, identityValidationService);
    }

    @Test
    @DisplayName("creating a workshop seeds its default specialty catalog (H6a)")
    void createWorkshopSeedsDefaultSpecialties() {
        when(ownerRepository.existsById(ownerId)).thenReturn(true);
        when(identityValidationService.validateRuc("12345678901")).thenReturn(Optional.of(
                new CompanyIdentityResult("12345678901", "Taller Central", "ACTIVO", "HABIDO",
                        "Av. 123", "Lima", "Lima", "Miraflores", "150122", true, true, true)
        ));
        when(workshopRepository.save(any(Workshop.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var command = new CreateWorkshopCommand(
                ownerId, "Taller Central", "TC Motors", new TaxId("12345678901"), new MileageIntervalConfig(1000));

        var result = service.handle(command);

        assertThat(result).isPresent();
        verify(workshopSpecialtyCommandService).seedDefaultSpecialties(result.get().getId());
    }

    @Test
    @DisplayName("creating workshop with invalid RUC throws shared.error.identity.notFound")
    void createWorkshopWithInvalidRucThrowsNotFound() {
        when(ownerRepository.existsById(ownerId)).thenReturn(true);
        when(identityValidationService.validateRuc("12345678901")).thenReturn(Optional.empty());

        var command = new CreateWorkshopCommand(
                ownerId, "Taller Central", "TC Motors", new TaxId("12345678901"), new MileageIntervalConfig(1000));

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.notFound");
    }

    @Test
    @DisplayName("creating workshop with inactive RUC throws shared.error.identity.rucInactive")
    void createWorkshopWithInactiveRucThrowsInactive() {
        when(ownerRepository.existsById(ownerId)).thenReturn(true);
        when(identityValidationService.validateRuc("12345678901")).thenReturn(Optional.of(
                new CompanyIdentityResult("12345678901", "Taller Central", "BAJA DEFINITIVA", "HABIDO",
                        "Av. 123", "Lima", "Lima", "Miraflores", "150122", false, false, true)
        ));

        var command = new CreateWorkshopCommand(
                ownerId, "Taller Central", "TC Motors", new TaxId("12345678901"), new MileageIntervalConfig(1000));

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.rucInactive");
    }

    @Test
    @DisplayName("creating workshop with not-habido RUC throws shared.error.identity.rucNotHabido")
    void createWorkshopWithNotHabidoRucThrowsNotHabido() {
        when(ownerRepository.existsById(ownerId)).thenReturn(true);
        when(identityValidationService.validateRuc("12345678901")).thenReturn(Optional.of(
                new CompanyIdentityResult("12345678901", "Taller Central", "ACTIVO", "NO HABIDO",
                        "Av. 123", "Lima", "Lima", "Miraflores", "150122", true, false, false)
        ));

        var command = new CreateWorkshopCommand(
                ownerId, "Taller Central", "TC Motors", new TaxId("12345678901"), new MileageIntervalConfig(1000));

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.rucNotHabido");
    }
}
