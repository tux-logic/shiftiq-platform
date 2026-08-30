package com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_registrations")
@SQLDelete(sql = "UPDATE customer_registrations SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class CustomerRegistrationPersistenceEntity extends AuditableAbstractPersistenceEntity implements Persistable<UUID> {

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}