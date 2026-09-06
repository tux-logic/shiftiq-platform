package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BillingCycle;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.SubscriptionStatus;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "branch_subscriptions")
@SQLDelete(sql = "UPDATE branch_subscriptions SET deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class BranchSubscriptionPersistenceEntity extends AuditableAbstractPersistenceEntity implements Persistable<UUID> {

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "canceled_at")
    private Date canceledAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
