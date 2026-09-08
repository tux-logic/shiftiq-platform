package com.tuxlogic.shiftiq.platform.operations.domain.model.events;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.AppointmentId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

/**
 * Domain event published when a Work Order is completed.
 * Can be listened to by billing/inventory contexts to trigger invoice generation or stock audits.
 */
public record WorkOrderCompletedEvent(
        Object source,
        BranchId branchId,
        WorkOrderId workOrderId,
        AppointmentId appointmentId,
        Money totalAmount
) {}
