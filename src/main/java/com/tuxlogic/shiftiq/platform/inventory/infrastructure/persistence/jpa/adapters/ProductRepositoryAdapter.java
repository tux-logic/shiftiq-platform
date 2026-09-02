package com.tuxlogic.shiftiq.platform.inventory.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.repositories.ProductRepository;
import com.tuxlogic.shiftiq.platform.inventory.infrastructure.persistence.jpa.assemblers.ProductEntityAssembler;
import com.tuxlogic.shiftiq.platform.inventory.infrastructure.persistence.jpa.entities.ProductJpaEntity;
import com.tuxlogic.shiftiq.platform.inventory.infrastructure.persistence.jpa.repositories.ProductJpaRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing the domain ProductRepository interface.
 * Translates domain calls to JPA operations using the ProductEntityAssembler.
 * @author Adiel Sanchez
 */
@Repository
public class ProductRepositoryAdapter implements ProductRepository {
    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity;
        if (product.getVersion() != null) {
            entity = jpaRepository.findById(product.getId()).orElse(new ProductJpaEntity());
        } else {
            entity = new ProductJpaEntity();
        }
        ProductEntityAssembler.toEntity(product, entity);
        var savedEntity = jpaRepository.save(entity);
        return ProductEntityAssembler.toAggregate(savedEntity);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id).map(ProductEntityAssembler::toAggregate);
    }

    @Override
    public List<Product> findAllByBranchId(BranchId branchId) {
        return jpaRepository.findAllByBranchId(branchId.value())
                .stream()
                .map(ProductEntityAssembler::toAggregate)
                .toList();
    }

    @Override
    public List<Product> findAllByBranchIdWithFilters(BranchId branchId, String name, String category, Boolean lowStockOnly) {
        boolean filterLowStock = Boolean.TRUE.equals(lowStockOnly);
        return jpaRepository.searchByBranchId(branchId.value(), name, category, filterLowStock)
                .stream()
                .map(ProductEntityAssembler::toAggregate)
                .toList();
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
                .map(ProductEntityAssembler::toAggregate)
                .toList();
    }

    @Override
    public boolean existsByBranchIdAndSku(BranchId branchId, String sku) {
        return jpaRepository.existsByBranchIdAndSku(branchId.value(), sku);
    }

    @Override
    public boolean existsByBranchIdAndSkuAndIdNot(BranchId branchId, String sku, UUID productId) {
        return jpaRepository.existsByBranchIdAndSkuAndIdNot(branchId.value(), sku, productId);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
