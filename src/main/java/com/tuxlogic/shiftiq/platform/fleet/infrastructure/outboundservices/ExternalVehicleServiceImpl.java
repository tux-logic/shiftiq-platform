package com.tuxlogic.shiftiq.platform.fleet.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalVehicleService;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.VehicleRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;

@Service
public class ExternalVehicleServiceImpl implements ExternalVehicleService {

    private final VehicleRepository vehicleRepository;

    public ExternalVehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public boolean existsVehicleById(VehicleId vehicleId) {
        if (vehicleId == null) return false;
        return vehicleRepository.findById(vehicleId).isPresent();
    }
}
