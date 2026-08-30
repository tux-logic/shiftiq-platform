package com.andeva.atelier.platform.operations.application.internal.queryservices;

import com.andeva.atelier.platform.operations.application.queryservices.ServiceQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.queries.GetAllServicesByBranchIdQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetServiceByIdQuery;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceQueryServiceImpl implements ServiceQueryService {
    private final ServiceRepository serviceRepository;

    public ServiceQueryServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public Optional<Service> handle(GetServiceByIdQuery query) {
        return serviceRepository.findById(query.id());
    }

    @Override
    public List<Service> handle(GetAllServicesByBranchIdQuery query) {
        return serviceRepository.findAllByBranchId(query.branchId());
    }
}
