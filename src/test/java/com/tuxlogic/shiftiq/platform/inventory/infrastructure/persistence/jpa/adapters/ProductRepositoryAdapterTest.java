package com.tuxlogic.shiftiq.platform.inventory.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertTriggeredEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCategory;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductName;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.Sku;
import com.tuxlogic.shiftiq.platform.inventory.infrastructure.persistence.jpa.entities.ProductJpaEntity;
import com.tuxlogic.shiftiq.platform.inventory.infrastructure.persistence.jpa.repositories.ProductJpaRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

    @Mock
    private ProductJpaRepository productJpaRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductRepositoryAdapter adapter;

    @Test
    @DisplayName("save publishes low stock domain events and clears them")
    void savePublishesLowStockEventsAndClearsDomainEvents() {
        var product = new Product(
                null,
                new BranchId(UUID.randomUUID()),
                new ProductCategory("PART"),
                new ProductName("Filtro de aceite"),
                new Sku("FLT-001"),
                new Money(new BigDecimal("45.50")),
                "Descripcion",
                5
        );
        product.refreshLowStockAlert();
        when(productJpaRepository.save(any(ProductJpaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(product);

        verify(eventPublisher).publishEvent(any(LowStockAlertTriggeredEvent.class));
        assertThat(product.domainEvents()).isEmpty();
    }
}
