package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.ProductId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskId;

/**
 * Command to remove a Product from a Task within a Work Order. This command encapsulates the necessary identifiers to perform the operation, including the Work Order ID, Task ID, and Product ID.
 * @param workOrderId
 * @param taskId
 * @param productId
 * @author Joel Huamani Estefanero
 */
public record RemoveProductFromTaskCommand(
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        ProductId productId
) {
    public RemoveProductFromTaskCommand {
        if (workOrderId == null) throw new IllegalArgumentException("operations.error.command.workOrderId.required");
        if (taskId == null) throw new IllegalArgumentException("operations.error.command.taskId.required");
        if (productId == null) throw new IllegalArgumentException("operations.error.command.productId.required");
    }
}