package com.tuxlogic.shiftiq.platform.iam.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iam.application.internal.queryservices.WorkshopRoleQueryServiceImpl;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.PermissionResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Permissions", description = "Assignable workshop permission catalog endpoints")
@PreAuthorize("isAuthenticated()")
public class PermissionsController {

    private final WorkshopRoleQueryServiceImpl queryService;

    public PermissionsController(WorkshopRoleQueryServiceImpl queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "List assignable permissions", description = "Returns the backend-owned catalog of permissions assignable to workshop roles")
    public ResponseEntity<List<PermissionResource>> listAssignablePermissions() {
        var resources = queryService.listAssignablePermissions().stream()
                .map(PermissionResource::from)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
