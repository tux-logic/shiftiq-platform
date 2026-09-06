package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.ProfileQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetProfileRolesByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetProfileByDocumentNumberQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.responses.ProfileSummary;

import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;

@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profiles", description = "Operations related to user profiles")
@PreAuthorize("isAuthenticated()")
public class ProfilesController {

    private final ProfileQueryService profileQueryService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public ProfilesController(ProfileQueryService profileQueryService, MultiTenancySecurityService multiTenancySecurityService) {
        this.profileQueryService = profileQueryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all profile roles for a specific user ID", description = "Returns a list of roles (e.g. OWNER, CUSTOMER, EMPLOYEE) that the user currently has.")
    public ResponseEntity<List<String>> getUserProfileRoles(@RequestParam(name = "userId") UUID userId) {
        multiTenancySecurityService.validateUserAccess(userId);
        var query = new GetProfileRolesByUserIdQuery(new UserId(userId));
        var roles = profileQueryService.handle(query);
        return ResponseEntity.ok(roles);
    }

    @GetMapping(params = "documentNumber")
    @Operation(summary = "Get profile by document number", description = "Searches for a user profile using their DNI/RUC")
    public ResponseEntity<ProfileSummary> getProfileByDocumentNumber(@RequestParam String documentNumber) {
        var query = new GetProfileByDocumentNumberQuery(documentNumber);
        var result = profileQueryService.handle(query);
        result.ifPresent(p -> multiTenancySecurityService.validateUserAccess(p.userId()));
        return result.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
