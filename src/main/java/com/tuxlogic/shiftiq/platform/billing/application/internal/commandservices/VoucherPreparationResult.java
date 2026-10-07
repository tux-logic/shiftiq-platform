package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherCommandFailure;

/**
 * Result abstraction representing the initial preparation phase for Mercado Pago checkout.
 */
public sealed interface VoucherPreparationResult permits
        VoucherPreparationResult.ReadyToEmit,
        VoucherPreparationResult.AlreadyEmitted,
        VoucherPreparationResult.InProgress,
        VoucherPreparationResult.Failed {

    record ReadyToEmit(
            Voucher voucher,
            Quote quote,
            String issuerRuc,
            String correlative
    ) implements VoucherPreparationResult {}

    record AlreadyEmitted(
            Voucher voucher
    ) implements VoucherPreparationResult {}

    record InProgress(
            Voucher voucher
    ) implements VoucherPreparationResult {}

    record Failed(
            VoucherCommandFailure failure
    ) implements VoucherPreparationResult {}
}
