package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.*;

/**
 * Command representing the intent to update the details of a Task within a Work Order.
 * This command encapsulates all necessary information to perform the update action, including identifiers for the work order and task, as well as the new details to be applied.
 * @param workOrderId
 * @param taskId
 * @param serviceId
 * @param mechanicId
 * @param description
 * @author Joel Huamani Estefanero
 */
public record UpdateWorkOrderTaskDetailsCommand(
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        ServiceId serviceId,
        MechanicId mechanicId,
        TaskDescription description
) {
    public UpdateWorkOrderTaskDetailsCommand {
        if (workOrderId == null) throw new IllegalArgumentException("operations.error.command.workOrderId.required");
        if (taskId == null) throw new IllegalArgumentException("operations.error.command.taskId.required");
        if (serviceId == null) throw new IllegalArgumentException("operations.error.command.serviceId.required");
        if (mechanicId == null) throw new IllegalArgumentException("operations.error.command.mechanicId.required");
        if (description == null) throw new IllegalArgumentException("operations.error.command.description.required");
    }
}
