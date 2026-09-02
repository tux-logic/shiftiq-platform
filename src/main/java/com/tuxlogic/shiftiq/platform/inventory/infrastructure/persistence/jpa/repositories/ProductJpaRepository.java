package com.tuxlogic.shiftiq.platform.inventory.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.inventory.infrastructure.persistence.jpa.entities.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the ProductJpaEntity.
 * Provides derived query methods that translate to SQL automatically.
 * @author Adiel Sanchez
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    /**
     * Finds all products whose branchId column matches the given string.
     * Note: branchId is stored as a String (UUID.toString()) in the database.
     * @param branchId the branch UUID
     * @return list of matching product entities
     */
    List<ProductJpaEntity> findAllByBranchId(UUID branchId);

    @Query("""
            SELECT p FROM ProductJpaEntity p
            WHERE p.branchId = :branchId
            AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:category IS NULL OR UPPER(p.category) = UPPER(:category))
            AND (:lowStockOnly = false OR p.currentStock <= p.minimumStock)
            """)
    List<ProductJpaEntity> searchByBranchId(
            @Param("branchId") UUID branchId,
            @Param("name") String name,
            @Param("category") String category,
            @Param("lowStockOnly") boolean lowStockOnly
    );

    boolean existsByBranchIdAndSku(UUID branchId, String sku);

    boolean existsByBranchIdAndSkuAndIdNot(UUID branchId, String sku, UUID id);
}
