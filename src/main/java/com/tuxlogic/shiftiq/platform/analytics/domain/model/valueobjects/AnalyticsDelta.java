package com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Additive change applied to a daily analytics snapshot. Applied as a single atomic
 * upsert so concurrent events never overwrite each other's counters.
 */
public record AnalyticsDelta(BigDecimal revenue, int workOrders, int appointments, int dtcAlerts) {

    public AnalyticsDelta {
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }
    }

    public static AnalyticsDelta of(BigDecimal revenue, int workOrders, int appointments, int dtcAlerts) {
        return new AnalyticsDelta(revenue, workOrders, appointments, dtcAlerts);
    }

    public static AnalyticsDelta zero() {
        return new AnalyticsDelta(BigDecimal.ZERO, 0, 0, 0);
    }

    public AnalyticsDelta negated() {
        return new AnalyticsDelta(revenue.negate(), -workOrders, -appointments, -dtcAlerts);
    }
}
