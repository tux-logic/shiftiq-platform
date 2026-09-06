package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;

import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetOwnerByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetOwnerByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.application.commandservices.OwnerCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.OwnerQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeleteOwnerCommand;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateOwnerResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.OwnerResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateOwnerResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.CreateOwnerCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.OwnerResourceFromEntityAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.UpdateOwnerCommandFromResourceAssembler;
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
@RequestMapping(value = "/api/v1/owners", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Owners", description = "Owner Management Endpoints")
@PreAuthorize("isAuthenticated()")
public class OwnersController {

    private final OwnerCommandService ownerCommandService;
    private final OwnerQueryService ownerQueryService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public OwnersController(OwnerCommandService ownerCommandService, OwnerQueryService ownerQueryService, MultiTenancySecurityService multiTenancySecurityService) {
        this.ownerCommandService = ownerCommandService;
        this.ownerQueryService = ownerQueryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @Operation(summary = "Create a new owner profile", description = "Creates a new owner profile associated with a user ID")
    @PostMapping
    public ResponseEntity<OwnerResource> createOwner(@Valid @RequestBody CreateOwnerResource resource) {
        var command = CreateOwnerCommandFromResourceAssembler.toCommandFromResource(resource);
        var owner = ownerCommandService.handle(command);
        if (owner.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var ownerResource = OwnerResourceFromEntityAssembler.toResourceFromEntity(owner.get());
        return new ResponseEntity<>(ownerResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an owner profile", description = "Updates an existing owner profile")
    @PutMapping("/{ownerId}")
    public ResponseEntity<OwnerResource> updateOwner(@PathVariable UUID ownerId, @Valid @RequestBody UpdateOwnerResource resource) {
        var command = UpdateOwnerCommandFromResourceAssembler.toCommandFromResource(ownerId, resource);
        var owner = ownerCommandService.handle(command);
        if (owner.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var ownerResource = OwnerResourceFromEntityAssembler.toResourceFromEntity(owner.get());
        return ResponseEntity.ok(ownerResource);
    }

    @Operation(summary = "Get an owner profile by ID", description = "Retrieves the details of a specific owner profile")
    @GetMapping("/{ownerId}")
    public ResponseEntity<OwnerResource> getOwnerById(@PathVariable UUID ownerId) {
        var query = new GetOwnerByIdQuery(new OwnerId(ownerId));
        var owner = ownerQueryService.handle(query);
        if (owner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var ownerResource = OwnerResourceFromEntityAssembler.toResourceFromEntity(owner.get());
        return ResponseEntity.ok(ownerResource);
    }

    @Operation(summary = "Get an owner profile by User ID", description = "Retrieves the details of a specific owner profile using the User ID")
    @GetMapping
    public ResponseEntity<OwnerResource> getOwnerByUserId(@RequestParam(name = "userId") UUID userId) {
        var query = new GetOwnerByUserIdQuery(new UserId(userId));
        var owner = ownerQueryService.handle(query);
        if (owner.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var ownerResource = OwnerResourceFromEntityAssembler.toResourceFromEntity(owner.get());
        return ResponseEntity.ok(ownerResource);
    }

    @Operation(summary = "Delete an owner profile", description = "Deletes an existing owner profile")
    @DeleteMapping("/{ownerId}")
    public ResponseEntity<?> deleteOwner(@PathVariable UUID ownerId) {
        var command = new DeleteOwnerCommand(new OwnerId(ownerId));
        ownerCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
