package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;

import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByDocumentNumberQuery;
import com.tuxlogic.shiftiq.platform.core.application.commandservices.EmployeeCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.EmployeeQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeleteEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateEmployeeResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.EmployeeResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateEmployeeResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.CreateEmployeeCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.EmployeeResourceFromEntityAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.UpdateEmployeeCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;

@RestController
@RequestMapping(value = "/api/v1/employees", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Employees", description = "Employee Management Endpoints")
@PreAuthorize("isAuthenticated()")
public class EmployeesController {

    private final EmployeeCommandService employeeCommandService;
    private final EmployeeQueryService employeeQueryService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public EmployeesController(EmployeeCommandService employeeCommandService, EmployeeQueryService employeeQueryService, MultiTenancySecurityService multiTenancySecurityService) {
        this.employeeCommandService = employeeCommandService;
        this.employeeQueryService = employeeQueryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @Operation(summary = "Create a new employee profile", description = "Creates a new employee profile associated with a user ID")
    @PostMapping
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForUser(#resource.userId())")
    public ResponseEntity<EmployeeResource> createEmployee(@Valid @RequestBody CreateEmployeeResource resource) {
        multiTenancySecurityService.validateUserAccess(resource.userId());
        var command = CreateEmployeeCommandFromResourceAssembler.toCommandFromResource(resource);
        var employee = employeeCommandService.handle(command);
        if (employee.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var employeeResource = EmployeeResourceFromEntityAssembler.toResourceFromEntity(employee.get());
        return new ResponseEntity<>(employeeResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an employee profile", description = "Updates an existing employee profile")
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeResource> updateEmployee(@PathVariable UUID employeeId, @Valid @RequestBody UpdateEmployeeResource resource) {
        var existing = employeeQueryService.handle(new GetEmployeeByIdQuery(new EmployeeId(employeeId)));
        existing.ifPresent(e -> multiTenancySecurityService.validateUserAccess(e.getUserId().value()));

        var command = UpdateEmployeeCommandFromResourceAssembler.toCommandFromResource(employeeId, resource);
        var employee = employeeCommandService.handle(command);
        if (employee.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var employeeResource = EmployeeResourceFromEntityAssembler.toResourceFromEntity(employee.get());
        return ResponseEntity.ok(employeeResource);
    }

    @Operation(summary = "Get an employee profile by ID", description = "Retrieves the details of a specific employee profile")
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResource> getEmployeeById(@PathVariable UUID employeeId) {
        var query = new GetEmployeeByIdQuery(new EmployeeId(employeeId));
        var employee = employeeQueryService.handle(query);
        if (employee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        multiTenancySecurityService.validateUserAccess(employee.get().getUserId().value());
        var employeeResource = EmployeeResourceFromEntityAssembler.toResourceFromEntity(employee.get());
        return ResponseEntity.ok(employeeResource);
    }

    @Operation(summary = "Get an employee profile by User ID", description = "Retrieves the details of a specific employee profile using the User ID")
    @GetMapping(params = "userId")
    public ResponseEntity<EmployeeResource> getEmployeeByUserId(@RequestParam(name = "userId") UUID userId) {
        multiTenancySecurityService.validateUserAccess(userId);
        var query = new GetEmployeeByUserIdQuery(new UserId(userId));
        var employee = employeeQueryService.handle(query);
        if (employee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var employeeResource = EmployeeResourceFromEntityAssembler.toResourceFromEntity(employee.get());
        return ResponseEntity.ok(employeeResource);
    }

    @Operation(summary = "Get an employee profile by Document Number", description = "Retrieves the details of a specific employee profile using their DNI/RUC")
    @GetMapping(params = "documentNumber")
    public ResponseEntity<EmployeeResource> getEmployeeByDocumentNumber(@RequestParam(name = "documentNumber") String documentNumber) {
        var query = new GetEmployeeByDocumentNumberQuery(documentNumber);
        var employee = employeeQueryService.handle(query);
        if (employee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        multiTenancySecurityService.validateUserAccess(employee.get().getUserId().value());
        var employeeResource = EmployeeResourceFromEntityAssembler.toResourceFromEntity(employee.get());
        return ResponseEntity.ok(employeeResource);
    }

    @Operation(summary = "Delete an employee profile", description = "Deletes an existing employee profile")
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<?> deleteEmployee(@PathVariable UUID employeeId) {
        var existing = employeeQueryService.handle(new GetEmployeeByIdQuery(new EmployeeId(employeeId)));
        existing.ifPresent(e -> multiTenancySecurityService.validateUserAccess(e.getUserId().value()));

        var command = new DeleteEmployeeCommand(new EmployeeId(employeeId));
        employeeCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
