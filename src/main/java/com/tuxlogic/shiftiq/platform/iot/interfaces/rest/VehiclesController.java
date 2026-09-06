package com.tuxlogic.shiftiq.platform.iot.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.tuxlogic.shiftiq.platform.iot.application.commandservices.VehicleCommandService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.DtcAlertQueryService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.TelemetryQueryService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.VehicleQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehiclesAvailableForLinkingQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleDtcAlertHistoryQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleTelemetrySnapshotHistoryQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleByIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.Obd2DeviceRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.VehicleRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.DtcAlertResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.RegisterVehicleResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.TelemetrySnapshotResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.UpdateVehicleResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.DtcAlertResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.RegisterVehicleCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.ResponseEntityFromVehicleCommandResultAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.TelemetrySnapshotResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.UpdateVehicleCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.iot.domain.model.commands.DeleteVehicleCommand;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.VehicleResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Vehicle operations inside the iot context.
 */
@RestController
@RequestMapping(value = "/api/v1/vehicles", produces = "application/json")
@Tag(name = "Vehicles", description = "Endpoints for registering and managing customer vehicles")
@PreAuthorize("isAuthenticated()")
public class VehiclesController {

    private static final String MSG_UNSUPPORTED_VEHICLE_STATUS_FILTER = "iot.error.vehicle.unsupportedStatusFilter";

    private final VehicleQueryService vehicleQueryService;
    private final VehicleCommandService vehicleCommandService;
    private final TelemetryQueryService telemetryQueryService;
    private final DtcAlertQueryService dtcAlertQueryService;
    private final MessageSource messageSource;
    private final MultiTenancySecurityService multiTenancySecurityService;
    private final Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository;
    private final VehicleRegistrationRepository vehicleRegistrationRepository;

    public VehiclesController(VehicleQueryService vehicleQueryService,
                              VehicleCommandService vehicleCommandService,
                              TelemetryQueryService telemetryQueryService,
                              DtcAlertQueryService dtcAlertQueryService,
                              MessageSource messageSource,
                              MultiTenancySecurityService multiTenancySecurityService,
                              Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository,
                              VehicleRegistrationRepository vehicleRegistrationRepository) {
        this.vehicleQueryService = vehicleQueryService;
        this.vehicleCommandService = vehicleCommandService;
        this.telemetryQueryService = telemetryQueryService;
        this.dtcAlertQueryService = dtcAlertQueryService;
        this.messageSource = messageSource;
        this.multiTenancySecurityService = multiTenancySecurityService;
        this.obd2DeviceRegistrationRepository = obd2DeviceRegistrationRepository;
        this.vehicleRegistrationRepository = vehicleRegistrationRepository;
    }

    /**
     * Validates that the authenticated caller may access the given vehicle.
     * A Vehicle has no branchId of its own: if it has an active OBD2 link, we
     * validate branch access through it (staff use case); otherwise we fall
     * back to checking that the caller is the vehicle's registered driver
     * (customer use case). If neither link exists, access is denied.
     */
    private void validateVehicleAccess(UUID vehicleId, Authentication authentication) {
        var vehicleIdVo = new VehicleId(vehicleId);

        var activeDeviceRegistration = obd2DeviceRegistrationRepository.findActiveByVehicleId(vehicleIdVo);
        if (activeDeviceRegistration.isPresent()) {
            BranchId branchId = activeDeviceRegistration.get().getBranchId();
            if (multiTenancySecurityService.isAuthorizedForBranch(branchId.value())) {
                return;
            }
        }

        var activeDriverRegistration = vehicleRegistrationRepository.findActiveByVehicleId(vehicleIdVo);
        if (activeDriverRegistration.isPresent()) {
            var userDetails = (UserDetailsImpl) authentication.getPrincipal();
            if (activeDriverRegistration.get().getUserId().equals(userDetails.getId())) {
                return;
            }
        }

        throw new AccessDeniedException("Unauthorized access for requested vehicle identifier: " + vehicleId);
    }

    private static final String STATUS_AVAILABLE_FOR_LINKING = "available-for-linking";

    @GetMapping
    @Operation(summary = "Get vehicles by branch and status", description = "Retrieves vehicles under a specific branch. Use ?status=available-for-linking to get vehicles available for OBD2 device linking.")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    public ResponseEntity<?> getVehicles(
            @RequestParam UUID branchId,
            @RequestParam String status) {

        if (STATUS_AVAILABLE_FOR_LINKING.equalsIgnoreCase(status)) {
            var query = new GetVehiclesAvailableForLinkingQuery(new BranchId(branchId));
            var list = vehicleQueryService.handle(query);
            var resources = list.stream()
                    .map(VehicleResourceFromAggregateAssembler::toResourceFromAggregate)
                    .toList();
            return ResponseEntity.ok(resources);
        }

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNPROCESSABLE_CONTENT,
                        messageSource.getMessage(MSG_UNSUPPORTED_VEHICLE_STATUS_FILTER, new Object[]{status}, LocaleContextHolder.getLocale())
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client vehicle by ID", description = "Retrieves client vehicle details by its unique identifier")
    public ResponseEntity<?> getVehicleById(@PathVariable UUID id, Authentication authentication) {
        validateVehicleAccess(id, authentication);
        var query = new GetVehicleByIdQuery(new VehicleId(id));
        var vehicleOpt = vehicleQueryService.handle(query);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(VehicleResourceFromAggregateAssembler.toResourceFromAggregate(vehicleOpt.get()));
    }

    @PostMapping
    @Operation(summary = "Register a client vehicle", description = "Registers a client vehicle and links it to the authenticated user")
    public ResponseEntity<?> registerVehicle(
            @Valid @RequestBody RegisterVehicleResource resource,
            Authentication authentication) {
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UUID userId = userDetails.getId();

        var command = RegisterVehicleCommandFromResourceAssembler.toCommandFromResource(userId, resource);
        var result = vehicleCommandService.handle(command);

        return ResponseEntityFromVehicleCommandResultAssembler.toResponseEntityFromRegistrationResult(result, messageSource);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update client vehicle", description = "Updates client vehicle details by its unique identifier")
    public ResponseEntity<?> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleResource resource,
            Authentication authentication) {
        validateVehicleAccess(id, authentication);
        var command = UpdateVehicleCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var result = vehicleCommandService.handle(command);

        return ResponseEntityFromVehicleCommandResultAssembler.toResponseEntityFromVehicleResult(result, messageSource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete client vehicle", description = "Performs a soft delete of a vehicle and deactivates active driver/OBD2 links")
    public ResponseEntity<?> deleteVehicle(@PathVariable UUID id, Authentication authentication) {
        validateVehicleAccess(id, authentication);
        var command = new DeleteVehicleCommand(new VehicleId(id));
        var result = vehicleCommandService.handle(command);
        return ResponseEntityFromVehicleCommandResultAssembler.toResponseEntityFromVoidResult(result, messageSource);
    }

    @GetMapping("/{vehicleId}/telemetry-snapshots")
    @Operation(summary = "Get historical telemetry snapshots for vehicle", description = "Retrieves all telemetry snapshots captured for the vehicle since its active registration start date")
    public ResponseEntity<List<TelemetrySnapshotResource>> getVehicleTelemetrySnapshots(
            @PathVariable UUID vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        validateVehicleAccess(vehicleId, authentication);
        var query = new GetVehicleTelemetrySnapshotHistoryQuery(new VehicleId(vehicleId), page, size);
        var list = telemetryQueryService.handle(query);
        var resources = list.stream()
                .map(TelemetrySnapshotResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{vehicleId}/dtc-alerts")
    @Operation(summary = "Get historical DTC alerts for vehicle", description = "Retrieves all DTC/motor alerts captured for the vehicle since its active registration start date")
    public ResponseEntity<List<DtcAlertResource>> getVehicleDtcAlerts(
            @PathVariable UUID vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        validateVehicleAccess(vehicleId, authentication);
        var query = new GetVehicleDtcAlertHistoryQuery(new VehicleId(vehicleId), page, size);
        var list = dtcAlertQueryService.handle(query);
        var resources = list.stream()
                .map(DtcAlertResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();
        return ResponseEntity.ok(resources);
    }
}