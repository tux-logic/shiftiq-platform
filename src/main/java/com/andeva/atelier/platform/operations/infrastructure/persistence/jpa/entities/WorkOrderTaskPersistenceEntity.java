package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities;

import org.springframework.data.domain.Persistable;
import java.util.UUID;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.MechanicId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.TaskDescription;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters.TaskDescriptionAttributeConverter;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.converters.MoneyAttributeConverter;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "work_order_tasks")
@SQLDelete(sql = "UPDATE work_order_tasks SET deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
public class WorkOrderTaskPersistenceEntity extends AuditableAbstractPersistenceEntity implements Persistable<UUID> {

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "service_id", nullable = false))
    private ServiceId serviceId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "branch_id", nullable = false))
    private BranchId branchId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "assigned_mechanic_id", nullable = false))
    private MechanicId assignedMechanicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderTaskStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Convert(converter = TaskDescriptionAttributeConverter.class)
    private TaskDescription description;

    @Column(name = "price", nullable = false)
    @Convert(converter = MoneyAttributeConverter.class)
    private Money price;

    private Instant startedAt;

    private Instant completedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_task_id", nullable = false)
    private List<WorkOrderTaskProductPersistenceEntity> products = new ArrayList<>();

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private java.util.UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private java.util.UUID updatedBy;

    @Version
    private Long version;
}
