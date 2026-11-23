package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.MercadoPagoPaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.commandservices.PaymentSessionService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.ExternalBillingContextService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentCheckoutSessionResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentGatewayRegistry;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.VoucherQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetVoucherByQuoteIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Shared application service for creating virtual payment sessions across any registered {@link PaymentProvider}.
 */
@Service
public class PaymentSessionServiceImpl implements PaymentSessionService {

    private final QuoteQueryService quoteQueryService;
    private final VoucherQueryService voucherQueryService;
    private final MercadoPagoPaymentCommandService paymentCommandService;
    private final MultiTenancySecurityService multiTenancySecurityService;
    private final ExternalBillingContextService externalBillingContextService;
    private final PaymentGatewayRegistry paymentGatewayRegistry;
    private final MessageSource messageSource;

    public PaymentSessionServiceImpl(
            QuoteQueryService quoteQueryService,
            VoucherQueryService voucherQueryService,
            MercadoPagoPaymentCommandService paymentCommandService,
            MultiTenancySecurityService multiTenancySecurityService,
            ExternalBillingContextService externalBillingContextService,
            PaymentGatewayRegistry paymentGatewayRegistry,
            MessageSource messageSource) {
        this.quoteQueryService = quoteQueryService;
        this.voucherQueryService = voucherQueryService;
        this.paymentCommandService = paymentCommandService;
        this.multiTenancySecurityService = multiTenancySecurityService;
        this.externalBillingContextService = externalBillingContextService;
        this.paymentGatewayRegistry = paymentGatewayRegistry;
        this.messageSource = messageSource;
    }

    @Override
    public Result<PaymentCheckoutSessionResult, ApplicationError> createCheckoutSession(
            UUID quoteId,
            PaymentProvider provider,
            String type,
            String customerDocumentType,
            String customerDocumentNumber,
            String customerName,
            Authentication authentication) {
        var quoteOpt = quoteQueryService.handle(new GetQuoteByIdQuery(quoteId));
        if (quoteOpt.isEmpty()) {
            String message = messageSource.getMessage("billing.error.quote.notFound", null, LocaleContextHolder.getLocale());
            return Result.failure(ApplicationError.notFound("preference", message));
        }

        var quote = quoteOpt.get();
        boolean isBranchStaff = multiTenancySecurityService.isAuthorizedForBranch(quote.getBranchId().value());
        if (!isBranchStaff) {
            boolean isOwningCustomer = authentication != null
                    && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal
                    && externalBillingContextService.isCustomerOwnerOfWorkOrder(principal.getId(), quote.getWorkOrderId());
            if (!isOwningCustomer) {
                throw new AccessDeniedException("Unauthorized access for requested quote: " + quote.getId());
            }
            if (!externalBillingContextService.isWorkOrderCompleted(quote.getWorkOrderId())) {
                return Result.failure(ApplicationError.conflict(
                        "preference", "Work order must be COMPLETED before customer payment."));
            }
        }

        if (quote.getStatus() != QuoteStatus.APPROVED) {
            String message = messageSource.getMessage("billing.error.voucher.notApproved", null, LocaleContextHolder.getLocale());
            return Result.failure(ApplicationError.conflict("preference", message));
        }

        if (voucherQueryService.handle(new GetVoucherByQuoteIdQuery(quote.getId())).isPresent()) {
            String message = messageSource.getMessage("billing.error.quote.alreadyInvoiced", null, LocaleContextHolder.getLocale());
            return Result.failure(ApplicationError.conflict("preference", message));
        }

        if ("INVOICE".equalsIgnoreCase(type)) {
            if (customerDocumentType == null || !"RUC".equalsIgnoreCase(customerDocumentType)) {
                String msg = messageSource.getMessage("billing.error.invoice.ruc.documentType", null, LocaleContextHolder.getLocale());
                return Result.failure(ApplicationError.validationError("customerDocumentType", msg));
            }
            if (customerDocumentNumber == null || !customerDocumentNumber.matches("^\\d{11}$")) {
                String msg = messageSource.getMessage("billing.error.invoice.ruc.number", null, LocaleContextHolder.getLocale());
                return Result.failure(ApplicationError.validationError("customerDocumentNumber", msg));
            }
            if (customerName == null || customerName.isBlank()) {
                String msg = messageSource.getMessage("billing.error.invoice.customerName", null, LocaleContextHolder.getLocale());
                return Result.failure(ApplicationError.validationError("customerName", msg));
            }
        }

        paymentCommandService.registerPaymentIntent(
                quote.getId(),
                type != null ? type : "RECEIPT",
                customerDocumentType != null ? customerDocumentType : "DNI",
                customerDocumentNumber != null ? customerDocumentNumber : "00000000",
                customerName != null ? customerName : "Cliente General",
                quote.getTotalAmount().amount()
        );

        PaymentProvider targetProvider = provider != null ? provider : PaymentProvider.MERCADO_PAGO;
        var strategy = paymentGatewayRegistry.resolve(targetProvider);
        var resultOpt = strategy.createCheckoutSession(
                quote.getTotalAmount().amount(),
                "PEN",
                "Cotizacion ShiftIQ #" + quote.getId().toString().substring(0, 8),
                quote.getId().toString()
        );

        if (resultOpt.isEmpty()) {
            String message = messageSource.getMessage("error.unexpected.message", null, LocaleContextHolder.getLocale());
            return Result.failure(ApplicationError.unexpected("preference", message));
        }

        return Result.success(resultOpt.get());
    }
}
