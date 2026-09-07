package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.*;

/**
 * Command representing the intent to update the quantity of a Product in a Task within a Work Order.
 * @param workOrderId
 * @param taskId
 * @param productId
 * @param newQuantity
 * @author Joel Huamani Estefanero
 */
public record UpdateProductQuantityInTaskCommand(
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        ProductId productId,
        Quantity newQuantity
) {
    public UpdateProductQuantityInTaskCommand {
        if (workOrderId == null) throw new IllegalArgumentException("operations.error.command.workOrderId.required");
        if (taskId == null) throw new IllegalArgumentException("operations.error.command.taskId.required");
        if (productId == null) throw new IllegalArgumentException("operations.error.command.productId.required");
        if (newQuantity == null) throw new IllegalArgumentException("operations.error.command.quantity.required");
    }
}
