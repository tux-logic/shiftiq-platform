package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.util.UUID;

/**
 * Service providing consecutive, gapless fiscal correlative numbers for SUNAT invoice and receipt series.
 */
public interface FiscalCorrelativeService {

    /**
     * Allocates and returns the next fiscal correlative for the given series and workshop,
     * formatted as an 8-digit zero-padded string (e.g. 00000001).
     *
     * @param workshopId the workshop identifier
     * @param series the fiscal series (e.g., F001, B001)
     * @return the zero-padded 8-digit correlative
     */
    String nextCorrelative(UUID workshopId, String series);

    /**
     * Backward-compatible overload allocating the next fiscal correlative with default workshop scope.
     *
     * @param series the fiscal series (e.g., F001, B001)
     * @return the zero-padded 8-digit correlative
     */
    default String nextCorrelative(String series) {
        return nextCorrelative(null, series);
    }
}
