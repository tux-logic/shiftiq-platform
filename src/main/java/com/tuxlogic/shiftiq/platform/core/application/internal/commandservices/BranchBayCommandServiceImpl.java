package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.BranchBayCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchBay;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateBranchBayCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeactivateBranchBayCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateBranchBayCommand;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchBayRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class BranchBayCommandServiceImpl implements BranchBayCommandService {

    private static final Logger log = LoggerFactory.getLogger(BranchBayCommandServiceImpl.class);

    private final BranchBayRepository bayRepository;
    private final BranchRepository branchRepository;

    public BranchBayCommandServiceImpl(BranchBayRepository bayRepository, BranchRepository branchRepository) {
        this.bayRepository = bayRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public Optional<BranchBay> handle(CreateBranchBayCommand command) {
        if (!branchRepository.existsById(command.branchId())) {
            log.warn("Cannot create bay: branch {} not found", command.branchId().value());
            throw new IllegalArgumentException("core.error.branch.notFound");
        }
        if (bayRepository.existsByBranchIdAndCode(command.branchId(), command.code())) {
            throw new IllegalArgumentException("core.error.bayCode.alreadyExists");
        }
        var bay = new BranchBay(command.branchId(), command.code(), command.name(), command.type());
        return Optional.of(bayRepository.save(bay));
    }

    @Override
    public Optional<BranchBay> handle(UpdateBranchBayCommand command) {
        var existing = bayRepository.findById(command.bayId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        var bay = existing.get();
        bay.update(command.name(), command.type());
        return Optional.of(bayRepository.save(bay));
    }

    @Override
    public boolean handle(DeactivateBranchBayCommand command) {
        var existing = bayRepository.findById(command.bayId());
        if (existing.isEmpty()) {
            return false;
        }
        var bay = existing.get();
        bay.deactivate();
        bayRepository.save(bay);
        return true;
    }
}