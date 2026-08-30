package com.tuxlogic.shiftiq.platform.operations.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Query to retrieve all Work Orders associated with a specific Vehicle ID. This query encapsulates the necessary identifier to perform the retrieval operation.
 * @param vehicleId
 * @author Joel Huamani Estefanero
 */
public record GetWorkOrdersByVehicleIdQuery(VehicleId vehicleId) {}
