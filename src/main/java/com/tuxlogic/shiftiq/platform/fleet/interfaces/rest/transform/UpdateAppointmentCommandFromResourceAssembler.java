package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateAppointmentCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentSummary;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.UpdateAppointmentResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.UUID;

public class UpdateAppointmentCommandFromResourceAssembler {

    public static UpdateAppointmentCommand toCommandFromResource(UUID appointmentId, UpdateAppointmentResource resource) {
        return new UpdateAppointmentCommand(
                appointmentId,
                new BranchId(resource.branchId()),
                new CustomerId(resource.customerId()),
                new VehicleId(resource.vehicleId()),
                resource.scheduledStart(),
                resource.status(),
                resource.notes() == null || resource.notes().isBlank()
                        ? null
                        : new AppointmentSummary(resource.notes())
        );
    }
}