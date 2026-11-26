package com.tuxlogic.shiftiq.platform.core.application.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchBay;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateBranchBayCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeactivateBranchBayCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateBranchBayCommand;

import java.util.Optional;

public interface BranchBayCommandService {
    Optional<BranchBay> handle(CreateBranchBayCommand command);
    Optional<BranchBay> handle(UpdateBranchBayCommand command);
    boolean handle(DeactivateBranchBayCommand command);
}