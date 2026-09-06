package com.tuxlogic.shiftiq.platform.operations.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.operations.application.queryservices.ServiceQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.Service;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetAllServicesByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetServiceByIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.repositories.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class ServiceQueryServiceImpl implements ServiceQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceQueryServiceImpl.class);
    private final ServiceRepository serviceRepository;

    public ServiceQueryServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public Optional<Service> handle(GetServiceByIdQuery query) {
        if (query.id() == null) {
            LOGGER.warn("GetServiceByIdQuery received with null id");
            throw new IllegalArgumentException("operations.error.query.serviceId.required");
        }
        return serviceRepository.findById(query.id());
    }

    @Override
    public List<Service> handle(GetAllServicesByBranchIdQuery query) {
        if (query.branchId() == null) {
            LOGGER.warn("GetAllServicesByBranchIdQuery received with null branchId");
            throw new IllegalArgumentException("operations.error.query.branchId.required");
        }
        return serviceRepository.findAllByBranchId(query.branchId());
    }
}
