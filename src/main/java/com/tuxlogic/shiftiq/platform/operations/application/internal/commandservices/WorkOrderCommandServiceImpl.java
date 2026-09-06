package com.tuxlogic.shiftiq.platform.operations.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.operations.application.commandservices.WorkOrderCommandFailure;
import com.tuxlogic.shiftiq.platform.operations.application.commandservices.WorkOrderCommandService;
import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalProductService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.*;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.OperationsMessageKeys;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.domain.repositories.ServiceRepository;
import com.tuxlogic.shiftiq.platform.operations.domain.repositories.WorkOrderRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Internal application service implementing {@link WorkOrderCommandService}.
 * Orchestrates use cases using the domain model and work order repository inside atomic transactions.
 * @author Joel Huamani Estefanero
 */
@Service
public class WorkOrderCommandServiceImpl implements WorkOrderCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkOrderCommandServiceImpl.class);

    private final WorkOrderRepository workOrderRepository;
    private final ServiceRepository serviceRepository;
    private final ExternalProductService externalProductService;

    public WorkOrderCommandServiceImpl(WorkOrderRepository workOrderRepository,
                                       ServiceRepository serviceRepository,
                                       ExternalProductService externalProductService) {
        this.workOrderRepository = workOrderRepository;
        this.serviceRepository = serviceRepository;
        this.externalProductService = externalProductService;
    }

    private Result<WorkOrder, WorkOrderCommandFailure> executeCommand(Supplier<WorkOrder> action) {
        try {
            WorkOrder savedWorkOrder = action.get();
            return Result.success(savedWorkOrder);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Work order command failed - Not Found: {}", e.getMessage());
            return Result.failure(new WorkOrderCommandFailure.NotFound(e.getMessage()));
        } catch (IllegalStateException e) {
            LOGGER.warn("Work order command failed - Invalid State: {}", e.getMessage());
            return Result.failure(new WorkOrderCommandFailure.InvalidState(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Unexpected error processing work order command", e);
            return Result.failure(new WorkOrderCommandFailure.InvalidState(OperationsMessageKeys.UNEXPECTED_ERROR));
        }
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(UpdateWorkOrderDetailsCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            workOrder.updateDetails(command.diagnosticSummary(), command.mileageIn());
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(CreateWorkOrderCommand command) {
        if (workOrderRepository.existsByAppointmentId(command.appointmentId())) {
            LOGGER.warn("Duplicate work order attempt for appointmentId: {}", command.appointmentId().value());
            return Result.failure(new WorkOrderCommandFailure.Duplicate(OperationsMessageKeys.WORK_ORDER_ALREADY_EXISTS_FOR_APPOINTMENT));
        }

        return executeCommand(() -> {
            int nextInternalNumber = workOrderRepository.findMaxInternalNumberByBranchId(command.branchId()) + 1;
            WorkOrder workOrder = new WorkOrder(
                    command.appointmentId(),
                    command.branchId(),
                    command.vehicleId(),
                    command.customerId(),
                    nextInternalNumber,
                    command.diagnosticSummary(),
                    command.mileageIn()
            );
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(AddTaskToWorkOrderCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            var service = serviceRepository.findById(command.serviceId())
                    .orElseThrow(() -> new IllegalArgumentException(OperationsMessageKeys.SERVICE_NOT_FOUND));
            workOrder.addTask(command.serviceId(), command.mechanicId(), command.description(), service.getPrice());
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(AddProductToTaskCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            var sellingPrice = externalProductService.getProductSellingPrice(command.productId().value())
                    .orElseThrow(() -> new IllegalArgumentException(OperationsMessageKeys.PRODUCT_NOT_FOUND));
            workOrder.addProductToTask(command.taskId(), command.productId(), command.quantity(), sellingPrice);
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(RemoveProductFromTaskCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            workOrder.removeProductFromTask(command.taskId(), command.productId());
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(RemoveTaskFromWorkOrderCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            workOrder.removeTask(command.taskId());
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(StartTaskCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            workOrder.startTask(command.taskId());
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(CompleteTaskCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            workOrder.completeTask(command.taskId());
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(ReopenTaskCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            workOrder.reopenTask(command.taskId());
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(MarkWorkOrderAsPaidCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            workOrder.markAsPaid();
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(UpdateWorkOrderTaskDetailsCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            var service = serviceRepository.findById(command.serviceId())
                    .orElseThrow(() -> new IllegalArgumentException(OperationsMessageKeys.SERVICE_NOT_FOUND));
            workOrder.updateTaskDetails(
                    command.taskId(),
                    command.serviceId(),
                    command.mechanicId(),
                    command.description(),
                    service.getPrice()
            );
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(UpdateProductQuantityInTaskCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            workOrder.updateProductQuantityInTask(
                    command.taskId(),
                    command.productId(),
                    command.newQuantity()
            );
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(DeleteWorkOrderCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            workOrder.delete();
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(CompleteWorkOrderCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            workOrder.completeWorkOrder();
            return workOrderRepository.save(workOrder);
        });
    }

    @Override
    @Transactional
    public Result<WorkOrder, WorkOrderCommandFailure> handle(AssignMechanicToTaskCommand command) {
        return executeCommand(() -> {
            WorkOrder workOrder = findWorkOrderOrThrow(command.workOrderId());
            var task = workOrder.getTasks().stream()
                    .filter(t -> t.getId().equals(command.taskId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(OperationsMessageKeys.TASK_NOT_FOUND));
            workOrder.updateTaskDetails(
                    command.taskId(),
                    task.getServiceId(),
                    command.mechanicId(),
                    task.getDescription(),
                    task.getPrice()
            );
            return workOrderRepository.save(workOrder);
        });
    }

    private WorkOrder findWorkOrderOrThrow(WorkOrderId workOrderId) {
        return workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException(OperationsMessageKeys.WORK_ORDER_NOT_FOUND));
    }
}