package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.MechanicId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskId;

/**
 * Command to assign a mechanic to a work order task.
 */
public record AssignMechanicToTaskCommand(WorkOrderId workOrderId, WorkOrderTaskId taskId, MechanicId mechanicId) {
    public AssignMechanicToTaskCommand {
        if (workOrderId == null) throw new IllegalArgumentException("operations.error.command.workOrderId.required");
        if (taskId == null) throw new IllegalArgumentException("operations.error.command.taskId.required");
        if (mechanicId == null) throw new IllegalArgumentException("operations.error.command.mechanicId.required");
    }
}
