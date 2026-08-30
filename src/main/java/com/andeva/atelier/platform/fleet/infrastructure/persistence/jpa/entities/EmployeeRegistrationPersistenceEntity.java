package com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "employee_registrations")
@Getter
@Setter
public class EmployeeRegistrationPersistenceEntity {

    @Id
    @Column(nullable = false, unique = true)
    private UUID id;

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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
