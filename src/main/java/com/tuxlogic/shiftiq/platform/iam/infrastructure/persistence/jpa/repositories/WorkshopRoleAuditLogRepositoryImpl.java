package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.WorkshopRoleAuditLog;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleAuditLogRepository;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers.WorkshopRoleAuditLogPersistenceAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WorkshopRoleAuditLogRepositoryImpl implements WorkshopRoleAuditLogRepository {

    private final WorkshopRoleAuditLogPersistenceRepository jpaRepository;

    public WorkshopRoleAuditLogRepositoryImpl(WorkshopRoleAuditLogPersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WorkshopRoleAuditLog save(WorkshopRoleAuditLog log) {
        var saved = jpaRepository.save(WorkshopRoleAuditLogPersistenceAssembler.toEntity(log));
        return WorkshopRoleAuditLogPersistenceAssembler.toDomain(saved);
    }

    @Override
    public List<WorkshopRoleAuditLog> findByWorkshopIdOrderByRecordedAtDesc(UUID workshopId) {
        return jpaRepository.findByWorkshopIdOrderByRecordedAtDesc(workshopId).stream()
                .map(WorkshopRoleAuditLogPersistenceAssembler::toDomain)
                .toList();
    }
}
