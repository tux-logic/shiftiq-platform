package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskId;

/**
 * Command representing the completion of a task within a work order. This command is used to indicate that a specific task has been completed, allowing the system to update the status of the task and potentially trigger subsequent actions, such as notifying relevant parties or updating the overall work order status.
 * @param workOrderId
 * @param taskId
 * @author Joel Huamani Estefanero
 */
public record CompleteTaskCommand(WorkOrderId workOrderId, WorkOrderTaskId taskId) {}
