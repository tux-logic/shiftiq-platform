package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandService;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.EmployeeRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByEmployeeIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdAndStatusQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.ApproveEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.RejectEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.CreateEmployeeRegistrationResource;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.RequestEmployeeJoinResource;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.RejectEmployeeRegistrationResource;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.UpdateEmployeeRegistrationResource;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform.CreateEmployeeRegistrationCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform.EmployeeRegistrationResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform.RequestEmployeeJoinCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform.UpdateEmployeeRegistrationCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/employee-registrations", produces = "application/json")
@Tag(name = "EmployeeRegistrations", description = "Employee Registration Management Endpoints")
@PreAuthorize("isAuthenticated()")
public class EmployeeRegistrationsController {

    private final EmployeeRegistrationCommandService commandService;
    private final EmployeeRegistrationQueryService queryService;
    private final MessageSource messageSource;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public EmployeeRegistrationsController(EmployeeRegistrationCommandService commandService,
                                           EmployeeRegistrationQueryService queryService,
                                           MessageSource messageSource,
                                           MultiTenancySecurityService multiTenancySecurityService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.messageSource = messageSource;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @PostMapping
    @Operation(summary = "Create a new employee registration", description = "Creates a new employee registration")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId()))")
    public ResponseEntity<?> create(@Valid @RequestBody CreateEmployeeRegistrationResource resource) {
        var command = CreateEmployeeRegistrationCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        return result.fold(
                registration -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(EmployeeRegistrationResourceFromAggregateAssembler.toResourceFromAggregate(registration)),
                this::handleCommandFailure
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee registration by ID", description = "Retrieves an employee registration by ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        var query = new GetEmployeeRegistrationByIdQuery(new EmployeeId(id));
        var result = queryService.handle(query);
        if (result.isFailure()) {
            return ResponseEntity.notFound().build();
        }
        var registration = result.success().get();
        if (!multiTenancySecurityService.isAuthorizedForBranch(registration.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var resource = EmployeeRegistrationResourceFromAggregateAssembler.toResourceFromAggregate(registration);
        return ResponseEntity.ok(resource);
    }

    @GetMapping
    @Operation(summary = "Get employee registrations", description = "Get registrations filtered by branch, branch and status, or employee ID")
    public ResponseEntity<?> getRegistrations(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID employeeId) {

        if (employeeId != null) {
            var query = new GetEmployeeRegistrationByEmployeeIdQuery(employeeId);
            var result = queryService.handle(query);
            if (result.isFailure()) {
                return ResponseEntity.notFound().build();
            }
            var registration = result.success().get();
            if (!multiTenancySecurityService.isAuthorizedForBranch(registration.getBranchId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(EmployeeRegistrationResourceFromAggregateAssembler.toResourceFromAggregate(registration));
        } else if (branchId != null && status != null) {
            if (!multiTenancySecurityService.isAuthorizedForBranch(branchId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            var query = new GetEmployeeRegistrationsByBranchIdAndStatusQuery(new BranchId(branchId), new EmployeeRegistrationStatus(status.toUpperCase()));
            var result = queryService.handle(query);
            if (result.isFailure()) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(result.success().get().stream()
                    .map(EmployeeRegistrationResourceFromAggregateAssembler::toResourceFromAggregate)
                    .toList());
        } else if (branchId != null) {
            if (!multiTenancySecurityService.isAuthorizedForBranch(branchId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            var query = new GetEmployeeRegistrationsByBranchIdQuery(new BranchId(branchId));
            var result = queryService.handle(query);
            if (result.isFailure()) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(result.success().get().stream()
                    .map(EmployeeRegistrationResourceFromAggregateAssembler::toResourceFromAggregate)
                    .toList());
        }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee registration", description = "Updates the speciality and salary of an existing employee registration")
    public ResponseEntity<?> updateEmployeeRegistration(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmployeeRegistrationResource resource) {
        
        var queryResult = queryService.handle(new GetEmployeeRegistrationByIdQuery(new EmployeeId(id)));
        if (queryResult.isFailure()) {
            return ResponseEntity.notFound().build();
        }
        var registration = queryResult.success().get();
        if (!multiTenancySecurityService.isAuthorizedForBranch(registration.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var command = UpdateEmployeeRegistrationCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var result = commandService.handle(command);
        
        return result.fold(
                updatedReg -> ResponseEntity.ok(EmployeeRegistrationResourceFromAggregateAssembler.toResourceFromAggregate(updatedReg)),
                this::handleCommandFailure
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate an employee registration", description = "Performs a soft delete on an employee registration, marking it as inactive")
    public ResponseEntity<?> deactivateEmployeeRegistration(@PathVariable UUID id) {
        var queryResult = queryService.handle(new GetEmployeeRegistrationByIdQuery(new EmployeeId(id)));
        if (queryResult.isFailure()) {
            return ResponseEntity.notFound().build();
        }
        var registration = queryResult.success().get();
        if (!multiTenancySecurityService.isAuthorizedForBranch(registration.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var command = new DeleteEmployeeRegistrationCommand(new EmployeeId(id));
        var result = commandService.handle(command);
        
        return result.fold(
                deletedReg -> ResponseEntity.noContent().build(),
                this::handleCommandFailure
        );
    }

    @PostMapping("/request-join")
    @Operation(summary = "Request employee join to branch", description = "Submits a request to join a branch in PENDING_APPROVAL status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> requestJoin(@Valid @RequestBody RequestEmployeeJoinResource resource) {
        var command = RequestEmployeeJoinCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        return result.fold(
                registration -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(EmployeeRegistrationResourceFromAggregateAssembler.toResourceFromAggregate(registration)),
                this::handleCommandFailure
        );
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve an employee registration request", description = "Approves a pending employee registration, setting status to ACTIVE and assigning branch in IAM")
    public ResponseEntity<?> approve(@PathVariable UUID id) {
        var queryResult = queryService.handle(new GetEmployeeRegistrationByIdQuery(new EmployeeId(id)));
        if (queryResult.isFailure()) {
            return ResponseEntity.notFound().build();
        }
        var registration = queryResult.success().get();
        if (!multiTenancySecurityService.isAuthorizedForBranch(registration.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var command = new ApproveEmployeeRegistrationCommand(new EmployeeId(id));
        var result = commandService.handle(command);
        return result.fold(
                approvedReg -> ResponseEntity.ok(EmployeeRegistrationResourceFromAggregateAssembler.toResourceFromAggregate(approvedReg)),
                this::handleCommandFailure
        );
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject an employee registration request", description = "Rejects a pending employee registration")
    public ResponseEntity<?> reject(@PathVariable UUID id, @RequestBody(required = false) RejectEmployeeRegistrationResource resource) {
        var queryResult = queryService.handle(new GetEmployeeRegistrationByIdQuery(new EmployeeId(id)));
        if (queryResult.isFailure()) {
            return ResponseEntity.notFound().build();
        }
        var registration = queryResult.success().get();
        if (!multiTenancySecurityService.isAuthorizedForBranch(registration.getBranchId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String reason = resource != null ? resource.reason() : null;
        var command = new RejectEmployeeRegistrationCommand(new EmployeeId(id), reason);
        var result = commandService.handle(command);
        return result.fold(
                rejectedReg -> ResponseEntity.ok(EmployeeRegistrationResourceFromAggregateAssembler.toResourceFromAggregate(rejectedReg)),
                this::handleCommandFailure
        );
    }

    private ResponseEntity<?> handleCommandFailure(EmployeeRegistrationCommandFailure failure) {
        String messageKey = switch (failure) {
            case REGISTRATION_ALREADY_EXISTS -> "fleet.error.employeeRegistration.alreadyExists";
            case REGISTRATION_NOT_FOUND -> "fleet.error.employeeRegistration.notFound";
            case INVALID_REGISTRATION_DATA -> "fleet.error.employeeRegistration.invalidData";
            case INVALID_STATUS_TRANSITION -> "fleet.error.employeeRegistration.invalidStatusTransition";
        };
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        ApplicationError error = switch (failure) {
            case REGISTRATION_ALREADY_EXISTS -> ApplicationError.conflict("employeeRegistration", message);
            case REGISTRATION_NOT_FOUND -> ApplicationError.notFound("employeeRegistration", message);
            case INVALID_REGISTRATION_DATA, INVALID_STATUS_TRANSITION -> ApplicationError.validationError("employeeRegistration", message);
        };
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
    }
}
