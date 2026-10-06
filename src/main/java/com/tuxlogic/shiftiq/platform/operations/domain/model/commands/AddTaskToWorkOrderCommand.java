package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.*;

/**
 * Command object representing the action of adding a Task to a Work Order in the operations domain.
 * This command encapsulates all necessary information to perform the action, including identifiers for the work order
 * @param workOrderId
 * @param serviceId
 * @param mechanicId
 * @param description
 * @author Joel Huamani Estefanero
 */
import java.util.List;

public record AddTaskToWorkOrderCommand(
        WorkOrderId workOrderId,
        ServiceId serviceId,
        MechanicId mechanicId,
        TaskDescription description,
        List<String> evidenceImages
) {
    public AddTaskToWorkOrderCommand(WorkOrderId workOrderId, ServiceId serviceId, MechanicId mechanicId, TaskDescription description) {
        this(workOrderId, serviceId, mechanicId, description, java.util.Collections.emptyList());
    }

    public AddTaskToWorkOrderCommand {
        if (workOrderId == null) throw new IllegalArgumentException("operations.error.command.workOrderId.required");
        if (serviceId == null) throw new IllegalArgumentException("operations.error.command.serviceId.required");
        if (mechanicId == null) throw new IllegalArgumentException("operations.error.command.mechanicId.required");
        if (description == null) throw new IllegalArgumentException("operations.error.command.description.required");
    }
}
