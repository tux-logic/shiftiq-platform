package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities;

import com.tuxlogic.shiftiq.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans")
@SQLDelete(sql = "UPDATE subscription_plans SET deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionPlanPersistenceEntity extends AuditableAbstractPersistenceEntity implements Persistable<UUID> {

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "monthly_price", nullable = false)
    private double monthlyPrice;

    @Column(name = "max_obd2_devices", nullable = false)
    private int maxObd2Devices;

    @Column(name = "max_monthly_snapshots_per_vehicle", nullable = false)
    private int maxMonthlySnapshotsPerVehicle;

    @Column(name = "max_customers", nullable = false)
    private int maxCustomers;

    @Column(name = "max_staff_accounts", nullable = false)
    private int maxStaffAccounts;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
