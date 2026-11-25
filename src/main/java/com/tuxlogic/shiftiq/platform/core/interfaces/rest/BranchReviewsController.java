package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.BranchReviewCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchReviewQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchReviewsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.BranchReviewResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateBranchReviewResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.BranchReviewResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.CreateBranchReviewCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
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
@RequestMapping(value = "/api/v1/branches/{branchId}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Branch Reviews", description = "Customer reviews for workshop branches")
@PreAuthorize("isAuthenticated()")
public class BranchReviewsController {

    private final BranchReviewCommandService commandService;
    private final BranchReviewQueryService queryService;

    public BranchReviewsController(
            BranchReviewCommandService commandService,
            BranchReviewQueryService queryService
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(summary = "Create a branch review")
    public ResponseEntity<BranchReviewResource> createReview(
            @PathVariable UUID branchId,
            @Valid @RequestBody CreateBranchReviewResource resource
    ) {
        try {
            var command = CreateBranchReviewCommandFromResourceAssembler.toCommand(branchId, resource);
            return commandService.handle(command)
                    .map(review -> ResponseEntity.status(HttpStatus.CREATED)
                            .body(BranchReviewResourceFromAggregateAssembler.toResource(review)))
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "List branch reviews")
    public ResponseEntity<List<BranchReviewResource>> listReviews(@PathVariable UUID branchId) {
        var query = new GetBranchReviewsByBranchIdQuery(new BranchId(branchId));
        var reviews = queryService.handle(query).stream()
                .map(BranchReviewResourceFromAggregateAssembler::toResource)
                .toList();
        return ResponseEntity.ok(reviews);
    }
}