package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.SubscriptionCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.AssignSubscriptionCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CancelSubscriptionCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchSubscription;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchSubscriptionRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionCommandServiceImpl.class);

    private final BranchSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final BranchRepository branchRepository;

    public SubscriptionCommandServiceImpl(
            BranchSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            BranchRepository branchRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public Optional<BranchSubscription> handle(AssignSubscriptionCommand command) {
        if (!branchRepository.existsById(command.branchId())) {
            throw new IllegalArgumentException("core.error.branch.notFound");
        }

        planRepository.findById(command.planId())
                .orElseThrow(() -> new IllegalArgumentException("core.error.subscriptionPlan.notFound"));

        // Mock Payment Processing
        if (command.creditCard() != null && command.creditCard().cardNumber() != null && !command.creditCard().cardNumber().isBlank()) {
            log.info("Simulating payment processing via Payment Gateway for card ending in {}", 
                command.creditCard().cardNumber().substring(Math.max(0, command.creditCard().cardNumber().length() - 4)));
            log.info("Payment successful for Branch ID: {}", command.branchId());
        }

        // Todo: Validate limits when downgrading using other bounded context queries if necessary

        var existingSubscription = subscriptionRepository.findActiveByBranchId(command.branchId());
        existingSubscription.ifPresent(sub -> {
            sub.cancel(Instant.now());
            subscriptionRepository.save(sub);
        });

        Instant startDate = Instant.now();
        LocalDate localEndDate = LocalDate.now().plusMonths(command.billingCycle().name().equals("MONTHLY") ? 1 : 12);
        Instant endDate = localEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        var newSubscription = new BranchSubscription(
                command.branchId(),
                command.planId(),
                command.billingCycle(),
                startDate,
                endDate
        );

        var savedSubscription = subscriptionRepository.save(newSubscription);
        return Optional.of(savedSubscription);
    }

    @Override
    public Optional<BranchSubscription> handle(CancelSubscriptionCommand command) {
        var existingSubscription = subscriptionRepository.findActiveByBranchId(command.branchId());
        if (existingSubscription.isEmpty()) {
            throw new IllegalArgumentException("core.error.branch.noActiveSubscription");
        }

        var sub = existingSubscription.get();
        sub.cancel(Instant.now());
        subscriptionRepository.save(sub);

        return Optional.of(sub);
    }
}
