package com.tuxlogic.shiftiq.platform.core.application.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.AssignSubscriptionCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CancelSubscriptionCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchSubscription;

import java.util.Optional;

public interface SubscriptionCommandService {
    Optional<BranchSubscription> handle(AssignSubscriptionCommand command);
    Optional<BranchSubscription> handle(CancelSubscriptionCommand command);
}
