package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "workshop_roles")
@Getter
@Setter
@NoArgsConstructor
public class WorkshopRolePersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "workshop_id")
    private UUID workshopId;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "workshop_role_permissions",
            joinColumns = @JoinColumn(name = "workshop_role_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 64)
    private Set<Permission> permissions = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
