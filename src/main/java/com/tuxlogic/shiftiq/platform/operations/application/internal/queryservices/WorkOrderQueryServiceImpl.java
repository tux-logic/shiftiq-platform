package com.tuxlogic.shiftiq.platform.operations.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalBranchService;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.*;
import com.tuxlogic.shiftiq.platform.operations.domain.repositories.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Internal application service implementing {@link WorkOrderQueryService}.
 * Provides read-only transactional access to work order aggregates.
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class WorkOrderQueryServiceImpl implements WorkOrderQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkOrderQueryServiceImpl.class);
    private final WorkOrderRepository workOrderRepository;
    private final ExternalBranchService externalBranchService;

    public WorkOrderQueryServiceImpl(WorkOrderRepository workOrderRepository, ExternalBranchService externalBranchService) {
        this.workOrderRepository = workOrderRepository;
        this.externalBranchService = externalBranchService;
    }

    @Override
    public Optional<WorkOrder> handle(GetWorkOrderByIdQuery query) {
        if (query.workOrderId() == null) {
            LOGGER.warn("GetWorkOrderByIdQuery received with null workOrderId");
            throw new IllegalArgumentException("operations.error.query.workOrderId.required");
        }
        return workOrderRepository.findById(query.workOrderId());
    }

    @Override
    public Optional<WorkOrder> handle(GetWorkOrderByTaskIdQuery query) {
        if (query.taskId() == null) {
            LOGGER.warn("GetWorkOrderByTaskIdQuery received with null taskId");
            throw new IllegalArgumentException("operations.error.query.taskId.required");
        }
        return workOrderRepository.findByTaskId(query.taskId());
    }

    @Override
    public List<WorkOrder> handle(GetWorkOrdersByBranchIdQuery query) {
        if (query.branchId() == null) {
            LOGGER.warn("GetWorkOrdersByBranchIdQuery received with null branchId");
            throw new IllegalArgumentException("operations.error.query.branchId.required");
        }
        return workOrderRepository.findAllByBranchId(query.branchId());
    }

    @Override
    public List<WorkOrder> handle(GetWorkOrdersByVehicleIdQuery query) {
        if (query.vehicleId() == null) {
            LOGGER.warn("GetWorkOrdersByVehicleIdQuery received with null vehicleId");
            throw new IllegalArgumentException("operations.error.query.vehicleId.required");
        }
        return workOrderRepository.findAllByVehicleId(query.vehicleId());
    }

    @Override
    public String getBranchCode(UUID branchId) {
        return externalBranchService.fetchBranchCode(branchId).orElse("WO");
    }
}