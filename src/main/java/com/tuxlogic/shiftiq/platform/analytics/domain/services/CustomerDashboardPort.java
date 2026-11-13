package com.tuxlogic.shiftiq.platform.analytics.domain.services;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.CustomerDashboardSummary;

import java.util.Optional;
import java.util.UUID;

/**
 * Anti-corruption port isolating {@code analytics} from {@code core}, {@code fleet},
 * {@code operations}, {@code billing}, and {@code iot} when assembling a customer's
 * self-service dashboard summary.
 */
public interface CustomerDashboardPort {
    Optional<CustomerDashboardSummary> loadSummaryForUser(UUID userId);
}
