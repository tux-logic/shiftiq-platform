package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StaffCareerSnapshotResource(
        UUID id,
        UUID registrationId,
        UUID employeeId,
        UUID branchId,
        String speciality,
        String specialityName,
        BigDecimal salary,
        String status,
        String changeType,
        String changeReason,
        UUID changedBy,
        Instant recordedAt
) {
}
