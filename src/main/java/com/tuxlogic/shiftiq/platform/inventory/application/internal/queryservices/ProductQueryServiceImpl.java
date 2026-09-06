package com.tuxlogic.shiftiq.platform.inventory.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.inventory.application.queryservices.ProductQueryService;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.queries.GetProductByIdQuery;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.queries.GetProductsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.inventory.domain.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {

    private static final Logger log = LoggerFactory.getLogger(ProductQueryServiceImpl.class);
    private final ProductRepository productRepository;

    public ProductQueryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> handle(GetProductsByBranchIdQuery query) {
        if (query.branchId() == null) {
            throw new IllegalArgumentException("inventory.error.query.branchId.required");
        }
        if (hasFilters(query)) {
            return productRepository.findAllByBranchIdWithFilters(
                    query.branchId(),
                    query.name(),
                    query.category(),
                    query.lowStockOnly()
            );
        }
        return productRepository.findAllByBranchId(query.branchId());
    }

    @Override
    public Optional<Product> handle(GetProductByIdQuery query) {
        return productRepository.findById(query.productId());
    }

    private boolean hasFilters(GetProductsByBranchIdQuery query) {
        return (query.name() != null && !query.name().isBlank())
                || (query.category() != null && !query.category().isBlank())
                || Boolean.TRUE.equals(query.lowStockOnly());
    }
}
