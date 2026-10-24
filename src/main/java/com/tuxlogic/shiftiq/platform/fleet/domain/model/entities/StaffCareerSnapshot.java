package com.tuxlogic.shiftiq.platform.fleet.domain.model.entities;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable domain representation of a staff member's career / assignment snapshot in a branch.
 */
public record StaffCareerSnapshot(
        UUID id,
        UUID registrationId,
        UUID employeeId,
        BranchId branchId,
        String speciality,
        String specialityName,
        BigDecimal salary,
        EmployeeRegistrationStatus status,
        String changeType,
        String changeReason,
        UUID changedBy,
        Instant recordedAt
) {
    public static StaffCareerSnapshot create(
            UUID registrationId,
            UUID employeeId,
            BranchId branchId,
            String speciality,
            String specialityName,
            BigDecimal salary,
            EmployeeRegistrationStatus status,
            String changeType,
            String changeReason,
            UUID changedBy
    ) {
        return new StaffCareerSnapshot(
                UUID.randomUUID(),
                registrationId,
                employeeId,
                branchId,
                speciality,
                specialityName,
                salary,
                status,
                changeType,
                changeReason,
                changedBy,
                Instant.now()
        );
    }
}
