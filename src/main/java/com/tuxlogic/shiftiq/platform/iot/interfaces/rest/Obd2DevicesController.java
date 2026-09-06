package com.tuxlogic.shiftiq.platform.iot.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iot.application.commandservices.Obd2DeviceCommandFailure;
import com.tuxlogic.shiftiq.platform.iot.application.commandservices.Obd2DeviceCommandService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.Obd2DeviceQueryService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.TelemetryQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.Obd2Device;
import com.tuxlogic.shiftiq.platform.iot.domain.model.commands.DeleteObd2DeviceCommand;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetAvailableObd2DevicesQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetLatestTelemetrySnapshotQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetObd2DeviceByIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetObd2DevicesByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetTelemetrySnapshotHistoryQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;

import java.util.List;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.CreateObd2DeviceResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.Obd2DeviceResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.TelemetrySnapshotResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.UpdateObd2DeviceResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.CreateObd2DeviceCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.Obd2DeviceResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.ResponseEntityFromObd2DeviceCommandResultAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.TelemetrySnapshotResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.UpdateObd2DeviceCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import org.springframework.context.i18n.LocaleContextHolder;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * REST controller for managing OBD2 Devices registration.
 */
@RestController
@RequestMapping(value = "/api/v1/obd2-devices", produces = "application/json")
@Tag(name = "OBD2 Devices", description = "Endpoints for managing OBD2 hardware dongles and IoT telemetry")
@PreAuthorize("isAuthenticated()")
public class Obd2DevicesController {

    private final Obd2DeviceCommandService commandService;
    private final Obd2DeviceQueryService queryService;
    private final TelemetryQueryService telemetryQueryService;
    private final MessageSource messageSource;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public Obd2DevicesController(
            Obd2DeviceCommandService commandService,
            Obd2DeviceQueryService queryService,
            TelemetryQueryService telemetryQueryService,
            MessageSource messageSource,
            MultiTenancySecurityService multiTenancySecurityService
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.telemetryQueryService = telemetryQueryService;
        this.messageSource = messageSource;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    private Obd2Device validateAndGetDevice(UUID id) {
        var device = queryService.handle(new GetObd2DeviceByIdQuery(new Obd2DeviceId(id)))
                .orElseThrow(() -> new AccessDeniedException("Unauthorized access for requested device identifier: " + id));
        if (!multiTenancySecurityService.isAuthorizedForBranch(device.getBranchId().value())) {
            throw new AccessDeniedException("Unauthorized access for requested device identifier: " + id);
        }
        return device;
    }

    @PostMapping
    @Operation(summary = "Register a new OBD2 device", description = "Registers a new physical OBD2 device in the specified branch")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId())")
    public ResponseEntity<?> createObd2Device(@Valid @RequestBody CreateObd2DeviceResource resource) {
        var command = CreateObd2DeviceCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        return ResponseEntityFromObd2DeviceCommandResultAssembler.toResponseEntityFromResult(result, messageSource);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get OBD2 device by ID", description = "Retrieves the details of a registered OBD2 device by its unique ID")
    public ResponseEntity<Obd2DeviceResource> getObd2DeviceById(@PathVariable UUID id) {
        var device = validateAndGetDevice(id);
        return ResponseEntity.ok(Obd2DeviceResourceFromAggregateAssembler.toResourceFromAggregate(device));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an OBD2 device", description = "Performs a soft delete of an OBD2 device by its unique ID")
    public ResponseEntity<?> deleteObd2Device(@PathVariable UUID id) {
        validateAndGetDevice(id);
        var command = new DeleteObd2DeviceCommand(new Obd2DeviceId(id));
        var result = commandService.handle(command);

        return result.fold(
                _void -> ResponseEntity.noContent().build(),
                failure -> {
                    HttpStatus status = switch (failure) {
                        case Obd2DeviceCommandFailure.NotFound(String _) -> HttpStatus.NOT_FOUND;
                        case Obd2DeviceCommandFailure.InvalidState(String _) -> HttpStatus.BAD_REQUEST;
                        case Obd2DeviceCommandFailure.Duplicate(String _) -> HttpStatus.CONFLICT;
                    };
                    String messageKey = switch (failure) {
                        case Obd2DeviceCommandFailure.NotFound(String message) -> message;
                        case Obd2DeviceCommandFailure.InvalidState(String message) -> message;
                        case Obd2DeviceCommandFailure.Duplicate(String message) -> message;
                    };
                    String localizedMessage;
                    try {
                        localizedMessage = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
                    } catch (Exception e) {
                        localizedMessage = messageKey;
                    }
                    return ResponseEntity.status(status).body(
                            ProblemDetail.forStatusAndDetail(status, localizedMessage)
                    );
                }
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an OBD2 device", description = "Updates an existing registered OBD2 device's details (such as MAC address)")
    public ResponseEntity<?> updateObd2Device(@PathVariable UUID id, @Valid @RequestBody UpdateObd2DeviceResource resource) {
        validateAndGetDevice(id);
        var command = UpdateObd2DeviceCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var result = commandService.handle(command);
        return ResponseEntityFromObd2DeviceCommandResultAssembler.toResponseEntityFromResult(result, HttpStatus.OK, messageSource);
    }

    private static final String STATUS_AVAILABLE = "available";

    @GetMapping
    @Operation(summary = "Get OBD2 devices for a branch", description = "Retrieves all OBD2 devices for a specific branch. Filter by ?status=available to get unlinked devices.")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    public ResponseEntity<List<Obd2DeviceResource>> getObd2Devices(
            @RequestParam UUID branchId,
            @RequestParam(required = false) String status
    ) {
        List<Obd2Device> devices;
        if (STATUS_AVAILABLE.equalsIgnoreCase(status)) {
            devices = queryService.handle(new GetAvailableObd2DevicesQuery(new BranchId(branchId)));
        } else {
            devices = queryService.handle(new GetObd2DevicesByBranchIdQuery(new BranchId(branchId)));
        }
        var resources = devices.stream()
                .map(Obd2DeviceResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{deviceId}/telemetry-snapshots/latest")
    @Operation(summary = "Get latest telemetry snapshot for a device", description = "Retrieves the most recent telemetry capture from a specific OBD2 device")
    public ResponseEntity<TelemetrySnapshotResource> getLatestTelemetrySnapshot(@PathVariable UUID deviceId) {
        validateAndGetDevice(deviceId);
        var query = new GetLatestTelemetrySnapshotQuery(new Obd2DeviceId(deviceId));
        var result = telemetryQueryService.handle(query);

        return result
                .map(snapshot -> ResponseEntity.ok(
                        TelemetrySnapshotResourceFromAggregateAssembler.toResourceFromAggregate(snapshot)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{deviceId}/telemetry-snapshots")
    @Operation(summary = "Get telemetry snapshot history for a device", description = "Retrieves all telemetry snapshots recorded for a specific OBD2 device ordered descending by date")
    public ResponseEntity<List<TelemetrySnapshotResource>> getTelemetrySnapshotHistory(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validateAndGetDevice(deviceId);
        var query = new GetTelemetrySnapshotHistoryQuery(new Obd2DeviceId(deviceId), page, size);
        var list = telemetryQueryService.handle(query);
        var resources = list.stream()
                .map(TelemetrySnapshotResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();
        return ResponseEntity.ok(resources);
    }
}