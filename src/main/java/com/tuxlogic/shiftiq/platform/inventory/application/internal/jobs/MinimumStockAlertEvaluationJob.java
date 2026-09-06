package com.tuxlogic.shiftiq.platform.inventory.application.internal.jobs;

import com.tuxlogic.shiftiq.platform.inventory.domain.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MinimumStockAlertEvaluationJob {

    private static final Logger log = LoggerFactory.getLogger(MinimumStockAlertEvaluationJob.class);
    private final ProductRepository productRepository;

    public MinimumStockAlertEvaluationJob(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Scheduled(cron = "${inventory.low-stock-alert.cron:0 0 * * * *}")
    @Transactional
    public void evaluateMinimumStockAlerts() {
        log.info("Starting scheduled evaluation of minimum stock alerts");
        try {
            productRepository.findAll().forEach(product -> {
                try {
                    if (product.refreshLowStockAlert()) {
                        productRepository.save(product);
                        log.info("Low stock alert status updated for product ID {}", product.getId());
                    }
                } catch (Exception e) {
                    log.error("Error evaluating low stock alert for product ID {}", product.getId(), e);
                }
            });
            log.info("Completed scheduled evaluation of minimum stock alerts");
        } catch (Exception e) {
            log.error("Failed to execute MinimumStockAlertEvaluationJob", e);
        }
    }
}
