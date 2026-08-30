package com.andeva.atelier.platform.core.domain.model.commands;

import com.andeva.atelier.platform.core.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.core.domain.model.valueobjects.WorkshopId;

public record UpdateWorkshopCommand(
        WorkshopId id,
        String businessName,
        String brandName,
        TaxId taxId,
        int mileageIntervalConfig
) {
    public UpdateWorkshopCommand {
        if (businessName == null || businessName.isBlank()) throw new IllegalArgumentException("core.error.businessName.required");
        if (brandName == null || brandName.isBlank()) throw new IllegalArgumentException("core.error.brandName.required");
    }
}
