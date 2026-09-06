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

    @EntityGraph(attributePaths = {"tasks", "tasks.products"})
    List<WorkOrderPersistenceEntity> findAllByBranchId(BranchId branchId);

    @EntityGraph(attributePaths = {"tasks", "tasks.products"})
    Optional<WorkOrderPersistenceEntity> findByAppointmentId(AppointmentId appointmentId);

    boolean existsByAppointmentId(AppointmentId appointmentId);

    @EntityGraph(attributePaths = {"tasks", "tasks.products"})
    List<WorkOrderPersistenceEntity> findAllByCustomerId(CustomerId customerId);

    @EntityGraph(attributePaths = {"tasks", "tasks.products"})
    List<WorkOrderPersistenceEntity> findAllByVehicleId(VehicleId vehicleId);

    @EntityGraph(attributePaths = {"tasks", "tasks.products"})
    Optional<WorkOrderPersistenceEntity> findByInternalNumberAndBranchId(Integer internalNumber, BranchId branchId);

    @EntityGraph(attributePaths = {"tasks", "tasks.products"})
    @Query("SELECT w FROM WorkOrderPersistenceEntity w JOIN w.tasks t WHERE t.id = :taskId")
    Optional<WorkOrderPersistenceEntity> findByTaskId(@Param("taskId") UUID taskId);

    @Query("SELECT COALESCE(MAX(w.internalNumber), 0) FROM WorkOrderPersistenceEntity w WHERE w.branchId = :branchId")
    int findMaxInternalNumberByBranchId(@Param("branchId") BranchId branchId);
}
