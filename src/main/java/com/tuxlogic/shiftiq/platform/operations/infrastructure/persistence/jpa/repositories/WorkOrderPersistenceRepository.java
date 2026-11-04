package com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.AppointmentId;
import com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkOrderPersistenceRepository extends JpaRepository<WorkOrderPersistenceEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = {"tasks", "tasks.products"})
    Optional<WorkOrderPersistenceEntity> findById(UUID id);

    @Query("SELECT w FROM WorkOrderPersistenceEntity w WHERE w.branchId = :branchId")
    List<WorkOrderPersistenceEntity> findAllByBranchId(@Param("branchId") BranchId branchId);

    @EntityGraph(attributePaths = {"tasks", "tasks.products"})
    Optional<WorkOrderPersistenceEntity> findByAppointmentId(AppointmentId appointmentId);

    boolean existsByAppointmentId(AppointmentId appointmentId);

    @Query("SELECT w FROM WorkOrderPersistenceEntity w WHERE w.customerId = :customerId")
    List<WorkOrderPersistenceEntity> findAllByCustomerId(@Param("customerId") CustomerId customerId);

    @Query("SELECT w FROM WorkOrderPersistenceEntity w WHERE w.vehicleId = :vehicleId")
    List<WorkOrderPersistenceEntity> findAllByVehicleId(@Param("vehicleId") VehicleId vehicleId);

    @EntityGraph(attributePaths = {"tasks", "tasks.products"})
    Optional<WorkOrderPersistenceEntity> findByInternalNumberAndBranchId(Integer internalNumber, BranchId branchId);

    @Query("SELECT DISTINCT w FROM WorkOrderPersistenceEntity w JOIN w.tasks t WHERE t.id = :taskId")
    Optional<WorkOrderPersistenceEntity> findByTaskId(@Param("taskId") UUID taskId);

    @Query("SELECT COALESCE(MAX(w.internalNumber), 0) FROM WorkOrderPersistenceEntity w WHERE w.branchId = :branchId")
    int findMaxInternalNumberByBranchId(@Param("branchId") BranchId branchId);
}
