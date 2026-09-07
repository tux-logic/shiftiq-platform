package com.tuxlogic.shiftiq.platform.operations.interfaces.rest;

import com.tuxlogic.shiftiq.platform.operations.application.commandservices.ServiceCommandService;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.ServiceQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.Service;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.DeleteServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetAllServicesByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.ServiceId;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.resources.CreateServiceResource;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.resources.ServiceResource;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.resources.UpdateServiceResource;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.transform.CreateServiceCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.transform.ServiceResourceFromEntityAssembler;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.transform.UpdateServiceCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.OperationsMessageKeys;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetServiceByIdQuery;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(value = "/api/v1/services", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Services", description = "Services Management Endpoints")
@PreAuthorize("isAuthenticated()")
public class ServicesController {

    private final ServiceCommandService serviceCommandService;
    private final ServiceQueryService serviceQueryService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public ServicesController(ServiceCommandService serviceCommandService,
                              ServiceQueryService serviceQueryService,
                              MultiTenancySecurityService multiTenancySecurityService) {
        this.serviceCommandService = serviceCommandService;
        this.serviceQueryService = serviceQueryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    private Service validateServiceAccess(UUID serviceId) {
        Service service = serviceQueryService.handle(new GetServiceByIdQuery(new ServiceId(serviceId)))
                .orElseThrow(() -> new IllegalArgumentException(OperationsMessageKeys.SERVICE_NOT_FOUND));
        multiTenancySecurityService.validateBranchAccess(service.getBranchId().value());
        return service;
    }

    @Operation(summary = "Create a new service", description = "Creates a new service with the provided details")
    @PostMapping
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId())")
    public ResponseEntity<ServiceResource> createService(@Valid @RequestBody CreateServiceResource resource) {
        var command = CreateServiceCommandFromResourceAssembler.toCommandFromResource(resource);
        var service = serviceCommandService.handle(command);
        if (service.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var serviceResource = ServiceResourceFromEntityAssembler.toResourceFromEntity(service.get());
        return new ResponseEntity<>(serviceResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a service", description = "Updates an existing service using the service ID")
    @PutMapping("/{serviceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceResource> updateService(@PathVariable UUID serviceId, @Valid @RequestBody UpdateServiceResource resource) {
        validateServiceAccess(serviceId);
        var command = UpdateServiceCommandFromResourceAssembler.toCommandFromResource(serviceId, resource);
        var service = serviceCommandService.handle(command);
        if (service.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var serviceResource = ServiceResourceFromEntityAssembler.toResourceFromEntity(service.get());
        return ResponseEntity.ok(serviceResource);
    }

    @Operation(summary = "Delete a service", description = "Deletes an existing service using the service ID")
    @DeleteMapping("/{serviceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteService(@PathVariable UUID serviceId) {
        validateServiceAccess(serviceId);
        var command = new DeleteServiceCommand(new ServiceId(serviceId));
        serviceCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get services by branch ID", description = "Retrieves all services belonging to a specific branch")
    @GetMapping
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    public ResponseEntity<List<ServiceResource>> getServicesByBranchId(@RequestParam(name = "branchId") UUID branchId) {
        var query = new GetAllServicesByBranchIdQuery(new BranchId(branchId));
        var services = serviceQueryService.handle(query);

        var serviceResources = services.stream()
                .map(ServiceResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(serviceResources);
    }
}
