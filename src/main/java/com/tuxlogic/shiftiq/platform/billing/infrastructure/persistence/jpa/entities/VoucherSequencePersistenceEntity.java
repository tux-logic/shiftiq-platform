package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA entity representing sequential numbering for fiscal series (e.g. B001, F001) scoped per workshop.
 */
@Entity
@Table(name = "voucher_sequences")
@IdClass(VoucherSequenceId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherSequencePersistenceEntity {

    @Id
    @Column(name = "workshop_id", nullable = false)
    private UUID workshopId;

    @Id
    @Column(name = "series", length = 10, nullable = false)
    private String series;

    @Column(name = "last_correlative", nullable = false)
    private Integer lastCorrelative;
}
