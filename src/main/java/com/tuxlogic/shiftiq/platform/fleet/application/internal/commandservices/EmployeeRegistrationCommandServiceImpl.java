package com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandService;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalIamService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.RequestEmployeeJoinCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.ApproveEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.RejectEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class EmployeeRegistrationCommandServiceImpl implements EmployeeRegistrationCommandService {

    private static final String DEFAULT_STAFF_ROLE = "ROLE_EMPLOYEE";

    private final EmployeeRegistrationRepository repository;
    private final ExternalCoreService externalCoreService;
    private final ExternalIamService externalIamService;

    public EmployeeRegistrationCommandServiceImpl(EmployeeRegistrationRepository repository,
                                                   ExternalCoreService externalCoreService,
                                                   ExternalIamService externalIamService) {
        this.repository = repository;
        this.externalCoreService = externalCoreService;
        this.externalIamService = externalIamService;
    }

    @Override
    @Transactional
    public Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(
            CreateEmployeeRegistrationCommand command) {
        try {
            if (!externalCoreService.existsBranchById(command.branchId())) {
                log.warn("Employee registration failed: Branch {} does not exist", command.branchId());
                return Result.failure(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
            }
            if (!externalCoreService.existsEmployeeById(command.employeeId())) {
                log.warn("Employee registration failed: Employee {} does not exist", command.employeeId());
                return Result.failure(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
            }

            if (repository.existsByEmployeeIdAndBranchId(command.employeeId().value(), command.branchId().value())) {
                log.warn("Employee registration conflict: employee {} already registered in branch {}", command.employeeId(), command.branchId());
                return Result.failure(EmployeeRegistrationCommandFailure.REGISTRATION_ALREADY_EXISTS);
            }

            var specialityFailure = validateSpeciality(command.branchId(), command.speciality());
            if (specialityFailure.isPresent()) {
                return Result.failure(specialityFailure.get());
            }

            var registration = new EmployeeRegistration(
                    command.employeeId().value(),
                    command.branchId(),
                    command.speciality(),
                    command.specialityName(),
                    command.salary());
            var saved = repository.save(registration);

            applyStaffAssignment(command.employeeId().value(), command.role());

            log.info("Employee registration created successfully with ID {}", saved.getId());
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            log.error("Failed to create employee registration: {}", ex.getMessage());
            return Result.failure(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
        }
    }

    @Override
    @Transactional
    public Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(UpdateEmployeeRegistrationCommand command) {
        var registrationOptional = repository.findById(command.id());
        if (registrationOptional.isEmpty()) {
            return Result.failure(EmployeeRegistrationCommandFailure.REGISTRATION_NOT_FOUND);
        }

        var registration = registrationOptional.get();
        registration.update(command.speciality(), command.specialityName(), command.salary());

        var savedRegistration = repository.save(registration);
        return Result.success(savedRegistration);
    }

    @Override
    @Transactional
    public Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(DeleteEmployeeRegistrationCommand command) {
        var registrationOptional = repository.findById(command.id());
        if (registrationOptional.isEmpty()) {
            return Result.failure(EmployeeRegistrationCommandFailure.REGISTRATION_NOT_FOUND);
        }

        var registration = registrationOptional.get();
        registration.deactivate();
        var savedRegistration = repository.save(registration);

        syncBranchMembership(registration.getEmployeeId());

        return Result.success(savedRegistration);
    }

    @Override
    @Transactional
    public Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(RequestEmployeeJoinCommand command) {
        try {
            if (!externalCoreService.existsBranchById(command.branchId())) {
                log.warn("Request employee join failed: Branch {} does not exist", command.branchId());
                return Result.failure(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
            }
            if (!externalCoreService.existsEmployeeById(command.employeeId())) {
                log.warn("Request employee join failed: Employee {} does not exist", command.employeeId());
                return Result.failure(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
            }
            if (repository.existsByEmployeeIdAndBranchId(command.employeeId().value(), command.branchId().value())) {
                log.warn("Request employee join conflict: employee {} already registered/pending in branch {}", command.employeeId(), command.branchId());
                return Result.failure(EmployeeRegistrationCommandFailure.REGISTRATION_ALREADY_EXISTS);
            }

            var specialityFailure = validateSpeciality(command.branchId(), command.speciality());
            if (specialityFailure.isPresent()) {
                return Result.failure(specialityFailure.get());
            }

            var registration = new EmployeeRegistration(
                    command.employeeId().value(),
                    command.branchId(),
                    command.speciality(),
                    command.specialityName(),
                    command.salary(),
                    EmployeeRegistrationStatus.PENDING_APPROVAL);
            var saved = repository.save(registration);
            log.info("Employee join request created successfully with ID {}", saved.getId());
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            log.error("Failed to request employee join: {}", ex.getMessage());
            return Result.failure(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
        }
    }

    @Override
    @Transactional
    public Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(ApproveEmployeeRegistrationCommand command) {
        var registrationOptional = repository.findById(command.registrationId());
        if (registrationOptional.isEmpty()) {
            return Result.failure(EmployeeRegistrationCommandFailure.REGISTRATION_NOT_FOUND);
        }

        var registration = registrationOptional.get();
        try {
            registration.approve();
        } catch (IllegalStateException ex) {
            log.warn("Failed to approve employee registration {}: {}", command.registrationId(), ex.getMessage());
            return Result.failure(EmployeeRegistrationCommandFailure.INVALID_STATUS_TRANSITION);
        }

        var savedRegistration = repository.save(registration);

        applyStaffAssignment(registration.getEmployeeId(), DEFAULT_STAFF_ROLE);

        log.info("Employee registration {} approved successfully", command.registrationId());
        return Result.success(savedRegistration);
    }

    @Override
    @Transactional
    public Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(RejectEmployeeRegistrationCommand command) {
        var registrationOptional = repository.findById(command.registrationId());
        if (registrationOptional.isEmpty()) {
            return Result.failure(EmployeeRegistrationCommandFailure.REGISTRATION_NOT_FOUND);
        }

        var registration = registrationOptional.get();
        try {
            registration.reject(command.reason());
        } catch (IllegalStateException ex) {
            log.warn("Failed to reject employee registration {}: {}", command.registrationId(), ex.getMessage());
            return Result.failure(EmployeeRegistrationCommandFailure.INVALID_STATUS_TRANSITION);
        }

        var savedRegistration = repository.save(registration);
        log.info("Employee registration {} rejected successfully", command.registrationId());
        return Result.success(savedRegistration);
    }

    /**
     * Ensures the speciality used by an onboarding request belongs to the active
     * catalog of the workshop that owns the branch.
     *
     * @return the failure to report, empty when the speciality is valid
     */
    private Optional<EmployeeRegistrationCommandFailure> validateSpeciality(BranchId branchId, String speciality) {
        var workshopId = externalCoreService.findWorkshopIdForBranch(branchId);
        if (workshopId.isEmpty()) {
            log.warn("Speciality validation failed: no workshop found for branch {}", branchId);
            return Optional.of(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
        }
        if (!externalCoreService.existsActiveWorkshopSpecialty(workshopId.get(), speciality)) {
            log.warn("Speciality '{}' is not an active specialty of workshop {} (branch {})",
                    speciality, workshopId.get(), branchId);
            return Optional.of(EmployeeRegistrationCommandFailure.SPECIALTY_NOT_IN_CATALOG);
        }
        return Optional.empty();
    }

    /**
     * Grants the target user the branch memberships of its active registrations and the
     * requested staff role, so the hierarchy checks of the security layer can resolve.
     */
    private void applyStaffAssignment(UUID employeeId, String role) {
        var userId = externalCoreService.findUserIdByEmployeeId(new EmployeeId(employeeId));
        if (userId.isEmpty()) {
            log.warn("Staff assignment skipped: employee {} has no user account", employeeId);
            return;
        }
        var activeBranchIds = repository.findActiveBranchIdsByEmployeeId(employeeId);
        externalIamService.setBranches(userId.get(), activeBranchIds);
        boolean roleApplied = externalIamService.assignRole(userId.get(), role);
        log.info("Staff assignment for user {}: branches={}, role '{}' applied={}",
                userId.get(), activeBranchIds, role, roleApplied);
    }

    /**
     * Recomputes the branch memberships of the target user after a registration is
     * deactivated, revoking branch access when no active registration remains.
     */
    private void syncBranchMembership(UUID employeeId) {
        var userId = externalCoreService.findUserIdByEmployeeId(new EmployeeId(employeeId));
        if (userId.isEmpty()) {
            log.warn("Branch membership sync skipped: employee {} has no user account", employeeId);
            return;
        }
        var activeBranchIds = repository.findActiveBranchIdsByEmployeeId(employeeId);
        externalIamService.setBranches(userId.get(), activeBranchIds);
        log.info("Branch membership of user {} synced to {}", userId.get(), activeBranchIds);
    }
}
