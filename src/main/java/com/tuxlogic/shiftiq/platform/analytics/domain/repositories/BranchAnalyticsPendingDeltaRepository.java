package com.tuxlogic.shiftiq.platform.analytics.domain.repositories;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.PendingAnalyticsDelta;

import java.util.List;

public interface BranchAnalyticsPendingDeltaRepository {

    PendingAnalyticsDelta save(PendingAnalyticsDelta pending);

    List<PendingAnalyticsDelta> findPending(int limit);

    void delete(PendingAnalyticsDelta pending);
}
