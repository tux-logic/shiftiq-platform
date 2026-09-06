package com.tuxlogic.shiftiq.platform.inventory.interfaces.events;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.InventoryQuantity;
import com.tuxlogic.shiftiq.platform.inventory.domain.repositories.ProductRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.events.ProductReservationCanceledEvent;
import com.tuxlogic.shiftiq.platform.shared.domain.model.events.ProductReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;
import java.util.function.Consumer;

@Component
public class InventoryStockListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryStockListener.class);
    private final ProductRepository productRepository;

    public InventoryStockListener(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(ProductReservedEvent event) {
        updateProductStock(event.productId(), product -> product.reserveStock(new InventoryQuantity(event.quantity())), "ProductReservedEvent", event.quantity());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(ProductReservationCanceledEvent event) {
        updateProductStock(event.productId(), product -> product.releaseStock(new InventoryQuantity(event.quantity())), "ProductReservationCanceledEvent", event.quantity());
    }

    private void updateProductStock(UUID productId, Consumer<Product> action, String eventName, int quantity) {
        try {
            productRepository.findById(productId).ifPresent(product -> {
                action.accept(product);
                productRepository.save(product);
                log.info("Processed {} for product ID {}: quantity = {}", eventName, productId, quantity);
            });
        } catch (Exception e) {
            log.error("Failed to process {} for product ID {}", eventName, productId, e);
        }
    }
}
