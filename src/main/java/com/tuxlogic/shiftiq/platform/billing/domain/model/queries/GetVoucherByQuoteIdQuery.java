package com.tuxlogic.shiftiq.platform.billing.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve a Voucher associated with a specific Quote ID.
 *
 * @param quoteId the unique identifier of the quote
 */
public record GetVoucherByQuoteIdQuery(UUID quoteId) {
    public GetVoucherByQuoteIdQuery {
        if (quoteId == null) {
            throw new IllegalArgumentException("billing.error.query.quoteIdRequired");
        }
    }
}
