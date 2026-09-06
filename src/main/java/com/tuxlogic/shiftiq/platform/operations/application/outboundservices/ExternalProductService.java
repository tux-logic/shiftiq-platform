package com.tuxlogic.shiftiq.platform.operations.application.outboundservices;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound service interface for fetching product pricing from inventory context.
 */
public interface ExternalProductService {
    Optional<Money> getProductSellingPrice(UUID productId);
}
