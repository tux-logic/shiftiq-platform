package com.tuxlogic.shiftiq.platform.inventory.interfaces.events;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.InventoryQuantity;
import com.tuxlogic.shiftiq.platform.inventory.domain.repositories.ProductRepository;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.ProductReservationCanceledEvent;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.ProductReservedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Component
public class InventoryStockListener {
    private final ProductRepository productRepository;

    public InventoryStockListener(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(ProductReservedEvent event) {
        Optional<Product> productOpt = productRepository.findById(event.productId().value());
        productOpt.ifPresent(product -> {
            product.reserveStock(new InventoryQuantity(event.quantity().value()));
            productRepository.save(product);
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(ProductReservationCanceledEvent event) {
        Optional<Product> productOpt = productRepository.findById(event.productId().value());
        productOpt.ifPresent(product -> {
            product.releaseStock(new InventoryQuantity(event.quantity().value()));
            productRepository.save(product);
        });
    }
}
