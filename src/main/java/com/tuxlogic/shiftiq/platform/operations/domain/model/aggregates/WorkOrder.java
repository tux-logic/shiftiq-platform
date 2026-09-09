package com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.operations.domain.model.entities.WorkOrderTask;
import com.tuxlogic.shiftiq.platform.operations.domain.model.entities.WorkOrderTaskProduct;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.TaskCompletedEvent;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.TaskReopenedEvent;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.TaskStartedEvent;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderCompletedEvent;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderPaidEvent;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.*;
import com.tuxlogic.shiftiq.platform.shared.domain.model.events.ProductReservationCanceledEvent;
import com.tuxlogic.shiftiq.platform.shared.domain.model.events.ProductReservedEvent;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.*;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import lombok.Getter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root representing a Work Order in the automotive service center context.
 * @author Joel Huamani Estefanero
 */
@Getter
public class WorkOrder extends AbstractDomainAggregateRoot<WorkOrder> {

    private WorkOrderId id;
    private AppointmentId appointmentId;
    private BranchId branchId;
    private VehicleId vehicleId;
    private CustomerId customerId;
    private Integer internalNumber;
    private WorkOrderStatus status;
    private DiagnosticSummary diagnosticSummary;
    private Mileage mileageIn;
    private Money totalAmount;
    private List<WorkOrderTask> tasks;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private Long version;

    public WorkOrder() {}

    public WorkOrder(WorkOrderId id, AppointmentId appointmentId, BranchId branchId, VehicleId vehicleId, CustomerId customerId, Integer internalNumber, WorkOrderStatus status, DiagnosticSummary diagnosticSummary, Mileage mileageIn, Money totalAmount, List<WorkOrderTask> tasks, Instant createdAt, Instant updatedAt, Instant deletedAt, UUID createdBy, UUID updatedBy, Long version) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.branchId = branchId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.internalNumber = internalNumber;
        this.status = status;
        this.diagnosticSummary = diagnosticSummary;
        this.mileageIn = mileageIn;
        this.totalAmount = totalAmount;
        this.tasks = tasks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.version = version;
    }

    public WorkOrder(AppointmentId appointmentId, BranchId branchId, VehicleId vehicleId, CustomerId customerId, Integer internalNumber, DiagnosticSummary diagnosticSummary, Mileage mileageIn) {
        this.id = new WorkOrderId(UUID.randomUUID());
        this.appointmentId = appointmentId;
        this.branchId = branchId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.internalNumber = internalNumber;
        this.diagnosticSummary = diagnosticSummary;
        this.mileageIn = mileageIn;
        this.status = WorkOrderStatus.PENDING;
        this.totalAmount = Money.ZERO;
        this.tasks = new ArrayList<>();
    }

    private void verifyOrderNotClosed() {
        if (this.status == WorkOrderStatus.COMPLETED || this.status == WorkOrderStatus.PAID) {
            throw new IllegalStateException(OperationsMessageKeys.WORK_ORDER_CANNOT_MODIFY_CLOSED);
        }
    }

    public void addTask(ServiceId serviceId, MechanicId mechanicId, TaskDescription description, Money laborPrice) {
        verifyOrderNotClosed();
        WorkOrderTask task = new WorkOrderTask(serviceId, this.branchId, mechanicId, description, laborPrice);
        this.tasks.add(task);
        recalculateTotalAmount();
    }

    public void addProductToTask(WorkOrderTaskId taskId, ProductId productId, Quantity quantity, Money unitPrice) {
        verifyOrderNotClosed();
        WorkOrderTask task = findTaskOrThrow(taskId);
        task.addProduct(productId, quantity, unitPrice);
        recalculateTotalAmount();
        this.registerEvent(new ProductReservedEvent(this, this.branchId, productId.value(), quantity.value()));
    }

    public void removeProductFromTask(WorkOrderTaskId taskId, ProductId productId) {
        verifyOrderNotClosed();
        WorkOrderTask task = findTaskOrThrow(taskId);

        WorkOrderTaskProduct product = task.getProducts().stream()
                .filter(p -> p.getProductId().equals(productId) && !p.isDeleted())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(OperationsMessageKeys.TASK_PRODUCT_NOT_FOUND));

        Quantity returnedQuantity = product.getQuantity();
        task.removeProduct(productId);
        recalculateTotalAmount();

        this.registerEvent(new ProductReservationCanceledEvent(this, this.branchId, productId.value(), returnedQuantity.value()));
    }

    public void removeTask(WorkOrderTaskId taskId) {
        verifyOrderNotClosed();
        WorkOrderTask task = findTaskOrThrow(taskId);
        if (task.getStatus() == WorkOrderTaskStatus.COMPLETED) {
            throw new IllegalStateException(OperationsMessageKeys.WORK_ORDER_CANNOT_DELETE_COMPLETED_TASK);
        }

        for (WorkOrderTaskProduct product : task.getProducts()) {
            if (!product.isDeleted()) {
                this.registerEvent(new ProductReservationCanceledEvent(this, this.branchId, product.getProductId().value(), product.getQuantity().value()));
            }
        }
        this.tasks.remove(task);
        recalculateTotalAmount();
    }

    public void delete() {
        if (this.status == WorkOrderStatus.PAID) {
            throw new IllegalStateException(OperationsMessageKeys.WORK_ORDER_CANNOT_DELETE_PAID);
        }
        this.deletedAt = Instant.now();
        for (WorkOrderTask task : this.tasks) {
            if (!task.isDeleted()) {
                for (WorkOrderTaskProduct product : task.getProducts()) {
                    if (!product.isDeleted()) {
                        this.registerEvent(new ProductReservationCanceledEvent(this, this.branchId, product.getProductId().value(), product.getQuantity().value()));
                    }
                }
            }
        }
    }

    public void startTask(WorkOrderTaskId taskId) {
        verifyOrderNotClosed();
        WorkOrderTask task = findTaskOrThrow(taskId);
        task.start();
        checkAutoCompletion();
        this.registerEvent(new TaskStartedEvent(this, this.branchId, this.id, taskId));
    }

    public void completeTask(WorkOrderTaskId taskId) {
        verifyOrderNotClosed();
        WorkOrderTask task = findTaskOrThrow(taskId);
        if (task.complete()) {
            boolean allTasksCompleted = this.tasks.stream()
                    .filter(t -> !t.isDeleted())
                    .allMatch(t -> t.getStatus() == WorkOrderTaskStatus.COMPLETED);

            if (allTasksCompleted) {
                this.status = this.status.transitionTo(WorkOrderStatus.COMPLETED);
            } else {
                checkAutoCompletion();
            }
            this.registerEvent(new TaskCompletedEvent(this, this.branchId, this.id, taskId));
        }
    }

    public void reopenTask(WorkOrderTaskId taskId) {
        if (this.status == WorkOrderStatus.PAID) {
            throw new IllegalStateException(OperationsMessageKeys.WORK_ORDER_CANNOT_REOPEN_PAID);
        }
        WorkOrderTask task = findTaskOrThrow(taskId);
        if (task.reopen()) {
            if (this.status == WorkOrderStatus.COMPLETED) {
                this.status = WorkOrderStatus.IN_PROGRESS;
            }
            this.registerEvent(new TaskReopenedEvent(this, this.branchId, this.id, taskId));
        }
    }

    public void startWork() {
        this.status = this.status.transitionTo(WorkOrderStatus.IN_PROGRESS);
    }

    public void completeWorkOrder() {
        boolean allTasksCompleted = this.tasks.stream()
                .filter(t -> !t.isDeleted())
                .allMatch(t -> t.getStatus() == WorkOrderTaskStatus.COMPLETED);
        if (!allTasksCompleted) {
            throw new IllegalStateException(OperationsMessageKeys.WORK_ORDER_PENDING_TASKS_EXIST);
        }
        this.status = this.status.transitionTo(WorkOrderStatus.COMPLETED);
        this.registerEvent(new WorkOrderCompletedEvent(this, this.branchId, this.id, this.appointmentId, this.totalAmount));
    }

    public void assignMechanicToTask(WorkOrderTaskId taskId, MechanicId mechanicId) {
        verifyOrderNotClosed();
        WorkOrderTask task = findTaskOrThrow(taskId);
        task.assignMechanic(mechanicId);
    }

    public void markAsPaid() {
        this.status = this.status.transitionTo(WorkOrderStatus.PAID);

        List<WorkOrderTaskProduct> dispatchedProducts = new ArrayList<>();
        for (WorkOrderTask task : this.tasks) {
            if (!task.isDeleted()) {
                for (WorkOrderTaskProduct product : task.getProducts()) {
                    if (!product.isDeleted()) {
                        dispatchedProducts.add(product);
                    }
                }
            }
        }
        this.registerEvent(new WorkOrderPaidEvent(this, this.branchId, dispatchedProducts));
    }

    private void recalculateTotalAmount() {
        this.totalAmount = this.tasks.stream()
                .filter(t -> !t.isDeleted())
                .map(WorkOrderTask::getPrice)
                .reduce(Money.ZERO, Money::plus);
    }


    private WorkOrderTask findTaskOrThrow(WorkOrderTaskId taskId) {
        return this.tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(OperationsMessageKeys.TASK_NOT_FOUND));
    }

    public void updateDetails(DiagnosticSummary diagnosticSummary, Mileage mileageIn) {
        verifyOrderNotClosed();
        this.diagnosticSummary = diagnosticSummary;
        this.mileageIn = mileageIn;
    }

    public void updateTaskDetails(WorkOrderTaskId taskId, ServiceId serviceId, MechanicId mechanicId, TaskDescription description, Money newLaborPrice) {
        verifyOrderNotClosed();
        WorkOrderTask task = findTaskOrThrow(taskId);
        task.updateDetails(serviceId, mechanicId, description, newLaborPrice);
        recalculateTotalAmount();
    }

    public void updateProductQuantityInTask(WorkOrderTaskId taskId, ProductId productId, Quantity newQuantity) {
        verifyOrderNotClosed();
        WorkOrderTask task = findTaskOrThrow(taskId);

        Quantity oldQuantity = task.updateProductQuantity(productId, newQuantity);
        recalculateTotalAmount();

        int delta = newQuantity.value() - oldQuantity.value();
        if (delta > 0) {
            this.registerEvent(new ProductReservedEvent(this, this.branchId, productId.value(), delta));
        } else if (delta < 0) {
            this.registerEvent(new ProductReservationCanceledEvent(this, this.branchId, productId.value(), Math.abs(delta)));
        }
    }

    private void checkAutoCompletion() {
        if (this.status == WorkOrderStatus.PENDING) {
            this.status = WorkOrderStatus.IN_PROGRESS;
        }
    }

    public List<WorkOrderTask> getTasks() {
        return this.tasks != null ? Collections.unmodifiableList(this.tasks) : Collections.emptyList();
    }
}
