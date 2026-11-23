package com.tuxlogic.shiftiq.platform.billing.domain.model.commands;

import java.util.UUID;

/**
 * Command to recalculate a DRAFT quote's subtotal and total from the current Work Order amount.
 */
public record SyncQuoteWithWorkOrderCommand(UUID quoteId) {
}
