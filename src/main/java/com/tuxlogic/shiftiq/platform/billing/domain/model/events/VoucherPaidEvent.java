package com.tuxlogic.shiftiq.platform.billing.domain.model.events;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain event representing the moment a voucher is fully paid.
 *
 * @param source     The aggregate that emitted the event.
 * @param voucherId  The unique identifier of the paid voucher.
 * @param quoteId    The unique identifier of the quote associated with the voucher.
 * @param branchId   The branch where the payment was settled.
 * @param paidAmount The total paid amount recognized as revenue.
 */
public record VoucherPaidEvent(
        Object source,
        UUID voucherId,
        UUID quoteId,
        BranchId branchId,
        BigDecimal paidAmount
) {
    public VoucherPaidEvent(Object source, UUID voucherId, UUID quoteId) {
        this(source, voucherId, quoteId, null, BigDecimal.ZERO);
    }
}
