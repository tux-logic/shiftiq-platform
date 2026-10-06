package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.*;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.*;

/**
 * Command object representing the data required to create a new Work Order in the system. This command encapsulates all necessary information such as appointment details, branch, vehicle, customer, and diagnostic information.
 * @param appointmentId
 * @param branchId
 * @param vehicleId
 * @param customerId
 * @param diagnosticSummary
 * @param mileageIn
 * @author Joel Huamani Estefanero
 */
import java.util.List;

public record CreateWorkOrderCommand(
        AppointmentId appointmentId,
        BranchId branchId,
        VehicleId vehicleId,
        CustomerId customerId,
        DiagnosticSummary diagnosticSummary,
        Mileage mileageIn,
        List<String> entryInspectionImages
) {
    public CreateWorkOrderCommand(AppointmentId appointmentId, BranchId branchId, VehicleId vehicleId, CustomerId customerId, DiagnosticSummary diagnosticSummary, Mileage mileageIn) {
        this(appointmentId, branchId, vehicleId, customerId, diagnosticSummary, mileageIn, java.util.Collections.emptyList());
    }

    public CreateWorkOrderCommand {
        if (appointmentId == null) throw new IllegalArgumentException("operations.error.command.appointmentId.required");
        if (branchId == null) throw new IllegalArgumentException("operations.error.command.branchId.required");
        if (vehicleId == null) throw new IllegalArgumentException("operations.error.command.vehicleId.required");
        if (customerId == null) throw new IllegalArgumentException("operations.error.command.customerId.required");
        if (diagnosticSummary == null) throw new IllegalArgumentException("operations.error.command.diagnosticSummary.required");
        if (mileageIn == null) throw new IllegalArgumentException("operations.error.command.mileageIn.required");
    }
}
