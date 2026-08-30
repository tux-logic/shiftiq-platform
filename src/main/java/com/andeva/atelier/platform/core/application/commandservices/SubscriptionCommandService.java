package com.andeva.atelier.platform.core.application.commandservices;

import com.andeva.atelier.platform.core.domain.model.commands.AssignSubscriptionCommand;
import com.andeva.atelier.platform.core.domain.model.commands.CancelSubscriptionCommand;
import com.andeva.atelier.platform.core.domain.model.aggregates.BranchSubscription;

import java.util.Optional;

public interface SubscriptionCommandService {
    Optional<BranchSubscription> handle(AssignSubscriptionCommand command);
    Optional<BranchSubscription> handle(CancelSubscriptionCommand command);
}
