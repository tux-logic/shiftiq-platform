package com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects;

/**
 * Constants class holding internationalization message keys for the Operations Bounded Context.
 */
public final class OperationsMessageKeys {

    private OperationsMessageKeys() {}

    // Work Order Errors
    public static final String WORK_ORDER_NOT_FOUND = "operations.error.workOrder.notFound";
    public static final String WORK_ORDER_NOT_FOUND_FOR_TASK = "operations.error.workOrder.notFoundForTask";
    public static final String WORK_ORDER_CANNOT_MODIFY_CLOSED = "operations.error.workOrder.cannotModifyClosedOrder";
    public static final String WORK_ORDER_CANNOT_DELETE_COMPLETED_TASK = "operations.error.workOrder.cannotDeleteCompletedTask";
    public static final String WORK_ORDER_CANNOT_DELETE_PAID = "operations.error.workOrder.cannotDeletePaidOrder";
    public static final String WORK_ORDER_CANNOT_REOPEN_PAID = "operations.error.workOrder.cannotReopenTaskOfPaidOrder";
    public static final String WORK_ORDER_ALREADY_EXISTS_FOR_APPOINTMENT = "operations.error.workOrder.alreadyExistsForAppointment";
    public static final String WORK_ORDER_PENDING_TASKS_EXIST = "operations.error.workOrder.pendingTasksExist";
    public static final String WORK_ORDER_INVALID_TRANSITION = "operations.error.workOrderStatus.invalidTransition";

    // Task Errors
    public static final String TASK_NOT_FOUND = "operations.error.task.notFound";
    public static final String TASK_CANNOT_MODIFY_COMPLETED = "operations.error.task.cannotModifyCompletedTask";
    public static final String TASK_INVALID_TRANSITION = "operations.error.workOrderTaskStatus.invalidTransition";

    // Product & Task Product Errors
    public static final String PRODUCT_NOT_FOUND = "operations.error.product.notFound";
    public static final String TASK_PRODUCT_NOT_FOUND = "operations.error.taskProduct.notFound";

    // Service Errors
    public static final String SERVICE_NOT_FOUND = "operations.error.service.notFound";
    public static final String SERVICE_DELETE_FAILED = "operations.error.service.deleteFailed";

    // Repository & Unexpected Errors
    public static final String REPOSITORY_SAVE_FAILED = "operations.error.repository.saveFailed";
    public static final String REPOSITORY_DELETE_FAILED = "operations.error.repository.deleteFailed";
    public static final String UNEXPECTED_ERROR = "operations.error.unexpected";

    // Query Errors
    public static final String QUERY_WORK_ORDER_ID_REQUIRED = "operations.error.query.workOrderId.required";
    public static final String QUERY_TASK_ID_REQUIRED = "operations.error.query.taskId.required";
    public static final String QUERY_BRANCH_ID_REQUIRED = "operations.error.query.branchId.required";
    public static final String QUERY_VEHICLE_ID_REQUIRED = "operations.error.query.vehicleId.required";
    public static final String QUERY_SERVICE_ID_REQUIRED = "operations.error.query.serviceId.required";
}
