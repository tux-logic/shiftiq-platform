package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;

/**
 * Command representing the intent to mark a Work Order as paid
 * Resides inside the domain commands package
 * @param workOrderId The unique identifier of the Work Order
 * @author Joel Huamani Estefanero
 */
public record MarkWorkOrderAsPaidCommand(WorkOrderId workOrderId) {
    public MarkWorkOrderAsPaidCommand {
        if (workOrderId == null) throw new IllegalArgumentException("operations.error.command.workOrderId.required");
    }
}
