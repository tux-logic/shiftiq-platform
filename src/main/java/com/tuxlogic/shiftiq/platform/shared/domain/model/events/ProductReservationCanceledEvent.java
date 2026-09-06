package com.tuxlogic.shiftiq.platform.shared.domain.model.events;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.UUID;

/**
 * Shared event representing the cancellation of a Product reservation.
 */
public record ProductReservationCanceledEvent(
        Object source,
        BranchId branchId,
        UUID productId,
        Integer quantity
) {}
