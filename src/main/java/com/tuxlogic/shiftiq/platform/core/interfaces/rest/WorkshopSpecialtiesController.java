package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.WorkshopSpecialtyCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.WorkshopSpecialtyQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeactivateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtiesByWorkshopIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtyByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateWorkshopSpecialtyResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateWorkshopSpecialtyResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.WorkshopSpecialtyResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.CreateWorkshopSpecialtyCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.UpdateWorkshopSpecialtyCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.WorkshopSpecialtyResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
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

@RestController
@RequestMapping(value = "/api/v1/workshops/{workshopId}/specialties", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "WorkshopSpecialties", description = "Workshop Specialties Management Endpoints")
@PreAuthorize("isAuthenticated()")
public class WorkshopSpecialtiesController {

    private final WorkshopSpecialtyCommandService commandService;
    private final WorkshopSpecialtyQueryService queryService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public WorkshopSpecialtiesController(WorkshopSpecialtyCommandService commandService,
                                         WorkshopSpecialtyQueryService queryService,
                                         MultiTenancySecurityService multiTenancySecurityService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @PostMapping
    @Operation(summary = "Create a new specialty for a workshop", description = "Adds a customizable technician specialty to the workshop catalog")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    public ResponseEntity<WorkshopSpecialtyResource> createSpecialty(
            @PathVariable UUID workshopId,
            @Valid @RequestBody CreateWorkshopSpecialtyResource resource) {
        multiTenancySecurityService.validateWorkshopAccess(workshopId);
        var command = CreateWorkshopSpecialtyCommandFromResourceAssembler.toCommand(workshopId, resource);
        var result = commandService.handle(command);
        return result.map(specialty -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(WorkshopSpecialtyResourceFromAggregateAssembler.toResource(specialty)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    @Operation(summary = "List workshop specialties", description = "Retrieves all specialties for a workshop, optionally filtering for active only")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    public ResponseEntity<List<WorkshopSpecialtyResource>> getSpecialties(
            @PathVariable UUID workshopId,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        multiTenancySecurityService.validateWorkshopAccess(workshopId);
        var query = new GetWorkshopSpecialtiesByWorkshopIdQuery(new WorkshopId(workshopId), activeOnly);
        var specialties = queryService.handle(query).stream()
                .map(WorkshopSpecialtyResourceFromAggregateAssembler::toResource)
                .toList();
        return ResponseEntity.ok(specialties);
    }

    @GetMapping("/{specialtyId}")
    @Operation(summary = "Get a specialty by ID", description = "Retrieves details of a specific workshop specialty")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    public ResponseEntity<WorkshopSpecialtyResource> getSpecialtyById(
            @PathVariable UUID workshopId,
            @PathVariable UUID specialtyId) {
        multiTenancySecurityService.validateWorkshopAccess(workshopId);
        var query = new GetWorkshopSpecialtyByIdQuery(new WorkshopSpecialtyId(specialtyId));
        return queryService.handle(query)
                .filter(s -> s.getWorkshopId().value().equals(workshopId))
                .map(specialty -> ResponseEntity.ok(WorkshopSpecialtyResourceFromAggregateAssembler.toResource(specialty)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{specialtyId}")
    @Operation(summary = "Update a workshop specialty", description = "Updates the display name and description of an existing specialty")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    public ResponseEntity<WorkshopSpecialtyResource> updateSpecialty(
            @PathVariable UUID workshopId,
            @PathVariable UUID specialtyId,
            @Valid @RequestBody UpdateWorkshopSpecialtyResource resource) {
        multiTenancySecurityService.validateWorkshopAccess(workshopId);
        var existing = queryService.handle(new GetWorkshopSpecialtyByIdQuery(new WorkshopSpecialtyId(specialtyId)));
        if (existing.isEmpty() || !existing.get().getWorkshopId().value().equals(workshopId)) {
            return ResponseEntity.notFound().build();
        }

        var command = UpdateWorkshopSpecialtyCommandFromResourceAssembler.toCommand(specialtyId, resource);
        return commandService.handle(command)
                .map(specialty -> ResponseEntity.ok(WorkshopSpecialtyResourceFromAggregateAssembler.toResource(specialty)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{specialtyId}")
    @Operation(summary = "Deactivate a workshop specialty", description = "Deactivates a specialty so it cannot be assigned to new staff")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    public ResponseEntity<Void> deactivateSpecialty(
            @PathVariable UUID workshopId,
            @PathVariable UUID specialtyId) {
        multiTenancySecurityService.validateWorkshopAccess(workshopId);
        var existing = queryService.handle(new GetWorkshopSpecialtyByIdQuery(new WorkshopSpecialtyId(specialtyId)));
        if (existing.isEmpty() || !existing.get().getWorkshopId().value().equals(workshopId)) {
            return ResponseEntity.notFound().build();
        }

        boolean deactivated = commandService.handle(new DeactivateWorkshopSpecialtyCommand(new WorkshopSpecialtyId(specialtyId)));
        return deactivated ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    }
}
