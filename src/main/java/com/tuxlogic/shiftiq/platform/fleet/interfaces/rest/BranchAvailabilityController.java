package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest;

import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.BranchAvailabilityQueryService;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.BranchAvailabilityResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/branches/{branchId}/availability", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Branch Availability", description = "Bay slot availability for B2C booking")
@PreAuthorize("isAuthenticated()")
public class BranchAvailabilityController {

    private final BranchAvailabilityQueryService availabilityQueryService;

    public BranchAvailabilityController(BranchAvailabilityQueryService availabilityQueryService) {
        this.availabilityQueryService = availabilityQueryService;
    }

    @GetMapping
    @Operation(summary = "Get bay availability for a date")
    public ResponseEntity<BranchAvailabilityResource> getAvailability(
            @PathVariable UUID branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(availabilityQueryService.getAvailability(branchId, date));
    }
}