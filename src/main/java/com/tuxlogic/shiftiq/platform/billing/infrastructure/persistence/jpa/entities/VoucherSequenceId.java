package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite primary key for VoucherSequencePersistenceEntity scoped by workshop and series.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VoucherSequenceId implements Serializable {
    private UUID workshopId;
    private String series;
}
