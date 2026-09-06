package com.tuxlogic.shiftiq.platform.operations.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.inventory.application.queryservices.ProductQueryService;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.queries.GetProductByIdQuery;
import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalProductService;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * ACL adapter delegating product pricing queries to the inventory context.
 */
@Service
public class ExternalProductServiceImpl implements ExternalProductService {

    private final ProductQueryService productQueryService;

    public ExternalProductServiceImpl(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }

    @Override
    public Optional<Money> getProductSellingPrice(UUID productId) {
        return productQueryService.handle(new GetProductByIdQuery(productId))
                .map(product -> product.getCurrentSellingPrice());
    }
}
