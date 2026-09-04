package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest;

import com.tuxlogic.shiftiq.platform.inventory.application.commandservices.ProductCommandService;
import com.tuxlogic.shiftiq.platform.inventory.application.queryservices.ProductQueryService;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.DeleteProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.queries.GetProductByIdQuery;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.queries.GetProductsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCommandFailure;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.AddBatchToProductResource;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.CreateProductResource;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.ProductBatchResource;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.ProductDetailsResource;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.ProductResource;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.UpdateProductResource;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform.AddBatchToProductCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform.CreateProductCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform.ProductBatchResourceFromEntityAssembler;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform.ProductDetailsResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform.ProductResourceFromAggregateAssembler;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.transform.UpdateProductCommandFromResourceAssembler;
import com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/inventory/products", produces = "application/json")
@Tag(name = "Inventory Products", description = "Endpoints for managing products in the inventory")
@PreAuthorize("isAuthenticated()")
public class ProductsController {
    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;
    private final MessageSource messageSource;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public ProductsController(ProductCommandService productCommandService,
                              ProductQueryService productQueryService,
                              MessageSource messageSource,
                              MultiTenancySecurityService multiTenancySecurityService) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
        this.messageSource = messageSource;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @PostMapping
    @Operation(summary = "Create a new Product", description = "Creates a new product in the inventory for a specific branch")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId())")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductResource resource) {
        var command = CreateProductCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = productCommandService.handle(command);
        if (result.isSuccess()) {
            var productResource = ProductResourceFromAggregateAssembler.toResourceFromAggregate(result.success().get());
            return new ResponseEntity<>(productResource, HttpStatus.CREATED);
        }
        return toErrorResponse(result.failure().get());
    }

    @GetMapping({"", "/branch/{branchId}"})
    @Operation(summary = "Get products by branch", description = "Retrieves products for a branch with optional name, category and low-stock filters")
    public ResponseEntity<?> getProductsByBranch(
            @PathVariable(required = false) UUID branchId,
            @RequestParam(required = false) UUID branchIdQuery,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStockOnly) {
        UUID resolvedBranchId = branchId != null ? branchId : branchIdQuery;
        if (resolvedBranchId == null) {
            String message = messageSource.getMessage("inventory.error.query.branchId.required", null, LocaleContextHolder.getLocale());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError("product", message));
        }
        multiTenancySecurityService.validateBranchAccess(resolvedBranchId);
        return ResponseEntity.ok(toProductResources(resolvedBranchId, name, category, lowStockOnly));
    }

    @PostMapping("/{productId}/batches")
    @Operation(summary = "Add or adjust product stock", description = "Registers stock entries (positive quantity) or warehouse adjustments (negative quantity)")
    public ResponseEntity<?> addBatchToProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody AddBatchToProductResource resource) {
        validateProductBranchAccess(productId);

        if (resource.quantity() == 0) {
            String message = messageSource.getMessage("inventory.error.resource.quantity.nonZero", null, LocaleContextHolder.getLocale());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError("productBatch", message));
        }

        var command = AddBatchToProductCommandFromResourceAssembler.toCommandFromResource(productId, resource);
        var result = productCommandService.handle(command);
        if (result.isSuccess()) {
            ProductBatchResource batchResource = resource.quantity() < 0
                    ? ProductBatchResourceFromEntityAssembler.toResourceFromStockAdjustment(
                            resource.quantity(), resource.acquisitionCost(), result.success().get())
                    : ProductBatchResourceFromEntityAssembler.toResourceFromEntity(result.success().get());
            return new ResponseEntity<>(batchResource, HttpStatus.CREATED);
        }
        return toErrorResponse(result.failure().get());
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product details by ID", description = "Retrieves all details for a product including its associated batches")
    public ResponseEntity<ProductDetailsResource> getProductById(@PathVariable UUID productId) {
        var query = new GetProductByIdQuery(productId);
        var product = productQueryService.handle(query);

        return product.map(p -> ResponseEntity.ok(
                authorizeAndMapProductDetails(p)
        )).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update product details", description = "Updates the basic details of a product (Name, Category, SKU)")
    public ResponseEntity<?> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductResource resource) {
        validateProductBranchAccess(productId);

        var command = UpdateProductCommandFromResourceAssembler.toCommandFromResource(productId, resource);
        var result = productCommandService.handle(command);
        if (result.isSuccess()) {
            var productResource = ProductResourceFromAggregateAssembler.toResourceFromAggregate(result.success().get());
            return ResponseEntity.ok(productResource);
        }
        return toErrorResponse(result.failure().get());
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete a product", description = "Deletes a product and all its associated batches")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID productId) {
        validateProductBranchAccess(productId);
        var command = new DeleteProductCommand(productId);
        var result = productCommandService.handle(command);
        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        }
        return toErrorResponse(result.failure().get());
    }

    private List<ProductResource> toProductResources(UUID branchId, String name, String category, Boolean lowStockOnly) {
        var query = new GetProductsByBranchIdQuery(new BranchId(branchId), name, category, lowStockOnly);
        return productQueryService.handle(query).stream()
                .map(ProductResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();
    }

    private ProductDetailsResource authorizeAndMapProductDetails(com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product product) {
        multiTenancySecurityService.validateBranchAccess(product.getBranchId().value());
        return ProductDetailsResourceFromAggregateAssembler.toResourceFromAggregate(product);
    }

    private void validateProductBranchAccess(UUID productId) {
        var product = productQueryService.handle(new GetProductByIdQuery(productId));
        product.ifPresent(value -> multiTenancySecurityService.validateBranchAccess(value.getBranchId().value()));
    }

    private ResponseEntity<?> toErrorResponse(ProductCommandFailure failure) {
        return switch (failure) {
            case PRODUCT_NOT_FOUND -> {
                String message = messageSource.getMessage("inventory.error.product.notFound", null, LocaleContextHolder.getLocale());
                yield ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.notFound("product", message));
            }
            case INVALID_PRODUCT_DATA -> {
                String message = messageSource.getMessage("inventory.error.product.invalidData", null, LocaleContextHolder.getLocale());
                yield ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError("product", message));
            }
            case DUPLICATE_SKU -> {
                String message = messageSource.getMessage("inventory.error.product.duplicateSku", null, LocaleContextHolder.getLocale());
                yield ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.conflict("product", message));
            }
            case PRODUCT_IN_USE -> {
                String message = messageSource.getMessage("inventory.error.product.inUse", null, LocaleContextHolder.getLocale());
                yield ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.conflict("product", message));
            }
            case INSUFFICIENT_STOCK -> {
                String message = messageSource.getMessage("inventory.error.product.insufficientStock", null, LocaleContextHolder.getLocale());
                yield ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError("productBatch", message));
            }
        };
    }
}
