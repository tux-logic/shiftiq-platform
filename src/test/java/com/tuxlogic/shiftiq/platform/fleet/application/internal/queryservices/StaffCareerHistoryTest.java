package com.tuxlogic.shiftiq.platform.fleet.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.entities.StaffCareerSnapshot;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetStaffCareerHistoryByEmployeeAndBranchQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.StaffCareerHistoryRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffCareerHistoryTest {

    @Mock
    private StaffCareerHistoryRepository repository;

    private StaffCareerHistoryQueryServiceImpl queryService;

    private final UUID branchUuid = UUID.randomUUID();
    private final BranchId branchId = new BranchId(branchUuid);
    private final UUID employeeId = UUID.randomUUID();
    private final UUID registrationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        queryService = new StaffCareerHistoryQueryServiceImpl(repository);
    }

    @Test
    @DisplayName("retrieving staff career history delegates to repository and returns list ordered")
    void handleQueryReturnsSnapshots() {
        var snapshot1 = new StaffCareerSnapshot(
                UUID.randomUUID(), registrationId, employeeId, branchId,
                "DIAGNOSTIC", "Diagnóstico Computarizado", new BigDecimal("2500"),
                EmployeeRegistrationStatus.ACTIVE, "UPDATED", "Promoted to diagnostic specialist",
                UUID.randomUUID(), Instant.now()
        );
        var snapshot2 = new StaffCareerSnapshot(
                UUID.randomUUID(), registrationId, employeeId, branchId,
                "GENERAL_MECHANIC", "Mecánica General", new BigDecimal("1800"),
                EmployeeRegistrationStatus.ACTIVE, "CREATED", "Initial onboarding",
                UUID.randomUUID(), Instant.now().minusSeconds(86400)
        );

        when(repository.findByBranchIdAndEmployeeId(branchId, employeeId))
                .thenReturn(List.of(snapshot1, snapshot2));

        var query = new GetStaffCareerHistoryByEmployeeAndBranchQuery(branchId, employeeId);
        var result = queryService.handle(query);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).speciality()).isEqualTo("DIAGNOSTIC");
        assertThat(result.get(1).speciality()).isEqualTo("GENERAL_MECHANIC");
        verify(repository).findByBranchIdAndEmployeeId(branchId, employeeId);
    }

    @Test
    @DisplayName("invalid query throws IllegalArgumentException")
    void invalidQueryThrowsException() {
        assertThatThrownBy(() -> new GetStaffCareerHistoryByEmployeeAndBranchQuery(null, employeeId))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GetStaffCareerHistoryByEmployeeAndBranchQuery(branchId, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
