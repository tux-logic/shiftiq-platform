package com.tuxlogic.shiftiq.platform.core.application.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateBranchCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateBranchCommand;

import java.util.Optional;

public interface BranchCommandService {
    Optional<Branch> handle(CreateBranchCommand command);
    Optional<Branch> handle(UpdateBranchCommand command);
}
