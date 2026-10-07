package com.tuxlogic.shiftiq.platform.analytics.application.internal.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Clock abstraction for the analytics bounded context.
 * All daily snapshot buckets are resolved with the configured time zone
 * ({@code analytics.zone-id}) so KPI days match the business day of the
 * workshop network instead of the JVM default time zone.
 */
@Component
public class AnalyticsClock {

    private final ZoneId zoneId;

    public AnalyticsClock(@Value("${analytics.zone-id:America/Lima}") String zoneId) {
        this.zoneId = ZoneId.of(zoneId);
    }

    public LocalDate today() {
        return LocalDate.now(zoneId);
    }

    public ZoneId zoneId() {
        return zoneId;
    }
}
