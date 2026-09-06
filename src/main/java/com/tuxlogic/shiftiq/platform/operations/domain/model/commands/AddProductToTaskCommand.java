package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.*;

/**
 * Command representing the intent to add a Product to a Task within a Work Order.
 * Resides inside the domain commands package.
 * @param workOrderId
 * @param taskId
 * @param productId
 * @param quantity
 * @author Joel Huamani Estefanero
 */
public record AddProductToTaskCommand(
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        ProductId productId,
        Quantity quantity
) {
    public AddProductToTaskCommand {
        if (workOrderId == null) throw new IllegalArgumentException("operations.error.command.workOrderId.required");
        if (taskId == null) throw new IllegalArgumentException("operations.error.command.taskId.required");
        if (productId == null) throw new IllegalArgumentException("operations.error.command.productId.required");
        if (quantity == null) throw new IllegalArgumentException("operations.error.command.quantity.required");
    }
}
