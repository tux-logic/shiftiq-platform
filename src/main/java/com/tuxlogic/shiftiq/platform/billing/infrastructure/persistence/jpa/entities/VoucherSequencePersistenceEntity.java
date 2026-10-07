package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing sequential numbering for fiscal series (e.g. B001, F001).
 */
@Entity
@Table(name = "voucher_sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherSequencePersistenceEntity {

    @Id
    @Column(name = "series", length = 10, nullable = false)
    private String series;

    @Column(name = "last_correlative", nullable = false)
    private Integer lastCorrelative;
}
