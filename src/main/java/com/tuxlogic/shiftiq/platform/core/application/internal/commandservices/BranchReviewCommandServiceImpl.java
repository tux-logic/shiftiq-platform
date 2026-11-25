package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.BranchReviewCommandService;
import com.tuxlogic.shiftiq.platform.core.application.outboundservices.ExternalFleetAppointmentService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchReview;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateBranchReviewCommand;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class BranchReviewCommandServiceImpl implements BranchReviewCommandService {

    private static final Logger log = LoggerFactory.getLogger(BranchReviewCommandServiceImpl.class);

    private final BranchReviewRepository reviewRepository;
    private final BranchRepository branchRepository;
    private final ExternalFleetAppointmentService externalFleetAppointmentService;

    public BranchReviewCommandServiceImpl(
            BranchReviewRepository reviewRepository,
            BranchRepository branchRepository,
            ExternalFleetAppointmentService externalFleetAppointmentService
    ) {
        this.reviewRepository = reviewRepository;
        this.branchRepository = branchRepository;
        this.externalFleetAppointmentService = externalFleetAppointmentService;
    }

    @Override
    public Optional<BranchReview> handle(CreateBranchReviewCommand command) {
        if (!branchRepository.existsById(command.branchId())) {
            throw new IllegalArgumentException("core.error.branch.notFound");
        }
        if (command.appointmentId() != null) {
            if (reviewRepository.existsByAppointmentId(command.appointmentId())) {
                throw new IllegalArgumentException("core.error.review.appointmentAlreadyReviewed");
            }
            if (!externalFleetAppointmentService.isCompletedAppointmentForCustomer(
                    command.appointmentId(),
                    command.branchId().value(),
                    command.customerId()
            )) {
                log.warn("Review rejected: appointment {} is not completed for customer", command.appointmentId());
                throw new IllegalArgumentException("core.error.review.appointmentNotCompleted");
            }
        }
        var review = new BranchReview(
                command.branchId(),
                command.customerId(),
                command.appointmentId(),
                command.rating(),
                command.comment()
        );
        return Optional.of(reviewRepository.save(review));
    }
}