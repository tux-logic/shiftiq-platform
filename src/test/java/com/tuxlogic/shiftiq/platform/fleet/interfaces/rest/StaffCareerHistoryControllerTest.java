package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest;

import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.StaffCareerHistoryQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.entities.StaffCareerSnapshot;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetStaffCareerHistoryByEmployeeAndBranchQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffCareerHistoryControllerTest {

    @Mock
    private StaffCareerHistoryQueryService queryService;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    private StaffCareerHistoryController controller;

    private final UUID branchId = UUID.randomUUID();
    private final UUID employeeId = UUID.randomUUID();
    private final UUID registrationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new StaffCareerHistoryController(queryService, multiTenancySecurityService);
    }

    @Test
    @DisplayName("authorized user retrieves staff career history successfully")
    void getStaffCareerHistorySuccess() {
        var snapshot = new StaffCareerSnapshot(
                UUID.randomUUID(), registrationId, employeeId, new BranchId(branchId),
                "TIRE_ALIGNMENT", "Alineación y Suspensión", new BigDecimal("2200"),
                EmployeeRegistrationStatus.ACTIVE, "UPDATED", "Specialty updated",
                UUID.randomUUID(), Instant.now()
        );

        when(queryService.handle(any(GetStaffCareerHistoryByEmployeeAndBranchQuery.class)))
                .thenReturn(List.of(snapshot));

        var response = controller.getStaffCareerHistory(branchId, employeeId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).speciality()).isEqualTo("TIRE_ALIGNMENT");
        assertThat(response.getBody().get(0).salary()).isEqualByComparingTo("2200");
        verify(multiTenancySecurityService).validateBranchAccess(branchId);
    }

    @Test
    @DisplayName("unauthorized user access to branch history throws AccessDeniedException")
    void getStaffCareerHistoryUnauthorizedThrows() {
        doThrow(new AccessDeniedException("Forbidden")).when(multiTenancySecurityService).validateBranchAccess(branchId);

        assertThatThrownBy(() -> controller.getStaffCareerHistory(branchId, employeeId))
                .isInstanceOf(AccessDeniedException.class);
    }
}
