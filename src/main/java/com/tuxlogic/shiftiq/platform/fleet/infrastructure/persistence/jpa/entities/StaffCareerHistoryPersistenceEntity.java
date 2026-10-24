package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "staff_career_history")
@Getter
@Setter
@NoArgsConstructor
public class StaffCareerHistoryPersistenceEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "registration_id", nullable = false)
    private UUID registrationId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "speciality", nullable = false, length = 50)
    private String speciality;

    @Column(name = "speciality_name", length = 100)
    private String specialityName;

    @Column(name = "salary", precision = 10, scale = 2)
    private BigDecimal salary;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public StaffCareerHistoryPersistenceEntity(UUID id, UUID registrationId, UUID employeeId, UUID branchId,
                                               String speciality, String specialityName, BigDecimal salary,
                                               String status, String changeType, String changeReason,
                                               UUID changedBy, Instant recordedAt) {
        this.id = id;
        this.registrationId = registrationId;
        this.employeeId = employeeId;
        this.branchId = branchId;
        this.speciality = speciality;
        this.specialityName = specialityName;
        this.salary = salary;
        this.status = status;
        this.changeType = changeType;
        this.changeReason = changeReason;
        this.changedBy = changedBy;
        this.recordedAt = recordedAt;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
