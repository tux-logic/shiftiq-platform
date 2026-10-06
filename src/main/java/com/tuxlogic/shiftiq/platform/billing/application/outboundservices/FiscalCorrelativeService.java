package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

/**
 * Service providing strictly sequential, collision-free fiscal correlatives per series.
 */
public interface FiscalCorrelativeService {

    /**
     * Obtains the next 8-digit fiscal correlative (e.g. "00000001") for the specified series.
     *
     * @param series the fiscal series (e.g. "B001", "F001")
     * @return the zero-padded 8-digit correlative string
     */
    String nextCorrelative(String series);
}
