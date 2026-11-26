package com.tuxlogic.shiftiq.platform.core.application.queryservices;

import java.util.UUID;

public record BranchCatalogItem(
        UUID branchId,
        UUID workshopId,
        String workshopBrandName,
        String workshopBusinessName,
        String branchCode,
        String branchName,
        String address,
        String phone,
        String coverImageUrl,
        Double latitude,
        Double longitude,
        Double averageRating,
        Integer reviewCount,
        Integer activeBaysCount,
        Integer freeBaysToday
) {
}
