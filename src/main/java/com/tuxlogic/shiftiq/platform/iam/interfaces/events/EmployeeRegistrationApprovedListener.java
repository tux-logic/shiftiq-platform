package com.tuxlogic.shiftiq.platform.iam.interfaces.events;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.events.EmployeeRegistrationApprovedEvent;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.UserCommandService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.AssignBranchToUserCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EmployeeRegistrationApprovedListener {

    private static final Logger log = LoggerFactory.getLogger(EmployeeRegistrationApprovedListener.class);
    private final UserCommandService userCommandService;

    public EmployeeRegistrationApprovedListener(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(EmployeeRegistrationApprovedEvent event) {
        try {
            log.info("Handling EmployeeRegistrationApprovedEvent for employeeId: {} and branchId: {}",
                    event.employeeId().value(), event.branchId().value());
            var userId = new UserId(event.employeeId().value());
            var command = new AssignBranchToUserCommand(userId, event.branchId());
            userCommandService.handle(command);
        } catch (Exception e) {
            log.error("Error processing EmployeeRegistrationApprovedEvent for employeeId: {}",
                    event.employeeId().value(), e);
        }
    }
}
