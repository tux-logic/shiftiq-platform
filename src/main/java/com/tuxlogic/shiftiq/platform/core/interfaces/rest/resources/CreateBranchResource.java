package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateBranchResource(
        @NotNull(message = "workshopId is required") UUID workshopId,
        @NotBlank(message = "code is required") String code,
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "address is required") String address,
        @NotBlank(message = "phone is required") String phone
) {
}
