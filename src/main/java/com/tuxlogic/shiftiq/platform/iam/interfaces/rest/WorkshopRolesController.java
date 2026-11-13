package com.tuxlogic.shiftiq.platform.iam.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices.WorkshopRoleCommandServiceImpl;
import com.tuxlogic.shiftiq.platform.iam.application.internal.queryservices.WorkshopRoleQueryServiceImpl;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.DefaultRolePermissions;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Roles;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.*;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/api/v1/workshops/{workshopId}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Workshop Roles", description = "Hybrid system and custom workshop role management endpoints")
@PreAuthorize("isAuthenticated()")
public class WorkshopRolesController {

    private final WorkshopRoleCommandServiceImpl commandService;
    private final WorkshopRoleQueryServiceImpl queryService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public WorkshopRolesController(WorkshopRoleCommandServiceImpl commandService,
                                   WorkshopRoleQueryServiceImpl queryService,
                                   MultiTenancySecurityService multiTenancySecurityService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @GetMapping
    @Operation(summary = "List workshop roles", description = "Lists immutable system role templates and active custom roles for the workshop")
    public ResponseEntity<List<WorkshopRoleResource>> listRoles(@PathVariable UUID workshopId) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.ROLE_MANAGE);
        var roles = queryService.listAvailableForWorkshop(workshopId).stream()
                .map(WorkshopRoleResource::from)
                .toList();
        return ResponseEntity.ok(roles);
    }

    @PostMapping
    @Operation(summary = "Create custom workshop role", description = "Creates a workshop-scoped custom role from the assignable permission catalog")
    public ResponseEntity<WorkshopRoleResource> createRole(@PathVariable UUID workshopId,
                                                           @Valid @RequestBody CreateWorkshopRoleResource resource) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.ROLE_MANAGE);
        AuthenticatedPrincipal actor = requirePrincipal();
        Set<Permission> requested = parsePermissions(resource.permissions());
        var created = commandService.createCustomRole(
                workshopId,
                actor.getId(),
                resolveActorPermissions(actor),
                isAdmin(actor),
                resource.name(),
                resource.description(),
                requested
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(WorkshopRoleResource.from(created));
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update custom workshop role", description = "Updates name, description, and permissions of a workshop-scoped custom role")
    public ResponseEntity<WorkshopRoleResource> updateRole(@PathVariable UUID workshopId,
                                                           @PathVariable UUID roleId,
                                                           @Valid @RequestBody UpdateWorkshopRoleResource resource) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.ROLE_MANAGE);
        AuthenticatedPrincipal actor = requirePrincipal();
        Set<Permission> requested = parsePermissions(resource.permissions());
        var updated = commandService.updateCustomRole(
                workshopId,
                roleId,
                actor.getId(),
                resolveActorPermissions(actor),
                isAdmin(actor),
                resource.name(),
                resource.description(),
                requested
        );
        return ResponseEntity.ok(WorkshopRoleResource.from(updated));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Deactivate custom workshop role", description = "Soft-deactivates a workshop-scoped custom role")
    public ResponseEntity<Void> deactivateRole(@PathVariable UUID workshopId, @PathVariable UUID roleId) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.ROLE_MANAGE);
        AuthenticatedPrincipal actor = requirePrincipal();
        commandService.deactivateCustomRole(workshopId, roleId, actor.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roleId}/assignments")
    @Operation(summary = "Assign role to user", description = "Assigns a system template or workshop custom role to a staff user")
    public ResponseEntity<Void> assignRole(@PathVariable UUID workshopId,
                                           @PathVariable UUID roleId,
                                           @Valid @RequestBody AssignWorkshopRoleResource resource) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.ROLE_MANAGE);
        AuthenticatedPrincipal actor = requirePrincipal();
        commandService.assignRoleToUser(
                workshopId,
                roleId,
                resource.userId(),
                actor.getId(),
                resolveActorPermissions(actor),
                isAdmin(actor)
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "List role audit logs", description = "Retrieves the audit trail of custom role changes and assignments for the workshop")
    public ResponseEntity<List<WorkshopRoleAuditLogResource>> listAuditLogs(@PathVariable UUID workshopId) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.ROLE_MANAGE);
        var logs = queryService.listAuditLogs(workshopId).stream()
                .map(WorkshopRoleAuditLogResource::from)
                .toList();
        return ResponseEntity.ok(logs);
    }

    private static AuthenticatedPrincipal requirePrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
            throw new AccessDeniedException("Authentication required");
        }
        return principal;
    }

    private static boolean isAdmin(AuthenticatedPrincipal actor) {
        return actor.hasRole("ROLE_ADMIN") || actor.hasPermission(Permission.PLATFORM_ADMIN.name());
    }

    private static Set<Permission> resolveActorPermissions(AuthenticatedPrincipal actor) {
        Set<String> explicit = actor.getPermissions();
        EnumSet<Permission> resolved = EnumSet.noneOf(Permission.class);
        if (explicit != null && !explicit.isEmpty()) {
            for (String code : explicit) {
                try {
                    resolved.add(Permission.valueOf(code));
                } catch (IllegalArgumentException ignored) {
                }
            }
            return resolved;
        }
        for (Roles role : Roles.values()) {
            if (actor.hasRole(role.name())) {
                resolved.addAll(DefaultRolePermissions.permissionsFor(role));
            }
        }
        return resolved;
    }

    private static Set<Permission> parsePermissions(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new IllegalArgumentException("iam.error.workshopRole.permissionsRequired");
        }
        EnumSet<Permission> parsed = EnumSet.noneOf(Permission.class);
        for (String code : codes) {
            try {
                parsed.add(Permission.valueOf(code.trim().toUpperCase(Locale.ROOT)));
            } catch (Exception e) {
                throw new IllegalArgumentException("iam.error.workshopRole.invalidPermission");
            }
        }
        return parsed;
    }
}
