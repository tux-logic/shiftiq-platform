package com.andeva.atelier.platform.core.domain.model.commands;

import com.andeva.atelier.platform.core.domain.model.valueobjects.OwnerId;
import com.andeva.atelier.platform.core.domain.model.valueobjects.TaxId;

public record CreateWorkshopCommand(
        OwnerId ownerId,
        String businessName,
        String brandName,
        TaxId taxId,
        int mileageIntervalConfig
) {
    public CreateWorkshopCommand {
        if (businessName == null || businessName.isBlank()) throw new IllegalArgumentException("core.error.businessName.required");
        if (brandName == null || brandName.isBlank()) throw new IllegalArgumentException("core.error.brandName.required");
    }
}
