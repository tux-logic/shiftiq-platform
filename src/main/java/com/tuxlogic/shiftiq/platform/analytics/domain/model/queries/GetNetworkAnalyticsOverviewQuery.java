package com.tuxlogic.shiftiq.platform.analytics.domain.model.queries;

import java.util.Set;
import java.util.UUID;

/**
 * Network-wide aggregation query.
 *
 * @param branchIds branch scope for the aggregation: must be an explicit, non-null set;
 *                  an empty set means the caller may not see any branch
 */
public record GetNetworkAnalyticsOverviewQuery(Set<UUID> branchIds) {
    public GetNetworkAnalyticsOverviewQuery {
        if (branchIds == null) {
            throw new IllegalArgumentException("analytics.error.branchScope.required");
        }
    }
}
