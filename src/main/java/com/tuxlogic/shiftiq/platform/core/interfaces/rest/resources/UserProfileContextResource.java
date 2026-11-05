package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import java.util.List;
import java.util.UUID;

public record UserProfileContextResource(
        UUID userId,
        List<String> roles,
        OwnerSummaryResource owner,
        EmployeeSummaryResource employee,
        CustomerSummaryResource customer
) {
    public record OwnerSummaryResource(
            UUID id,
            String firstName,
            String lastName,
            String documentType,
            String documentNumber,
            String phone
    ) {}

    public record EmployeeSummaryResource(
            UUID id,
            String firstName,
            String lastName,
            String documentType,
            String documentNumber,
            String phone,
            String profileImageUrl
    ) {}

    public record CustomerSummaryResource(
            UUID id,
            boolean isCorporate,
            String firstName,
            String lastName,
            String businessName,
            String documentType,
            String documentNumber,
            String phone,
            String profileImageUrl
    ) {}
}

