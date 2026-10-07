package com.tuxlogic.shiftiq.platform.operations.domain.model.events;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.AppointmentId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

/**
 * Domain event published when a completed Work Order is reopened, reverting the
 * completion. Listeners must undo the effect of {@link WorkOrderCompletedEvent}
 * (analytics revenue and completed work order counters).
 */
public record WorkOrderReopenedEvent(
        Object source,
        BranchId branchId,
        WorkOrderId workOrderId,
        AppointmentId appointmentId,
        Money totalAmount
) {}
