package com.tuxlogic.shiftiq.platform.shared.domain.model.events;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.UUID;

/**
 * Shared event representing the reservation of a Product for a Task.
 */
public record ProductReservedEvent(
        Object source,
        BranchId branchId,
        UUID productId,
        Integer quantity
) {}
