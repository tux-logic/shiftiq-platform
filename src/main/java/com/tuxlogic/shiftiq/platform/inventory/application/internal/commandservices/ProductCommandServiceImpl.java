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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductCommandServiceImpl implements ProductCommandService {
    private final ProductRepository productRepository;

    public ProductCommandServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Result<Product, ProductCommandFailure> handle(CreateProductCommand command) {
        if (productRepository.existsByBranchIdAndSku(command.branchId(), command.sku().value())) {
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
            return Result.success(productRepository.save(product));
        } catch (IllegalArgumentException e) {
            return Result.failure(ProductCommandFailure.INVALID_PRODUCT_DATA);
        }
    }

    @Override
    @Transactional
    public Result<ProductBatch, ProductCommandFailure> handle(AddBatchToProductCommand command) {
        var product = productRepository.findById(command.productId());
        if (product.isEmpty()) {
            return Result.failure(ProductCommandFailure.PRODUCT_NOT_FOUND);
        }

        try {
            var movementBatch = product.get().applyStockMovement(command.quantity(), command.acquisitionCost());
            var savedProduct = productRepository.save(product.get());
            if (movementBatch.isPresent()) {
                var savedBatch = savedProduct.getBatches().get(savedProduct.getBatches().size() - 1);
                return Result.success(savedBatch);
            }
            return Result.success(ProductBatch.forStockAdjustment(command.quantity(), command.acquisitionCost(), savedProduct.getCurrentStock().value()));
        } catch (InsufficientStockException e) {
            return Result.failure(ProductCommandFailure.INSUFFICIENT_STOCK);
        } catch (IllegalArgumentException e) {
            return Result.failure(ProductCommandFailure.INVALID_PRODUCT_DATA);
        }
    }

    @Override
    @Transactional
    public Result<Product, ProductCommandFailure> handle(UpdateProductCommand command) {
        var product = productRepository.findById(command.productId());
        if (product.isEmpty()) {
            return Result.failure(ProductCommandFailure.PRODUCT_NOT_FOUND);
        }

        if (productRepository.existsByBranchIdAndSkuAndIdNot(
                product.get().getBranchId(),
                command.sku().value(),
                command.productId()
        )) {
            return Result.failure(ProductCommandFailure.DUPLICATE_SKU);
        }

        try {
            product.get().updateDetails(
                    command.name(),
                    command.category(),
                    command.sku(),
                    command.salePrice(),
                    command.description(),
                    command.minimumStock().value()
            );
            return Result.success(productRepository.save(product.get()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ProductCommandFailure.INVALID_PRODUCT_DATA);
        }
    }

    @Override
    @Transactional
    public Result<Void, ProductCommandFailure> handle(DeleteProductCommand command) {
        if (!productRepository.existsById(command.productId())) {
            return Result.failure(ProductCommandFailure.PRODUCT_NOT_FOUND);
        }

        try {
            productRepository.deleteById(command.productId());
            return Result.success(null);
        } catch (DataIntegrityViolationException e) {
            return Result.failure(ProductCommandFailure.PRODUCT_IN_USE);
        }
    }
}
