package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.OperatingHourExceptionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatingHourExceptionJpaRepository extends JpaRepository<OperatingHourExceptionPersistenceEntity, UUID> {
    List<OperatingHourExceptionPersistenceEntity> findAllByWorkshopIdAndBranchIdIsNullOrderByExceptionDateAsc(UUID workshopId);
    Optional<OperatingHourExceptionPersistenceEntity> findByWorkshopIdAndBranchIdIsNullAndExceptionDate(UUID workshopId, LocalDate exceptionDate);
    List<OperatingHourExceptionPersistenceEntity> findAllByBranchIdOrderByExceptionDateAsc(UUID branchId);
    Optional<OperatingHourExceptionPersistenceEntity> findByBranchIdAndExceptionDate(UUID branchId, LocalDate exceptionDate);
}
