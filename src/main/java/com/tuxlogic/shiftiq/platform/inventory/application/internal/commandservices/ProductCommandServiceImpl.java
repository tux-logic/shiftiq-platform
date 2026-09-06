package com.tuxlogic.shiftiq.platform.inventory.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.inventory.application.commandservices.ProductCommandService;
import com.tuxlogic.shiftiq.platform.inventory.domain.exceptions.InsufficientStockException;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.AddBatchToProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.CreateProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.DeleteProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.UpdateProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.entities.ProductBatch;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCommandFailure;
import com.tuxlogic.shiftiq.platform.inventory.domain.repositories.ProductRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProductCommandServiceImpl implements ProductCommandService {

    private static final Logger log = LoggerFactory.getLogger(ProductCommandServiceImpl.class);
    private final ProductRepository productRepository;

    public ProductCommandServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Result<Product, ProductCommandFailure> handle(CreateProductCommand command) {
        if (productRepository.existsByBranchIdAndSku(command.branchId(), command.sku().value())) {
            log.warn("Create product failed: duplicate SKU '{}' for branch '{}'", command.sku().value(), command.branchId());
            return Result.failure(ProductCommandFailure.DUPLICATE_SKU);
        }

        try {
            Product product = new Product(
                    UUID.randomUUID(),
                    command.branchId(),
                    command.category(),
                    command.name(),
                    command.sku(),
                    command.salePrice(),
                    command.description(),
                    command.minimumStock().value()
            );
            product.refreshLowStockAlert();
            Product savedProduct = productRepository.save(product);
            log.info("Created product ID '{}' for branch '{}'", savedProduct.getId(), savedProduct.getBranchId());
            return Result.success(savedProduct);
        } catch (IllegalArgumentException e) {
            return handleInvalidData("Create product", e);
        }
    }

    @Override
    @Transactional
    public Result<ProductBatch, ProductCommandFailure> handle(AddBatchToProductCommand command) {
        var productOpt = findProductOrNotFound(command.productId(), "Add batch");
        if (productOpt.isEmpty()) {
            return Result.failure(ProductCommandFailure.PRODUCT_NOT_FOUND);
        }

        Product product = productOpt.get();
        try {
            var movementBatch = product.applyStockMovement(command.quantity(), command.acquisitionCost());
            var savedProduct = productRepository.save(product);
            log.info("Applied stock movement of {} for product ID '{}'. New stock: {}",
                    command.quantity(), savedProduct.getId(), savedProduct.getCurrentStock().value());

            if (movementBatch.isPresent()) {
                var savedBatch = savedProduct.getBatches().get(savedProduct.getBatches().size() - 1);
                return Result.success(savedBatch);
            }
            return Result.success(ProductBatch.forStockAdjustment(command.quantity().value(), command.acquisitionCost(), savedProduct.getCurrentStock().value()));
        } catch (InsufficientStockException e) {
            log.warn("Add batch failed: insufficient stock for product ID '{}'", command.productId());
            return Result.failure(ProductCommandFailure.INSUFFICIENT_STOCK);
        } catch (IllegalArgumentException e) {
            return handleInvalidData("Add batch", e);
        }
    }

    @Override
    @Transactional
    public Result<Product, ProductCommandFailure> handle(UpdateProductCommand command) {
        var productOpt = findProductOrNotFound(command.productId(), "Update product");
        if (productOpt.isEmpty()) {
            return Result.failure(ProductCommandFailure.PRODUCT_NOT_FOUND);
        }

        Product product = productOpt.get();
        if (productRepository.existsByBranchIdAndSkuAndIdNot(
                product.getBranchId(),
                command.sku().value(),
                command.productId()
        )) {
            log.warn("Update product failed: duplicate SKU '{}' for branch '{}'", command.sku().value(), product.getBranchId());
            return Result.failure(ProductCommandFailure.DUPLICATE_SKU);
        }

        try {
            product.updateDetails(
                    command.name(),
                    command.category(),
                    command.sku(),
                    command.salePrice(),
                    command.description(),
                    command.minimumStock().value()
            );
            Product savedProduct = productRepository.save(product);
            log.info("Updated details for product ID '{}'", savedProduct.getId());
            return Result.success(savedProduct);
        } catch (IllegalArgumentException e) {
            return handleInvalidData("Update product", e);
        }
    }

    @Override
    @Transactional
    public Result<Void, ProductCommandFailure> handle(DeleteProductCommand command) {
        if (!productRepository.existsById(command.productId())) {
            log.warn("Delete product failed: product ID '{}' not found", command.productId());
            return Result.failure(ProductCommandFailure.PRODUCT_NOT_FOUND);
        }

        try {
            productRepository.deleteById(command.productId());
            log.info("Soft-deleted product ID '{}'", command.productId());
            return Result.success(null);
        } catch (DataIntegrityViolationException e) {
            log.warn("Delete product failed: product ID '{}' is in use", command.productId());
            return Result.failure(ProductCommandFailure.PRODUCT_IN_USE);
        }
    }

    private Optional<Product> findProductOrNotFound(UUID productId, String actionName) {
        var productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            log.warn("{} failed: product ID '{}' not found", actionName, productId);
        }
        return productOpt;
    }

    private <T> Result<T, ProductCommandFailure> handleInvalidData(String actionName, IllegalArgumentException e) {
        log.warn("{} failed due to invalid data: {}", actionName, e.getMessage());
        return Result.failure(ProductCommandFailure.INVALID_PRODUCT_DATA);
    }
}
