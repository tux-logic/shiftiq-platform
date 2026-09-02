package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherCommandFailure;
import com.tuxlogic.shiftiq.platform.billing.application.commandservices.VoucherCommandService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.ProcessCheckoutCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentMethod;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.ProcessCheckoutResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.transform.VoucherResourceFromAggregateAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing billing checkouts.
 * Exposes endpoints for the checkout workflow which involves generating a voucher and processing a full payment.
 */
@RestController
@RequestMapping(value = "/api/v1/checkouts", produces = "application/json")
@Tag(name = "Checkouts", description = "Endpoints for processing complete checkout workflows")
public class CheckoutsController {

    private final VoucherCommandService commandService;
    private final org.springframework.context.MessageSource messageSource;

    public CheckoutsController(VoucherCommandService commandService, org.springframework.context.MessageSource messageSource) {
        this.commandService = commandService;
        this.messageSource = messageSource;
    }

    @PostMapping
    @Operation(summary = "Process checkout", description = "Generates a voucher and records a full payment in a single transaction")
    public ResponseEntity<?> checkout(@Valid @RequestBody ProcessCheckoutResource resource) {
        VoucherType voucherType;
        PaymentMethod paymentMethod;
        try {
            voucherType = resource.type() != null ? VoucherType.valueOf(resource.type().toUpperCase()) : VoucherType.RECEIPT;
            paymentMethod = resource.method() != null ? PaymentMethod.valueOf(resource.method().toUpperCase()) : PaymentMethod.CASH;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid voucher type or payment method");
        }

        var command = new ProcessCheckoutCommand(
                resource.quoteId(),
                voucherType,
                resource.customerDocumentType(),
                resource.customerDocumentNumber(),
                resource.customerName(),
                paymentMethod
        );

        var result = commandService.handle(command);

        if (result.isSuccess()) {
            var voucherResource = VoucherResourceFromAggregateAssembler.toResourceFromAggregate(result.success().get());
            return new ResponseEntity<>(voucherResource, HttpStatus.CREATED);
        }

        return toErrorResponse(result.failure().get());
    }

    private ResponseEntity<?> toErrorResponse(VoucherCommandFailure failure) {
        return switch (failure) {
            case QUOTE_NOT_FOUND -> {
                String message = messageSource.getMessage("billing.error.quote.notFound", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.notFound("checkout", message));
            }
            case QUOTE_NOT_APPROVED -> {
                String message = messageSource.getMessage("billing.error.voucher.notApproved", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.conflict("checkout", message));
            }
            case INVALID_VOUCHER_DATA -> {
                String message = messageSource.getMessage("billing.error.voucher.invalidData", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.validationError("checkout", message));
            }
            case ISSUER_NOT_FOUND -> {
                String message = messageSource.getMessage("billing.error.voucher.issuerNotFound", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.unexpected("checkout", message));
            }
            case FACTOS_ISSUANCE_FAILED -> {
                String message = messageSource.getMessage("billing.error.voucher.facthubFailed", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.unexpected("checkout", message));
            }
            case VOUCHER_NOT_FOUND -> {
                String message = messageSource.getMessage("billing.error.voucher.notFound", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.notFound("checkout", message));
            }
            case VOUCHER_ALREADY_PAID -> {
                String message = messageSource.getMessage("billing.error.voucher.alreadyPaidInFull", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.conflict("checkout", message));
            }
            case VOUCHER_CANCELED -> {
                String message = messageSource.getMessage("billing.error.voucher.cannotAddPaymentCanceled", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.conflict("checkout", message));
            }
            case PAYMENT_EXCEEDS_TOTAL_DEBT -> {
                String message = messageSource.getMessage("billing.error.voucher.paymentExceedsDebt", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.validationError("checkout", message));
            }
            case PAYMENT_NOT_FOUND -> {
                String message = messageSource.getMessage("billing.error.payment.notFound", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.notFound("checkout", message));
            }
        };
    }
}
