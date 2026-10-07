package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.WorkshopSpecialtyCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.WorkshopSpecialtyQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeactivateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtiesByWorkshopIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtyByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateWorkshopSpecialtyResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateWorkshopSpecialtyResource;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkshopSpecialtiesControllerTest {

    @Mock
    private WorkshopSpecialtyCommandService commandService;

    @Mock
    private WorkshopSpecialtyQueryService queryService;

    @Mock
    private MultiTenancySecurityService securityService;

    private WorkshopSpecialtiesController controller;

    @BeforeEach
    void setUp() {
        controller = new WorkshopSpecialtiesController(commandService, queryService, securityService);
    }

    @Test
    @DisplayName("createSpecialty returns 201 on success")
    void createSpecialtySuccess() {
        UUID workshopId = UUID.randomUUID();
        var resource = new CreateWorkshopSpecialtyResource("Mecánica", "MEC", "Desc");
        var created = new WorkshopSpecialty(new WorkshopId(workshopId), "Mecánica", "MEC", "Desc");

        when(commandService.handle(any(CreateWorkshopSpecialtyCommand.class))).thenReturn(Optional.of(created));

        var response = controller.createSpecialty(workshopId, resource);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Mecánica");
        verify(securityService).validateWorkshopAccess(workshopId);
    }

    @Test
    @DisplayName("createSpecialty validates workshop access")
    void createSpecialtyDeniesAccess() {
        UUID workshopId = UUID.randomUUID();
        var resource = new CreateWorkshopSpecialtyResource("Mecánica", "MEC", "Desc");

        doThrow(new AccessDeniedException("Forbidden")).when(securityService).validateWorkshopAccess(workshopId);

        assertThatThrownBy(() -> controller.createSpecialty(workshopId, resource))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getSpecialties lists all specialties for workshop")
    void getSpecialtiesListsActive() {
        UUID workshopId = UUID.randomUUID();
        var s1 = new WorkshopSpecialty(new WorkshopId(workshopId), "Mecánica", "MEC", null);
        var s2 = new WorkshopSpecialty(new WorkshopId(workshopId), "Electricidad", "ELEC", null);

        when(queryService.handle(any(GetWorkshopSpecialtiesByWorkshopIdQuery.class))).thenReturn(List.of(s1, s2));

        var response = controller.getSpecialties(workshopId, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        verify(securityService).validateWorkshopAccess(workshopId);
    }

    @Test
    @DisplayName("deactivateSpecialty returns 204 when successfully deactivated")
    void deactivatesSpecialty() {
        UUID workshopId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        var specialty = new WorkshopSpecialty(new WorkshopId(workshopId), "Mecánica", "MEC", null);

        when(queryService.handle(any(GetWorkshopSpecialtyByIdQuery.class))).thenReturn(Optional.of(specialty));
        when(commandService.handle(any(DeactivateWorkshopSpecialtyCommand.class))).thenReturn(true);

        var response = controller.deactivateSpecialty(workshopId, specialtyId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
