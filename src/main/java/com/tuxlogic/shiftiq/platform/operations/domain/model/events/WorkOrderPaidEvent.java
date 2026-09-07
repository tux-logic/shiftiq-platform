package com.tuxlogic.shiftiq.platform.operations.domain.model.events;

import com.tuxlogic.shiftiq.platform.operations.domain.model.entities.WorkOrderTaskProduct;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import java.util.List;

/**
 * Event representing the successful payment of a Work Order.
 */
public record WorkOrderPaidEvent(
        Object source,
        BranchId branchId,
        List<WorkOrderTaskProduct> dispatchedProducts
) {
    public WorkOrderPaidEvent {
        dispatchedProducts = dispatchedProducts != null ? List.copyOf(dispatchedProducts) : List.of();
    }
}
