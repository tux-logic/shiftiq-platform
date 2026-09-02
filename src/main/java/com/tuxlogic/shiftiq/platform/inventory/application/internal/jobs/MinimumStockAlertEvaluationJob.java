package com.tuxlogic.shiftiq.platform.inventory.application.internal.jobs;

import com.tuxlogic.shiftiq.platform.inventory.domain.repositories.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MinimumStockAlertEvaluationJob {

    private final ProductRepository productRepository;

    public MinimumStockAlertEvaluationJob(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Scheduled(cron = "${inventory.low-stock-alert.cron:0 0 * * * *}")
    @Transactional
    public void evaluateMinimumStockAlerts() {
        productRepository.findAll().forEach(product -> {
            if (product.refreshLowStockAlert()) {
                productRepository.save(product);
            }
        });
    }
}
