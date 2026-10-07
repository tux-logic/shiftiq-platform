package com.tuxlogic.shiftiq.platform.analytics.domain.model.queries;

import java.util.Set;
import java.util.UUID;

/**
 * Network-wide aggregation query.
 *
 * @param branchIds branch scope for the aggregation: {@code null} means unrestricted
 *                  (platform admin), an empty set means the caller may not see any branch
 */
public record GetNetworkAnalyticsOverviewQuery(Set<UUID> branchIds) {}
