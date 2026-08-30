package com.andeva.atelier.platform.fleet.interfaces.rest.transform;

import com.andeva.atelier.platform.fleet.domain.model.commands.CreateAppointmentCommand;
import com.andeva.atelier.platform.fleet.domain.model.valueobjects.AppointmentSummary;
import com.andeva.atelier.platform.fleet.interfaces.rest.resources.CreateAppointmentResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

public class CreateAppointmentCommandFromResourceAssembler {

    public static CreateAppointmentCommand toCommandFromResource(CreateAppointmentResource resource) {
        return new CreateAppointmentCommand(
                new BranchId(resource.branchId()),
                new CustomerId(resource.customerId()),
                new VehicleId(resource.vehicleId()),
                resource.scheduledStart(),
                resource.notes() == null || resource.notes().isBlank()
                        ? null
                        : new AppointmentSummary(resource.notes())
        );
    }
}