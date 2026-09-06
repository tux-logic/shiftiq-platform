package com.tuxlogic.shiftiq.platform.iot.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iot.application.commandservices.Obd2DeviceRegistrationCommandService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.DtcAlertQueryService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.Obd2DeviceRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.TelemetryQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.commands.DeactivateObd2DeviceRegistrationCommand;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetDtcAlertsByRegistrationIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetObd2DeviceRegistrationsByBranchIdAndStatusQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetTelemetrySnapshotsByRegistrationIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceRegistrationId;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2RegistrationStatus;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.Obd2DeviceRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.DtcAlertResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.LinkObd2DeviceResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.Obd2DeviceRegistrationResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.TelemetrySnapshotResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.UpdateObd2DeviceRegistrationStatusResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.DtcAlertResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.LinkObd2DeviceCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.Obd2DeviceRegistrationResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.ResponseEntityFromObd2DeviceRegistrationCommandResultAssembler;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.TelemetrySnapshotResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * REST controller for managing OBD2 Device Registrations (linking devices to vehicles).
 */
@RestController
@RequestMapping(value = "/api/v1/obd2-device-registrations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "OBD2 Device Registrations", description = "Endpoints for managing OBD2-vehicle couplings")
@PreAuthorize("isAuthenticated()")
public class Obd2DeviceRegistrationsController {

    private static final String MSG_UNSUPPORTED_STATUS_TRANSITION = "iot.error.obd2DeviceRegistration.unsupportedStatusTransition";

    private final Obd2DeviceRegistrationCommandService commandService;
    private final Obd2DeviceRegistrationQueryService queryService;
    private final TelemetryQueryService telemetryQueryService;
    private final DtcAlertQueryService dtcAlertQueryService;
    private final MessageSource messageSource;
    private final MultiTenancySecurityService multiTenancySecurityService;
    private final Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository;

    public Obd2DeviceRegistrationsController(
            Obd2DeviceRegistrationCommandService commandService,
            Obd2DeviceRegistrationQueryService queryService,
            TelemetryQueryService telemetryQueryService,
            DtcAlertQueryService dtcAlertQueryService,
            MessageSource messageSource,
            MultiTenancySecurityService multiTenancySecurityService,
            Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.telemetryQueryService = telemetryQueryService;
        this.dtcAlertQueryService = dtcAlertQueryService;
        this.messageSource = messageSource;
        this.multiTenancySecurityService = multiTenancySecurityService;
        this.obd2DeviceRegistrationRepository = obd2DeviceRegistrationRepository;
    }

    private void validateRegistrationAccess(UUID id) {
        var registration = obd2DeviceRegistrationRepository.findById(new Obd2DeviceRegistrationId(id))
                .orElseThrow(() -> new AccessDeniedException("Unauthorized access for requested registration identifier: " + id));
        if (!multiTenancySecurityService.isAuthorizedForBranch(registration.getBranchId().value())) {
            throw new AccessDeniedException("Unauthorized access for requested registration identifier: " + id);
        }
    }

    @PostMapping
    @Operation(summary = "Link OBD2 device to vehicle", description = "Links a registered OBD2 device to a specific vehicle inside a branch")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId())")
    public ResponseEntity<?> linkObd2Device(@Valid @RequestBody LinkObd2DeviceResource resource) {
        var command = LinkObd2DeviceCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        return ResponseEntityFromObd2DeviceRegistrationCommandResultAssembler.toResponseEntityFromResult(result, messageSource);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update OBD2 device registration status", description = "Updates the status of an OBD2-vehicle coupling. Use status=INACTIVE to deactivate/unlink.")
    public ResponseEntity<?> updateObd2DeviceRegistrationStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateObd2DeviceRegistrationStatusResource resource) {
        validateRegistrationAccess(id);

        if ("INACTIVE".equalsIgnoreCase(resource.status())) {
            var command = new DeactivateObd2DeviceRegistrationCommand(new Obd2DeviceRegistrationId(id));
            var result = commandService.handle(command);
            return ResponseEntityFromObd2DeviceRegistrationCommandResultAssembler.toResponseEntityFromResult(result, HttpStatus.OK, messageSource);
        }

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        messageSource.getMessage(MSG_UNSUPPORTED_STATUS_TRANSITION, new Object[]{resource.status()}, LocaleContextHolder.getLocale())
                )
        );
    }

    @GetMapping
    @Operation(summary = "Get OBD2 device registrations by branch and status", description = "Retrieves all registered OBD2-vehicle couplings under a specific branch, filtered by status")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    public ResponseEntity<List<Obd2DeviceRegistrationResource>> getObd2DeviceRegistrations(
            @RequestParam UUID branchId,
            @RequestParam String status
    ) {
        var query = new GetObd2DeviceRegistrationsByBranchIdAndStatusQuery(
                new BranchId(branchId),
                new Obd2RegistrationStatus(status)
        );
        var list = queryService.handle(query);
        var resources = list.stream()
                .map(Obd2DeviceRegistrationResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}/telemetry-snapshots")
    @Operation(summary = "Get telemetry snapshots for registration", description = "Retrieves all telemetry snapshots captured under a specific OBD2-vehicle registration")
    public ResponseEntity<List<TelemetrySnapshotResource>> getTelemetrySnapshotsForRegistration(@PathVariable UUID id) {
        validateRegistrationAccess(id);
        var query = new GetTelemetrySnapshotsByRegistrationIdQuery(new Obd2DeviceRegistrationId(id));
        var list = telemetryQueryService.handle(query);
        var resources = list.stream()
                .map(TelemetrySnapshotResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}/dtc-alerts")
    @Operation(summary = "Get DTC alerts for registration", description = "Retrieves all DTC alerts captured under a specific OBD2-vehicle registration")
    public ResponseEntity<List<DtcAlertResource>> getDtcAlertsForRegistration(@PathVariable UUID id) {
        validateRegistrationAccess(id);
        var query = new GetDtcAlertsByRegistrationIdQuery(new Obd2DeviceRegistrationId(id));
        var list = dtcAlertQueryService.handle(query);
        var resources = list.stream()
                .map(DtcAlertResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();
        return ResponseEntity.ok(resources);
    }
}