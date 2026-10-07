package com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BranchAnalyticsSnapshotTest {

    @Test
    @DisplayName("constructor initializes fields with default values")
    void constructorInitializesDefaults() {
        var branchId = new BranchId(UUID.randomUUID());
        var date = LocalDate.now();

        var snapshot = new BranchAnalyticsSnapshot(branchId, date);

        assertThat(snapshot.getId()).isNotNull();
        assertThat(snapshot.getBranchId()).isEqualTo(branchId);
        assertThat(snapshot.getSnapshotDate()).isEqualTo(date);
        assertThat(snapshot.getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
        assertThat(snapshot.getCompletedWorkOrdersCount()).isEqualTo(0);
        assertThat(snapshot.getTotalAppointmentsCount()).isEqualTo(0);
        assertThat(snapshot.getLowStockAlertsCount()).isEqualTo(0);
        assertThat(snapshot.getDtcAlertsCount()).isEqualTo(0);
        assertThat(snapshot.getCreatedAt()).isNotNull();
        assertThat(snapshot.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("constructor throws exception on null branchId or date")
    void constructorValidation() {
        var branchId = new BranchId(UUID.randomUUID());
        var date = LocalDate.now();

        assertThatThrownBy(() -> new BranchAnalyticsSnapshot(null, date))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new BranchAnalyticsSnapshot(branchId, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("addRevenue increments revenue correctly")
    void addRevenueIncrements() {
        var snapshot = new BranchAnalyticsSnapshot(new BranchId(UUID.randomUUID()), LocalDate.now());
        snapshot.addRevenue(new BigDecimal("150.50"));
        snapshot.addRevenue(new BigDecimal("49.50"));

        assertThat(snapshot.getTotalRevenue()).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("counters increment correctly")
    void countersIncrement() {
        var snapshot = new BranchAnalyticsSnapshot(new BranchId(UUID.randomUUID()), LocalDate.now());
        snapshot.incrementCompletedWorkOrders();
        snapshot.incrementTotalAppointments();
        snapshot.incrementDtcAlertsCount();
        snapshot.updateLowStockAlertsCount(5);

        assertThat(snapshot.getCompletedWorkOrdersCount()).isEqualTo(1);
        assertThat(snapshot.getTotalAppointmentsCount()).isEqualTo(1);
        assertThat(snapshot.getDtcAlertsCount()).isEqualTo(1);
        assertThat(snapshot.getLowStockAlertsCount()).isEqualTo(5);
    }
}
