package com.tuxlogic.shiftiq.platform.analytics.domain.services;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.EmployeeDashboardSummary;

import java.util.Optional;
import java.util.UUID;

/**
 * Anti-corruption port isolating {@code analytics} from {@code core}, {@code fleet},
 * {@code operations}, and {@code iot} when assembling an employee's personal work queue summary.
 */
public interface EmployeeDashboardPort {
    Optional<EmployeeDashboardSummary> loadSummaryForUser(UUID userId);
}
