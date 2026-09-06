package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;

import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetCustomerByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetCustomerByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeleteCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.application.commandservices.CustomerCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.CustomerQueryService;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateCustomerResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CustomerResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateCustomerResource;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.CreateCustomerCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.CustomerResourceFromEntityAssembler;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform.UpdateCustomerCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/customers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Customers", description = "Customer Management Endpoints")
@PreAuthorize("isAuthenticated()")
public class CustomersController {

    private final CustomerCommandService customerCommandService;
    private final CustomerQueryService customerQueryService;

    public CustomersController(CustomerCommandService customerCommandService, CustomerQueryService customerQueryService) {
        this.customerCommandService = customerCommandService;
        this.customerQueryService = customerQueryService;
    }

    @Operation(summary = "Create a new customer profile", description = "Creates a new customer profile associated with a user ID")
    @PostMapping
    public ResponseEntity<CustomerResource> createCustomer(@Valid @RequestBody CreateCustomerResource resource) {
        var command = CreateCustomerCommandFromResourceAssembler.toCommandFromResource(resource);
        var customer = customerCommandService.handle(command);
        if (customer.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var customerResource = CustomerResourceFromEntityAssembler.toResourceFromEntity(customer.get());
        return new ResponseEntity<>(customerResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a customer profile", description = "Updates an existing customer profile")
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResource> updateCustomer(@PathVariable UUID customerId, @Valid @RequestBody UpdateCustomerResource resource) {
        var command = UpdateCustomerCommandFromResourceAssembler.toCommandFromResource(customerId, resource);
        var customer = customerCommandService.handle(command);
        if (customer.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var customerResource = CustomerResourceFromEntityAssembler.toResourceFromEntity(customer.get());
        return ResponseEntity.ok(customerResource);
    }

    @Operation(summary = "Get a customer profile by ID", description = "Retrieves the details of a specific customer profile")
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResource> getCustomerById(@PathVariable UUID customerId) {
        var query = new GetCustomerByIdQuery(new CustomerId(customerId));
        var customer = customerQueryService.handle(query);
        if (customer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var customerResource = CustomerResourceFromEntityAssembler.toResourceFromEntity(customer.get());
        return ResponseEntity.ok(customerResource);
    }

    @Operation(summary = "Get a customer profile by User ID", description = "Retrieves the details of a specific customer profile using the User ID")
    @GetMapping
    public ResponseEntity<CustomerResource> getCustomerByUserId(@RequestParam(name = "userId") UUID userId) {
        var query = new GetCustomerByUserIdQuery(new UserId(userId));
        var customer = customerQueryService.handle(query);
        if (customer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var customerResource = CustomerResourceFromEntityAssembler.toResourceFromEntity(customer.get());
        return ResponseEntity.ok(customerResource);
    }

    @Operation(summary = "Delete a customer profile", description = "Deletes an existing customer profile")
    @DeleteMapping("/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable UUID customerId) {
        var command = new DeleteCustomerCommand(new CustomerId(customerId));
        customerCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}

