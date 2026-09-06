package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;

import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetAllWorkshopsByOwnerIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopByIdQuery;
import com.tuxlogic.shiftiq.platform.core.application.commandservices.WorkshopCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.WorkshopQueryService;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateWorkshopResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateWorkshopResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.WorkshopResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.CreateWorkshopCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.UpdateWorkshopCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.WorkshopResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;

@RestController
@RequestMapping(value = "/api/v1/workshops", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Workshops", description = "Workshop Management Endpoints")
@PreAuthorize("isAuthenticated()")
public class WorkshopsController {

    private final WorkshopCommandService workshopCommandService;
    private final WorkshopQueryService workshopQueryService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public WorkshopsController(WorkshopCommandService workshopCommandService, WorkshopQueryService workshopQueryService, MultiTenancySecurityService multiTenancySecurityService) {
        this.workshopCommandService = workshopCommandService;
        this.workshopQueryService = workshopQueryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @Operation(summary = "Create a new workshop", description = "Creates a new workshop")
    @PostMapping
    public ResponseEntity<WorkshopResource> createWorkshop(@Valid @RequestBody CreateWorkshopResource resource) {
        var command = CreateWorkshopCommandFromResourceAssembler.toCommandFromResource(resource);
        var workshop = workshopCommandService.handle(command);
        if (workshop.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var workshopResource = WorkshopResourceFromEntityAssembler.toResourceFromEntity(workshop.get());
        return new ResponseEntity<>(workshopResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing workshop", description = "Updates the details of a workshop by its ID")
    @PutMapping("/{workshopId}")
    public ResponseEntity<WorkshopResource> updateWorkshop(@PathVariable UUID workshopId, @Valid @RequestBody UpdateWorkshopResource resource) {
        var existing = workshopQueryService.handle(new GetWorkshopByIdQuery(new WorkshopId(workshopId)));
        existing.ifPresent(w -> multiTenancySecurityService.validateUserAccess(w.getOwnerId().value()));

        var command = UpdateWorkshopCommandFromResourceAssembler.toCommandFromResource(workshopId, resource);
        var workshop = workshopCommandService.handle(command);
        if (workshop.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var workshopResource = WorkshopResourceFromEntityAssembler.toResourceFromEntity(workshop.get());
        return ResponseEntity.ok(workshopResource);
    }

    @Operation(summary = "Get a workshop by ID", description = "Retrieves the details of a specific workshop")
    @GetMapping("/{workshopId}")
    public ResponseEntity<WorkshopResource> getWorkshopById(@PathVariable UUID workshopId) {
        var query = new GetWorkshopByIdQuery(new WorkshopId(workshopId));
        var workshop = workshopQueryService.handle(query);
        if (workshop.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        multiTenancySecurityService.validateUserAccess(workshop.get().getOwnerId().value());
        var workshopResource = WorkshopResourceFromEntityAssembler.toResourceFromEntity(workshop.get());
        return ResponseEntity.ok(workshopResource);
    }

    @Operation(summary = "Get workshops by owner ID", description = "Retrieves all workshops belonging to a specific owner")
    @GetMapping
    public ResponseEntity<List<WorkshopResource>> getWorkshopsByOwnerId(@RequestParam(name = "ownerId") UUID ownerId) {
        multiTenancySecurityService.validateUserAccess(ownerId);
        var query = new GetAllWorkshopsByOwnerIdQuery(new OwnerId(ownerId));
        var workshops = workshopQueryService.handle(query);
        
        var workshopResources = workshops.stream()
                .map(WorkshopResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(workshopResources);
    }
}
