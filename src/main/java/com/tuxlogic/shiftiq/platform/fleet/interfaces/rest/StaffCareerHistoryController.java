package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest;

import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.StaffCareerHistoryQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetStaffCareerHistoryByEmployeeAndBranchQuery;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.StaffCareerSnapshotResource;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform.StaffCareerSnapshotResourceFromEntityAssembler;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/branches/{branchId}/employees/{employeeId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "StaffCareerHistory", description = "Staff Career and Assignment History Endpoints")
@PreAuthorize("isAuthenticated()")
public class StaffCareerHistoryController {

    private final StaffCareerHistoryQueryService queryService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public StaffCareerHistoryController(StaffCareerHistoryQueryService queryService,
                                        MultiTenancySecurityService multiTenancySecurityService) {
        this.queryService = queryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @GetMapping
    @Operation(summary = "Get employee career history in branch", description = "Retrieves the chronological list of career snapshots, specialty adjustments, and salary revisions for a staff member in a branch")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    public ResponseEntity<List<StaffCareerSnapshotResource>> getStaffCareerHistory(
            @PathVariable UUID branchId,
            @PathVariable UUID employeeId) {
        multiTenancySecurityService.validateBranchAccess(branchId);
        var query = new GetStaffCareerHistoryByEmployeeAndBranchQuery(new BranchId(branchId), employeeId);
        var snapshots = queryService.handle(query);
        var resources = snapshots.stream()
                .map(StaffCareerSnapshotResourceFromEntityAssembler::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
