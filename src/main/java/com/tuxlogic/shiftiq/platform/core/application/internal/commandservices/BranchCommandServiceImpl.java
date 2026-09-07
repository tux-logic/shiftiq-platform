package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.BranchCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateBranchCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateBranchCommand;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BranchCommandServiceImpl implements BranchCommandService {

    private static final Logger log = LoggerFactory.getLogger(BranchCommandServiceImpl.class);

    private final BranchRepository branchRepository;
    private final WorkshopRepository workshopRepository;

    public BranchCommandServiceImpl(BranchRepository branchRepository, WorkshopRepository workshopRepository) {
        this.branchRepository = branchRepository;
        this.workshopRepository = workshopRepository;
    }

    @Override
    public Optional<Branch> handle(CreateBranchCommand command) {
        if (!workshopRepository.existsById(command.workshopId())) {
            throw new IllegalArgumentException("core.error.workshop.notFound");
        }
        
        if (branchRepository.existsByCode(command.code())) {
            throw new IllegalArgumentException("core.error.branch.codeMustBeUnique");
        }

        var branch = new Branch(
                command.workshopId(),
                command.code(),
                command.name(),
                command.address(),
                command.phone()
        );

        var savedBranch = branchRepository.save(branch);
        log.info("Created Branch ID '{}' for workshop ID '{}'", savedBranch.getId().value(), command.workshopId().value());
        return Optional.of(savedBranch);
    }

    @Override
    public Optional<Branch> handle(UpdateBranchCommand command) {
        var branch = branchRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("core.error.branch.notFound"));

        if (!branch.getCode().equals(command.code()) && branchRepository.existsByCode(command.code())) {
            throw new IllegalArgumentException("core.error.branch.codeMustBeUnique");
        }
        
        branch.update(
            command.code(),
            command.name(),
            command.address(),
            command.phone()
        );

        var savedBranch = branchRepository.save(branch);
        log.info("Updated Branch ID '{}'", savedBranch.getId().value());
        return Optional.of(savedBranch);
    }
}
