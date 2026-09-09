package com.tuxlogic.shiftiq.platform.iot.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iot.application.queryservices.VehicleQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetActiveVehiclesByCustomerIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.services.CustomerDirectoryPort;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.VehicleResource;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform.VehicleResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.UserSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for exposing customer-specific vehicles operations.
 */
@RestController
@RequestMapping(value = "/api/v1/customers", produces = "application/json")
@Tag(name = "Customers", description = "Endpoints for managing client integrations with other contexts")
@PreAuthorize("isAuthenticated()")
public class CustomerVehiclesController {

    private final VehicleQueryService vehicleQueryService;
    private final CustomerDirectoryPort customerDirectoryPort;
    private final UserSecurityService userSecurityService;

    public CustomerVehiclesController(VehicleQueryService vehicleQueryService, CustomerDirectoryPort customerDirectoryPort, UserSecurityService userSecurityService) {
        this.vehicleQueryService = vehicleQueryService;
        this.customerDirectoryPort = customerDirectoryPort;
        this.userSecurityService = userSecurityService;
    }

    @GetMapping("/{customerId}/vehicles")
    @Operation(summary = "Get active vehicles for customer", description = "Retrieves all vehicles currently associated with an active registration for the customer")
    public ResponseEntity<List<VehicleResource>> getActiveVehiclesByCustomerId(@PathVariable UUID customerId) {
        var userIdOpt = customerDirectoryPort.findUserIdByCustomerId(customerId);
        if (userIdOpt.isEmpty() || !userSecurityService.isCurrentUser(userIdOpt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var query = new GetActiveVehiclesByCustomerIdQuery(new CustomerId(customerId));
        var list = vehicleQueryService.handle(query);
        var resources = list.stream()
                .map(VehicleResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();
        return ResponseEntity.ok(resources);
    }
}