package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;

/**
 * Command representing the completion of a work order.
 * @param workOrderId the identifier of the work order to complete
 */
public record CompleteWorkOrderCommand(WorkOrderId workOrderId) {
    public CompleteWorkOrderCommand {
        if (workOrderId == null) throw new IllegalArgumentException("operations.error.command.workOrderId.required");
    }
}
