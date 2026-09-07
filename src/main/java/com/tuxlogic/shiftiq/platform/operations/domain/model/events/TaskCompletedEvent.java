package com.tuxlogic.shiftiq.platform.operations.domain.model.events;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

/**
 * Domain event published when a mechanic task is completed.
 */
public record TaskCompletedEvent(
        Object source,
        BranchId branchId,
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId
) {}
