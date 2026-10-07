package com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalIamService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.ApproveEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeRegistrationCommandServiceImplTest {

    @Mock
    private EmployeeRegistrationRepository repository;

    @Mock
    private ExternalCoreService externalCoreService;

    @Mock
    private ExternalIamService externalIamService;

    private EmployeeRegistrationCommandServiceImpl service;

    private final UUID employeeId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID workshopId = UUID.randomUUID();
    private final BranchId branchId = new BranchId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        service = new EmployeeRegistrationCommandServiceImpl(repository, externalCoreService, externalIamService);
    }

    private CreateEmployeeRegistrationCommand createCommand(String role) {
        return new CreateEmployeeRegistrationCommand(
                new EmployeeId(employeeId), branchId, "GENERAL_MECHANIC",
                "Mecánica General", new BigDecimal("1500"), role);
    }

    private void stubOnboardingHappyPath() {
        when(externalCoreService.existsBranchById(branchId)).thenReturn(true);
        when(externalCoreService.existsEmployeeById(new EmployeeId(employeeId))).thenReturn(true);
        when(repository.existsByEmployeeIdAndBranchId(employeeId, branchId.value())).thenReturn(false);
        when(externalCoreService.findWorkshopIdForBranch(branchId)).thenReturn(Optional.of(workshopId));
        when(externalCoreService.existsActiveWorkshopSpecialty(workshopId, "GENERAL_MECHANIC")).thenReturn(true);
        when(repository.save(any(EmployeeRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(externalCoreService.findUserIdByEmployeeId(new EmployeeId(employeeId))).thenReturn(Optional.of(userId));
        when(repository.findActiveBranchIdsByEmployeeId(employeeId)).thenReturn(Set.of(branchId.value()));
    }

    @Test
    @DisplayName("create applies the requested staff role and branch membership (H1)")
    void createAppliesRoleAndBranches() {
        stubOnboardingHappyPath();

        var result = service.handle(createCommand("ROLE_BRANCH_MANAGER"));

        assertThat(result.isSuccess()).isTrue();
        verify(externalIamService).setBranches(userId, Set.of(branchId.value()));
        verify(externalIamService).assignRole(userId, "ROLE_BRANCH_MANAGER");
    }

    @Test
    @DisplayName("create fails when the speciality is not part of the workshop catalog (H6b)")
    void createFailsWhenSpecialityNotInCatalog() {
        when(externalCoreService.existsBranchById(branchId)).thenReturn(true);
        when(externalCoreService.existsEmployeeById(new EmployeeId(employeeId))).thenReturn(true);
        when(repository.existsByEmployeeIdAndBranchId(employeeId, branchId.value())).thenReturn(false);
        when(externalCoreService.findWorkshopIdForBranch(branchId)).thenReturn(Optional.of(workshopId));
        when(externalCoreService.existsActiveWorkshopSpecialty(workshopId, "GENERAL_MECHANIC")).thenReturn(false);

        var result = service.handle(createCommand(null));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure()).contains(EmployeeRegistrationCommandFailure.SPECIALTY_NOT_IN_CATALOG);
        verify(repository, never()).save(any(EmployeeRegistration.class));
        verifyNoInteractions(externalIamService);
    }

    @Test
    @DisplayName("create fails when the branch does not belong to any workshop")
    void createFailsWhenBranchHasNoWorkshop() {
        when(externalCoreService.existsBranchById(branchId)).thenReturn(true);
        when(externalCoreService.existsEmployeeById(new EmployeeId(employeeId))).thenReturn(true);
        when(repository.existsByEmployeeIdAndBranchId(employeeId, branchId.value())).thenReturn(false);
        when(externalCoreService.findWorkshopIdForBranch(branchId)).thenReturn(Optional.empty());

        var result = service.handle(createCommand(null));

        assertThat(result.failure()).contains(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
        verify(repository, never()).save(any(EmployeeRegistration.class));
        verifyNoInteractions(externalIamService);
    }

    @Test
    @DisplayName("approve grants the default employee role once the registration becomes active")
    void approveGrantsDefaultEmployeeRole() {
        var pending = new EmployeeRegistration(employeeId, branchId, "GENERAL_MECHANIC",
                "Mecánica General", new BigDecimal("1500"), EmployeeRegistrationStatus.PENDING_APPROVAL);
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(repository.save(any(EmployeeRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(externalCoreService.findUserIdByEmployeeId(new EmployeeId(employeeId))).thenReturn(Optional.of(userId));
        when(repository.findActiveBranchIdsByEmployeeId(employeeId)).thenReturn(Set.of(branchId.value()));

        var result = service.handle(new ApproveEmployeeRegistrationCommand(pending.getId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(pending.getStatus()).isEqualTo(EmployeeRegistrationStatus.ACTIVE);
        verify(externalIamService).setBranches(userId, Set.of(branchId.value()));
        verify(externalIamService).assignRole(userId, "ROLE_EMPLOYEE");
    }

    @Test
    @DisplayName("delete revokes the branch membership of the account (H1 reverse)")
    void deleteRecomputesBranchMembership() {
        var active = new EmployeeRegistration(employeeId, branchId, "GENERAL_MECHANIC",
                "Mecánica General", new BigDecimal("1500"), EmployeeRegistrationStatus.ACTIVE);
        when(repository.findById(active.getId())).thenReturn(Optional.of(active));
        when(repository.save(any(EmployeeRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(externalCoreService.findUserIdByEmployeeId(new EmployeeId(employeeId))).thenReturn(Optional.of(userId));
        when(repository.findActiveBranchIdsByEmployeeId(employeeId)).thenReturn(Set.of());

        var result = service.handle(new DeleteEmployeeRegistrationCommand(active.getId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(active.getStatus()).isEqualTo(EmployeeRegistrationStatus.INACTIVE);
        verify(externalIamService).setBranches(userId, Set.of());
        verify(externalIamService, never()).assignRole(eq(userId), any());
    }
}
