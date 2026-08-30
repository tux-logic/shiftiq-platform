package com.andeva.atelier.platform.iot.application.internal.queryservices;

import com.andeva.atelier.platform.core.application.queryservices.CustomerQueryService;
import com.andeva.atelier.platform.core.domain.model.queries.GetCustomerByIdQuery;
import com.andeva.atelier.platform.iot.application.queryservices.VehicleQueryService;
import com.andeva.atelier.platform.iot.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.iot.domain.model.queries.GetActiveVehiclesByCustomerIdQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetVehiclesAvailableForLinkingQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetVehicleByIdQuery;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleRegistrationRepository;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for handling Vehicle queries inside the iot context.
 */
@Service
public class VehicleQueryServiceImpl implements VehicleQueryService {

    private final VehicleRepository vehicleRepository;
    private final CustomerQueryService customerQueryService;
    private final VehicleRegistrationRepository vehicleRegistrationRepository;

    public VehicleQueryServiceImpl(
            VehicleRepository vehicleRepository,
            CustomerQueryService customerQueryService,
            VehicleRegistrationRepository vehicleRegistrationRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.customerQueryService = customerQueryService;
        this.vehicleRegistrationRepository = vehicleRegistrationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> handle(GetVehiclesAvailableForLinkingQuery query) {
        return vehicleRepository.findAvailableForLinkingByBranchId(query.branchId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> handle(GetActiveVehiclesByCustomerIdQuery query) {
        var customerOpt = customerQueryService.handle(new GetCustomerByIdQuery(query.customerId()));
        if (customerOpt.isEmpty()) {
            return List.of();
        }
        var customer = customerOpt.get();
        var userId = customer.getUserId().value();
        var activeRegistrations = vehicleRegistrationRepository.findAllActiveByUserId(userId);
        return activeRegistrations.stream()
                .map(reg -> vehicleRepository.findById(reg.getVehicleId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicle> handle(GetVehicleByIdQuery query) {
        return vehicleRepository.findById(query.vehicleId());
    }
}
