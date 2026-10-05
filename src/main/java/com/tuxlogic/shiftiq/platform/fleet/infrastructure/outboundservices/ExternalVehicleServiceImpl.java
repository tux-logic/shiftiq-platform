package com.tuxlogic.shiftiq.platform.fleet.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalVehicleService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.VehicleQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleByIdQuery;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;

/**
 * ACL adapter delegating vehicle existence checks to the iot context through
 * its application layer, so fleet never couples to iot repositories.
 */
@Service
public class ExternalVehicleServiceImpl implements ExternalVehicleService {

    private final VehicleQueryService vehicleQueryService;

    public ExternalVehicleServiceImpl(VehicleQueryService vehicleQueryService) {
        this.vehicleQueryService = vehicleQueryService;
    }

    @Override
    public boolean existsVehicleById(VehicleId vehicleId) {
        if (vehicleId == null) return false;
        return vehicleQueryService.handle(new GetVehicleByIdQuery(vehicleId)).isPresent();
    }
}
