package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.operations.domain.model.valueobjects.AppointmentId;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import java.util.UUID;

@Repository
public interface WorkOrderPersistenceRepository extends JpaRepository<WorkOrderPersistenceEntity, UUID> {

    List<WorkOrderPersistenceEntity> findAllByBranchId(BranchId branchId);

    Optional<WorkOrderPersistenceEntity> findByAppointmentId(AppointmentId appointmentId);

    boolean existsByAppointmentId(AppointmentId appointmentId);

    List<WorkOrderPersistenceEntity> findAllByCustomerId(CustomerId customerId);

    List<WorkOrderPersistenceEntity> findAllByVehicleId(VehicleId vehicleId);

    Optional<WorkOrderPersistenceEntity> findByInternalNumberAndBranchId(Integer internalNumber, BranchId branchId);

    @Query("SELECT w FROM WorkOrderPersistenceEntity w JOIN w.tasks t WHERE t.id = :taskId")
    Optional<WorkOrderPersistenceEntity> findByTaskId(@Param("taskId") UUID taskId);

    @Query("SELECT COALESCE(MAX(w.internalNumber), 0) FROM WorkOrderPersistenceEntity w WHERE w.branchId = :branchId")
    int findMaxInternalNumberByBranchId(@Param("branchId") BranchId branchId);
}
