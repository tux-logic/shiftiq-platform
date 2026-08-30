package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

public record UpdateWorkshopResource(
        String businessName,
        String brandName,
        String taxId,
        int mileageIntervalConfig
) {
}
