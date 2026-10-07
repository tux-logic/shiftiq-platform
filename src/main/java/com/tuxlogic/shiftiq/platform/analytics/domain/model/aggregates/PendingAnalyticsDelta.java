package com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * An analytics change that could not be applied immediately and is waiting for the
 * replay job. Rows keep the original business day so a late replay still lands on the
 * snapshot of the day the event happened.
 */
@Getter
public class PendingAnalyticsDelta {

    private static final int MAX_ERROR_LENGTH = 500;

    private UUID id;
    private BranchId branchId;
    private LocalDate deltaDate;
    private AnalyticsDelta delta;
    private boolean lowStockRecompute;
    private int attempts;
    private String lastError;
    private Instant createdAt;
    private Instant updatedAt;

    protected PendingAnalyticsDelta() {
    }

    public PendingAnalyticsDelta(BranchId branchId, LocalDate deltaDate, AnalyticsDelta delta, boolean lowStockRecompute) {
        if (branchId == null) {
            throw new IllegalArgumentException("analytics.error.branchId.required");
        }
        if (deltaDate == null) {
            throw new IllegalArgumentException("analytics.error.snapshotDate.required");
        }
        if (delta == null) {
            delta = AnalyticsDelta.zero();
        }
        this.id = UUID.randomUUID();
        this.branchId = branchId;
        this.deltaDate = deltaDate;
        this.delta = delta;
        this.lowStockRecompute = lowStockRecompute;
        this.attempts = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public PendingAnalyticsDelta(UUID id, BranchId branchId, LocalDate deltaDate, AnalyticsDelta delta,
                                 boolean lowStockRecompute, int attempts, String lastError,
                                 Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.branchId = branchId;
        this.deltaDate = deltaDate;
        this.delta = delta != null ? delta : AnalyticsDelta.zero();
        this.lowStockRecompute = lowStockRecompute;
        this.attempts = attempts;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void recordFailure(String error) {
        this.attempts++;
        this.lastError = truncate(error);
        this.updatedAt = Instant.now();
    }

    private String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= MAX_ERROR_LENGTH ? error : error.substring(0, MAX_ERROR_LENGTH);
    }
}
