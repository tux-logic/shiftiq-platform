package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Address;

public record CreateBranchCommand(
        WorkshopId workshopId,
        String code,
        String name,
        Address address,
        Phone phone,
        String coverImageUrl,
        Double latitude,
        Double longitude
) {
    public CreateBranchCommand {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("core.error.code.required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("core.error.name.required");
    }

    public CreateBranchCommand(WorkshopId workshopId, String code, String name, Address address, Phone phone) {
        this(workshopId, code, name, address, phone, null, null, null);
    }

    public CreateBranchCommand(WorkshopId workshopId, String code, String name, Address address, Phone phone, String coverImageUrl) {
        this(workshopId, code, name, address, phone, coverImageUrl, null, null);
    }
}
