package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.BranchBayCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchBayQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeactivateBranchBayCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchBayByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchBaysByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchBayId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.BranchBayResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateBranchBayResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateBranchBayResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.BranchBayResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.CreateBranchBayCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.UpdateBranchBayCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
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
@RequestMapping(value = "/api/v1/branches/{branchId}/bays", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Branch Bays", description = "Service bay management for a branch")
@PreAuthorize("isAuthenticated()")
public class BranchBaysController {

    private final BranchBayCommandService commandService;
    private final BranchBayQueryService queryService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public BranchBaysController(
            BranchBayCommandService commandService,
            BranchBayQueryService queryService,
            MultiTenancySecurityService multiTenancySecurityService
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @PostMapping
    @Operation(summary = "Create a service bay for a branch")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    public ResponseEntity<BranchBayResource> createBay(
            @PathVariable UUID branchId,
            @Valid @RequestBody CreateBranchBayResource resource
    ) {
        multiTenancySecurityService.validateBranchAccess(branchId);
        var command = CreateBranchBayCommandFromResourceAssembler.toCommand(branchId, resource);
        return commandService.handle(command)
                .map(bay -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(BranchBayResourceFromAggregateAssembler.toResource(bay)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    @Operation(summary = "List branch bays", description = "B2C discovery: any authenticated user may list active bays")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BranchBayResource>> listBays(
            @PathVariable UUID branchId,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly
    ) {
        var query = new GetBranchBaysByBranchIdQuery(new BranchId(branchId), activeOnly);
        var bays = queryService.handle(query).stream()
                .map(BranchBayResourceFromAggregateAssembler::toResource)
                .toList();
        return ResponseEntity.ok(bays);
    }

    @GetMapping("/{bayId}")
    @Operation(summary = "Get a bay by ID")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    public ResponseEntity<BranchBayResource> getBayById(
            @PathVariable UUID branchId,
            @PathVariable UUID bayId
    ) {
        multiTenancySecurityService.validateBranchAccess(branchId);
        var query = new GetBranchBayByIdQuery(new BranchBayId(bayId));
        return queryService.handle(query)
                .filter(bay -> bay.getBranchId().value().equals(branchId))
                .map(bay -> ResponseEntity.ok(BranchBayResourceFromAggregateAssembler.toResource(bay)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{bayId}")
    @Operation(summary = "Update a service bay")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    public ResponseEntity<BranchBayResource> updateBay(
            @PathVariable UUID branchId,
            @PathVariable UUID bayId,
            @Valid @RequestBody UpdateBranchBayResource resource
    ) {
        multiTenancySecurityService.validateBranchAccess(branchId);
        var existing = queryService.handle(new GetBranchBayByIdQuery(new BranchBayId(bayId)));
        if (existing.isEmpty() || !existing.get().getBranchId().value().equals(branchId)) {
            return ResponseEntity.notFound().build();
        }
        var command = UpdateBranchBayCommandFromResourceAssembler.toCommand(bayId, resource);
        return commandService.handle(command)
                .map(bay -> ResponseEntity.ok(BranchBayResourceFromAggregateAssembler.toResource(bay)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{bayId}")
    @Operation(summary = "Deactivate a service bay")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    public ResponseEntity<Void> deactivateBay(@PathVariable UUID branchId, @PathVariable UUID bayId) {
        multiTenancySecurityService.validateBranchAccess(branchId);
        var existing = queryService.handle(new GetBranchBayByIdQuery(new BranchBayId(bayId)));
        if (existing.isEmpty() || !existing.get().getBranchId().value().equals(branchId)) {
            return ResponseEntity.notFound().build();
        }
        boolean deactivated = commandService.handle(new DeactivateBranchBayCommand(new BranchBayId(bayId)));
        return deactivated ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    }
}