package com.tuxlogic.shiftiq.platform.core.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import com.tuxlogic.shiftiq.platform.core.domain.model.events.OwnerCreatedEvent;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Owner extends AbstractDomainAggregateRoot<Owner> {

    private OwnerId id;
    private UserId userId;
    private PersonName name;
    private Document document;
    private Phone phone;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Long version;

    public Owner() {}

    public Owner(OwnerId id, UserId userId, PersonName name, Document document, Phone phone, Instant createdAt, Instant updatedAt, Instant deletedAt, Long version) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.document = document;
        this.phone = phone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public Owner(UserId userId, PersonName name, Document document, Phone phone) {
        this.id = new OwnerId(UUID.randomUUID());
        this.userId = userId;
        this.name = name;
        this.document = document;
        this.phone = phone;
        this.registerDomainEvent(new OwnerCreatedEvent(this, this.id.value(), this.userId != null ? this.userId.value() : null));
    }

    public void update(PersonName name, Document document, Phone phone) {
        this.name = name;
        this.document = document;
        this.phone = phone;
        this.registerDomainEvent(new com.tuxlogic.shiftiq.platform.core.domain.model.events.OwnerUpdatedEvent(this, this.id.value()));
    }
}
