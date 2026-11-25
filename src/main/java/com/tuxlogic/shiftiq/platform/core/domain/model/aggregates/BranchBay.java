package com.tuxlogic.shiftiq.platform.core.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BayType;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchBayId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BranchBay extends AbstractDomainAggregateRoot<BranchBay> {

    private BranchBayId id;
    private BranchId branchId;
    private String code;
    private String name;
    private BayType type;
    private boolean active = true;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Long version;

    public BranchBay() {
    }

    public BranchBay(
            BranchBayId id,
            BranchId branchId,
            String code,
            String name,
            BayType type,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            Long version
    ) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("core.error.bayCode.required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("core.error.bayName.required");
        }
        if (type == null) {
            throw new IllegalArgumentException("core.error.bayType.required");
        }
        this.id = id;
        this.branchId = branchId;
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.type = type;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public BranchBay(BranchId branchId, String code, String name, BayType type) {
        if (branchId == null) {
            throw new IllegalArgumentException("core.error.branchId.required");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("core.error.bayCode.required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("core.error.bayName.required");
        }
        if (type == null) {
            throw new IllegalArgumentException("core.error.bayType.required");
        }
        this.id = new BranchBayId(UUID.randomUUID());
        this.branchId = branchId;
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.type = type;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(String name, BayType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("core.error.bayName.required");
        }
        if (type == null) {
            throw new IllegalArgumentException("core.error.bayType.required");
        }
        this.name = name.trim();
        this.type = type;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}