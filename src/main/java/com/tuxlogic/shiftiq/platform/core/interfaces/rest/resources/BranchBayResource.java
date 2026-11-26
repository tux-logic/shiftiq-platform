package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import java.util.UUID;

public record BranchBayResource(
        UUID id,
        UUID branchId,
        String code,
        String name,
        String type,
        boolean active
) {
}