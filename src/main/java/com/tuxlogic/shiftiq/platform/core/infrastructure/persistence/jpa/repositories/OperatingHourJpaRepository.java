package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.OperatingHourPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatingHourJpaRepository extends JpaRepository<OperatingHourPersistenceEntity, UUID> {
    List<OperatingHourPersistenceEntity> findAllByWorkshopIdAndBranchIdIsNull(UUID workshopId);
    Optional<OperatingHourPersistenceEntity> findByWorkshopIdAndBranchIdIsNullAndDayOfWeek(UUID workshopId, DayOfWeek dayOfWeek);
    List<OperatingHourPersistenceEntity> findAllByBranchId(UUID branchId);
    Optional<OperatingHourPersistenceEntity> findByBranchIdAndDayOfWeek(UUID branchId, DayOfWeek dayOfWeek);
    void deleteAllByBranchId(UUID branchId);
}
