package com.andeva.atelier.platform.billing.application.commandservices;

import com.andeva.atelier.platform.billing.domain.model.aggregates.Voucher;
import com.andeva.atelier.platform.billing.domain.model.commands.GenerateVoucherCommand;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.VoucherCommandFailure;
import com.andeva.atelier.platform.billing.domain.model.commands.AddPaymentCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.RemovePaymentCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.ProcessCheckoutCommand;

/**
 * Application service contract for executing Voucher command operations.
 * Handles the generation of invoices/receipts, checkout processes, and payment tracking.
 */
public interface VoucherCommandService {
    /**
     * Generates a new Voucher based on an approved Quote.
     * Integrates with external systems (e.g., Facthub) to register the invoice with tax authorities.
     * 
     * @param command The command object containing quote ID and customer billing details.
     * @return A Result containing the generated Voucher, or a specific failure reason.
     */
    Result<Voucher, VoucherCommandFailure> handle(GenerateVoucherCommand command);
    /**
     * Records a new partial or full payment against an existing Voucher.
     * 
     * @param command The command object containing voucher ID, payment amount, and method.
     * @return A Result containing the updated Voucher, or a specific failure reason.
     */
    Result<Voucher, VoucherCommandFailure> handle(AddPaymentCommand command);
    /**
     * Removes a previously recorded payment from a Voucher.
     * 
     * @param command The command object containing voucher ID and payment ID.
     * @return A Result containing the updated Voucher, or a specific failure reason.
     */
    Result<Voucher, VoucherCommandFailure> handle(RemovePaymentCommand command);
    /**
     * Processes a full checkout: generates a new Voucher and immediately records a payment for the total amount.
     * 
     * @param command The command object containing quote ID, customer details, and payment method.
     * @return A Result containing the fully paid Voucher, or a specific failure reason.
     */
    Result<Voucher, VoucherCommandFailure> handle(ProcessCheckoutCommand command);
}
