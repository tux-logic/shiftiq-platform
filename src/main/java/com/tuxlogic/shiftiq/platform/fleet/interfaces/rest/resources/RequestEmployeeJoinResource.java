package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RequestEmployeeJoinResource(
        @NotNull UUID employeeId,
        @NotNull UUID branchId,
        String speciality,
        String specialityName,
        BigDecimal salary
) {}
