package com.tuxlogic.shiftiq.platform.analytics.application.queryservices;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.BranchAnalyticsSummary;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsByDateRangeQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsSummaryQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetNetworkAnalyticsOverviewQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.NetworkAnalyticsOverview;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;

import java.util.List;

public interface AnalyticsQueryService {
    Result<BranchAnalyticsSummary, String> handle(GetBranchAnalyticsSummaryQuery query);
    Result<List<BranchAnalyticsSnapshot>, String> handle(GetBranchAnalyticsByDateRangeQuery query);
    Result<NetworkAnalyticsOverview, String> handle(GetNetworkAnalyticsOverviewQuery query);
}
