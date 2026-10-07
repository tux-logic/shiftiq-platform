package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record SetUserBranchesCommand(
        UserId userId,
        Set<UUID> branchIds
) {
    public SetUserBranchesCommand {
        if (userId == null) {
            throw new IllegalArgumentException("iam.error.userId.required");
        }
        branchIds = branchIds == null ? Set.of()
                : branchIds.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }
}
