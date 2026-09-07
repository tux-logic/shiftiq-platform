package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.MileageIntervalConfig;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.TaxId;

public record CreateWorkshopCommand(
        OwnerId ownerId,
        String businessName,
        String brandName,
        TaxId taxId,
        MileageIntervalConfig mileageIntervalConfig
) {
    public CreateWorkshopCommand {
        if (businessName == null || businessName.isBlank()) throw new IllegalArgumentException("core.error.businessName.required");
        if (brandName == null || brandName.isBlank()) throw new IllegalArgumentException("core.error.brandName.required");
        if (mileageIntervalConfig == null) throw new IllegalArgumentException("core.error.mileageIntervalConfig.required");
    }
}
