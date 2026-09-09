package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities;

import com.tuxlogic.shiftiq.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "employee_registrations")
@SQLDelete(sql = "UPDATE employee_registrations SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeRegistrationPersistenceEntity extends AuditableAbstractPersistenceEntity implements Persistable<UUID> {

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "speciality", nullable = false, length = 50)
    private String speciality;

    @Column(name = "speciality_name", length = 50)
    private String specialityName;

    @Column(name = "salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal salary;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}

