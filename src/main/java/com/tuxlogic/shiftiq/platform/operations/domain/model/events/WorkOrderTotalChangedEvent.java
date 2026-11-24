package com.tuxlogic.shiftiq.platform.operations.domain.model.events;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

/**
 * Domain event published whenever a Work Order's total amount is recalculated
 * (e.g., when tasks or products are added, updated, or removed).
 */
public record WorkOrderTotalChangedEvent(
        Object source,
        WorkOrderId workOrderId,
        Money totalAmount
) {
}
