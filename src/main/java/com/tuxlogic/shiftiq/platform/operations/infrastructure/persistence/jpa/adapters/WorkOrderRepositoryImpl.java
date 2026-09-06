package com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.AppointmentId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.OperationsMessageKeys;
import com.tuxlogic.shiftiq.platform.operations.domain.repositories.WorkOrderRepository;
import com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.assemblers.WorkOrderPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;
import com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.repositories.WorkOrderPersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class WorkOrderRepositoryImpl implements WorkOrderRepository {

    private final WorkOrderPersistenceRepository workOrderPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public WorkOrderRepositoryImpl(WorkOrderPersistenceRepository workOrderPersistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.workOrderPersistenceRepository = workOrderPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public WorkOrder save(WorkOrder workOrder) {
        try {
            WorkOrderPersistenceEntity entity = WorkOrderPersistenceAssembler.toPersistenceEntity(workOrder);
            WorkOrderPersistenceEntity savedEntity = workOrderPersistenceRepository.save(entity);
            WorkOrder savedWorkOrder = WorkOrderPersistenceAssembler.toDomainEntity(savedEntity);
            workOrder.domainEvents().forEach(eventPublisher::publishEvent);
            workOrder.clearDomainEvents();

            return savedWorkOrder;
        } catch (Exception e) {
            throw new IllegalStateException(OperationsMessageKeys.REPOSITORY_SAVE_FAILED, e);
        }
    }

    @Override
    public Optional<WorkOrder> findById(WorkOrderId id) {
        return workOrderPersistenceRepository.findById(id.value())
                .map(WorkOrderPersistenceAssembler::toDomainEntity);
    }

    @Override
    public Optional<WorkOrder> findByTaskId(WorkOrderTaskId taskId) {
        return workOrderPersistenceRepository.findByTaskId(taskId.value())
                .map(WorkOrderPersistenceAssembler::toDomainEntity);
    }

    @Override
    public List<WorkOrder> findAllByBranchId(BranchId branchId) {
        return workOrderPersistenceRepository.findAllByBranchId(branchId).stream()
                .map(WorkOrderPersistenceAssembler::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WorkOrder> findByAppointmentId(AppointmentId appointmentId) {
        return workOrderPersistenceRepository.findByAppointmentId(appointmentId)
                .map(WorkOrderPersistenceAssembler::toDomainEntity);
    }

    @Override
    public boolean existsByAppointmentId(AppointmentId appointmentId) {
        return workOrderPersistenceRepository.existsByAppointmentId(appointmentId);
    }

    @Override
    public List<WorkOrder> findAllByCustomerId(CustomerId customerId) {
        return workOrderPersistenceRepository.findAllByCustomerId(customerId).stream()
                .map(WorkOrderPersistenceAssembler::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findAllByVehicleId(VehicleId vehicleId) {
        return workOrderPersistenceRepository.findAllByVehicleId(vehicleId).stream()
                .map(WorkOrderPersistenceAssembler::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WorkOrder> findByInternalNumberAndBranchId(Integer internalNumber, BranchId branchId) {
        return workOrderPersistenceRepository.findByInternalNumberAndBranchId(internalNumber, branchId)
                .map(WorkOrderPersistenceAssembler::toDomainEntity);
    }

    @Override
    public int findMaxInternalNumberByBranchId(BranchId branchId) {
        return workOrderPersistenceRepository.findMaxInternalNumberByBranchId(branchId);
    }
}
