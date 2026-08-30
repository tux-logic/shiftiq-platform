package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.valueobjects.*;

/**
 * Command object representing the action of adding a Task to a Work Order in the operations domain.
 * This command encapsulates all necessary information to perform the action, including identifiers for the work order
 * @param workOrderId
 * @param serviceId
 * @param mechanicId
 * @param description
 * @author Joel Huamani Estefanero
 */
public record AddTaskToWorkOrderCommand(
        WorkOrderId workOrderId,
        ServiceId serviceId,
        MechanicId mechanicId,
        TaskDescription description
) {}
