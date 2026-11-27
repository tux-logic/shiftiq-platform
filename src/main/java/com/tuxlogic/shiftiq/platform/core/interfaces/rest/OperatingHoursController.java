package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.application.services.OperatingHoursService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
@Tag(name = "Operating Hours", description = "Workshop global and branch-level operating hours & holiday exceptions")
@PreAuthorize("isAuthenticated()")
public class OperatingHoursController {

    public record UpdateOperatingHoursRequest(
            @NotEmpty List<OperatingHoursService.WeeklyHourInput> days
    ) {}

    private final OperatingHoursService operatingHoursService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public OperatingHoursController(OperatingHoursService operatingHoursService,
                                    MultiTenancySecurityService multiTenancySecurityService) {
        this.operatingHoursService = operatingHoursService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @GetMapping("/workshops/{workshopId}/operating-hours")
    @Operation(summary = "Get workshop global weekly operating hours")
    public ResponseEntity<List<OperatingHoursService.WeeklyHourDto>> getWorkshopHours(@PathVariable UUID workshopId) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.BRANCH_MANAGE);
        return ResponseEntity.ok(operatingHoursService.getWorkshopWeeklySchedule(workshopId));
    }

    @PutMapping("/workshops/{workshopId}/operating-hours")
    @Operation(summary = "Update workshop global weekly operating hours")
    public ResponseEntity<List<OperatingHoursService.WeeklyHourDto>> updateWorkshopHours(
            @PathVariable UUID workshopId,
            @Valid @RequestBody UpdateOperatingHoursRequest request) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.BRANCH_MANAGE);
        return ResponseEntity.ok(operatingHoursService.upsertWorkshopWeeklySchedule(workshopId, request.days()));
    }

    @GetMapping("/workshops/{workshopId}/operating-hours/exceptions")
    @Operation(summary = "List workshop global holiday/date exceptions")
    public ResponseEntity<List<OperatingHoursService.ExceptionDto>> getWorkshopExceptions(@PathVariable UUID workshopId) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.BRANCH_MANAGE);
        return ResponseEntity.ok(operatingHoursService.getWorkshopExceptions(workshopId));
    }

    @PostMapping("/workshops/{workshopId}/operating-hours/exceptions")
    @Operation(summary = "Create or update a workshop global holiday/date exception")
    public ResponseEntity<OperatingHoursService.ExceptionDto> upsertWorkshopException(
            @PathVariable UUID workshopId,
            @Valid @RequestBody OperatingHoursService.ExceptionInput request) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.BRANCH_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(operatingHoursService.upsertWorkshopException(workshopId, request));
    }

    @DeleteMapping("/workshops/{workshopId}/operating-hours/exceptions/{exceptionId}")
    @Operation(summary = "Delete a workshop global holiday/date exception")
    public ResponseEntity<Void> deleteWorkshopException(
            @PathVariable UUID workshopId,
            @PathVariable UUID exceptionId) {
        multiTenancySecurityService.validateWorkshopPermission(workshopId, Permission.BRANCH_MANAGE);
        operatingHoursService.deleteWorkshopException(workshopId, exceptionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/branches/{branchId}/operating-hours")
    @Operation(summary = "Get effective weekly operating hours for a branch (branch override or workshop inherited)")
    public ResponseEntity<List<OperatingHoursService.WeeklyHourDto>> getBranchEffectiveHours(@PathVariable UUID branchId) {
        return ResponseEntity.ok(operatingHoursService.getBranchEffectiveWeeklySchedule(branchId));
    }

    @PutMapping("/branches/{branchId}/operating-hours")
    @Operation(summary = "Set branch-specific operating hours overrides")
    public ResponseEntity<List<OperatingHoursService.WeeklyHourDto>> updateBranchHours(
            @PathVariable UUID branchId,
            @Valid @RequestBody UpdateOperatingHoursRequest request) {
        multiTenancySecurityService.validateBranchPermission(branchId, Permission.BRANCH_MANAGE);
        return ResponseEntity.ok(operatingHoursService.upsertBranchWeeklySchedule(branchId, request.days()));
    }

    @DeleteMapping("/branches/{branchId}/operating-hours")
    @Operation(summary = "Reset branch operating hours overrides to inherit workshop global schedule")
    public ResponseEntity<Void> resetBranchHours(@PathVariable UUID branchId) {
        multiTenancySecurityService.validateBranchPermission(branchId, Permission.BRANCH_MANAGE);
        operatingHoursService.resetBranchWeeklySchedule(branchId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/branches/{branchId}/operating-hours/exceptions")
    @Operation(summary = "List branch-specific holiday/date exceptions")
    public ResponseEntity<List<OperatingHoursService.ExceptionDto>> getBranchExceptions(@PathVariable UUID branchId) {
        return ResponseEntity.ok(operatingHoursService.getBranchExceptions(branchId));
    }

    @PostMapping("/branches/{branchId}/operating-hours/exceptions")
    @Operation(summary = "Create or update a branch-specific holiday/date exception")
    public ResponseEntity<OperatingHoursService.ExceptionDto> upsertBranchException(
            @PathVariable UUID branchId,
            @Valid @RequestBody OperatingHoursService.ExceptionInput request) {
        multiTenancySecurityService.validateBranchPermission(branchId, Permission.BRANCH_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(operatingHoursService.upsertBranchException(branchId, request));
    }

    @DeleteMapping("/branches/{branchId}/operating-hours/exceptions/{exceptionId}")
    @Operation(summary = "Delete a branch-specific holiday/date exception")
    public ResponseEntity<Void> deleteBranchException(
            @PathVariable UUID branchId,
            @PathVariable UUID exceptionId) {
        multiTenancySecurityService.validateBranchPermission(branchId, Permission.BRANCH_MANAGE);
        operatingHoursService.deleteBranchException(branchId, exceptionId);
        return ResponseEntity.noContent().build();
    }
}
