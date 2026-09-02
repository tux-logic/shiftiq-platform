package com.tuxlogic.shiftiq.platform.inventory.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.CreateProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.DeleteProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.InventoryQuantity;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCategory;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCommandFailure;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductName;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.Sku;
import com.tuxlogic.shiftiq.platform.inventory.domain.repositories.ProductRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductCommandServiceImpl productCommandService;

    private BranchId branchId;
    private CreateProductCommand createProductCommand;

    @BeforeEach
    void setUp() {
        branchId = new BranchId(UUID.randomUUID());
        createProductCommand = new CreateProductCommand(
                branchId,
                new ProductCategory("PART"),
                new ProductName("Filtro de aceite"),
                new Sku("FLT-001"),
                "Descripcion",
                new Money(new BigDecimal("45.50")),
                new InventoryQuantity(5)
        );
    }

    @Test
    void createProduct_WhenSkuDoesNotExist_ShouldCreateSuccessfully() {
        when(productRepository.existsByBranchIdAndSku(branchId, "FLT-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Product, ProductCommandFailure> result = productCommandService.handle(createProductCommand);

        assertTrue(result.isSuccess());
        assertNotNull(result.success().get());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_WhenSkuAlreadyExists_ShouldReturnDuplicateSkuFailure() {
        when(productRepository.existsByBranchIdAndSku(branchId, "FLT-001")).thenReturn(true);

        Result<Product, ProductCommandFailure> result = productCommandService.handle(createProductCommand);

        assertTrue(result.isFailure());
        assertEquals(ProductCommandFailure.DUPLICATE_SKU, result.failure().get());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WhenProductDoesNotExist_ShouldReturnNotFoundFailure() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(false);

        Result<Void, ProductCommandFailure> result = productCommandService.handle(new DeleteProductCommand(productId));

        assertTrue(result.isFailure());
        assertEquals(ProductCommandFailure.PRODUCT_NOT_FOUND, result.failure().get());
        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    void deleteProduct_WhenProductIsInUse_ShouldReturnProductInUseFailure() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("FK violation")).when(productRepository).deleteById(productId);

        Result<Void, ProductCommandFailure> result = productCommandService.handle(new DeleteProductCommand(productId));

        assertTrue(result.isFailure());
        assertEquals(ProductCommandFailure.PRODUCT_IN_USE, result.failure().get());
    }

    @Test
    void addBatch_WhenNegativeQuantityExceedsStock_ShouldReturnInsufficientStockFailure() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(
                productId,
                branchId,
                new ProductCategory("PART"),
                new ProductName("Filtro"),
                new Sku("FLT-002"),
                new Money(new BigDecimal("10.00")),
                "Desc",
                2
        );
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        var command = new com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.AddBatchToProductCommand(
                productId,
                -5,
                new Money(new BigDecimal("1.00"))
        );

        Result<?, ProductCommandFailure> result = productCommandService.handle(command);

        assertTrue(result.isFailure());
        assertEquals(ProductCommandFailure.INSUFFICIENT_STOCK, result.failure().get());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_WhenInitialStockIsBelowMinimum_ShouldFlagLowStockAlert() {
        when(productRepository.existsByBranchIdAndSku(branchId, "FLT-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Product, ProductCommandFailure> result = productCommandService.handle(createProductCommand);

        assertTrue(result.isSuccess());
        assertTrue(result.success().get().isLowStockAlert());
    }
}
