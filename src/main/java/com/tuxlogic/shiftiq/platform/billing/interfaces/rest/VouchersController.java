package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherCommandFailure;
import com.tuxlogic.shiftiq.platform.billing.application.commandservices.VoucherCommandService;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.GenerateVoucherResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.AddPaymentResource;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.AddPaymentCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentMethod;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.transform.GenerateVoucherCommandFromResourceAssembler;
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

import com.tuxlogic.shiftiq.platform.billing.application.queryservices.VoucherQueryService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetVoucherByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.VoucherResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.UUID;
import java.util.List;

import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * REST controller for managing billing vouchers (Invoices and Receipts).
 * Exposes endpoints for voucher generation, payment processing, and checkout workflows.
 * Serves as the primary entry point for HTTP requests targeting the billing system's core capabilities.
 */
@RestController
@RequestMapping(value = "/api/v1/vouchers", produces = "application/json")
@Tag(name = "Vouchers", description = "Endpoints for generating and managing billing vouchers (invoices and receipts)")
@PreAuthorize("isAuthenticated()")
public class VouchersController {

    private final VoucherCommandService commandService;
    private final VoucherQueryService queryService;
    private final QuoteQueryService quoteQueryService;
    private final org.springframework.context.MessageSource messageSource;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public VouchersController(VoucherCommandService commandService, VoucherQueryService queryService, QuoteQueryService quoteQueryService, org.springframework.context.MessageSource messageSource, MultiTenancySecurityService multiTenancySecurityService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.quoteQueryService = quoteQueryService;
        this.messageSource = messageSource;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @PostMapping
    @Operation(summary = "Generate a new voucher", description = "Generates a new Voucher (Invoice/Receipt) based on an Approved Quote and sends it to SUNAT via Facthub")
    public ResponseEntity<?> generateVoucher(@Valid @RequestBody GenerateVoucherResource resource) {
        var quote = quoteQueryService.handle(new com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery(resource.quoteId()));
        quote.ifPresent(q -> multiTenancySecurityService.validateBranchAccess(q.getBranchId().value()));

        var command = GenerateVoucherCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        
        if (result.isSuccess()) {
            var voucherResource = VoucherResourceFromAggregateAssembler.toResourceFromAggregate(result.success().get());
            return new ResponseEntity<>(voucherResource, HttpStatus.CREATED);
        }
        
        return toErrorResponse(result.failure().get());
    }

    private com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher validateVoucherAccess(UUID voucherId) {
        var voucher = queryService.handle(new GetVoucherByIdQuery(voucherId))
                .orElseThrow(() -> new IllegalArgumentException("billing.error.voucher.notFound"));
        var quote = quoteQueryService.handle(new com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery(voucher.getQuoteId()));
        quote.ifPresent(q -> multiTenancySecurityService.validateBranchAccess(q.getBranchId().value()));
        return voucher;
    }

    @GetMapping("/{voucherId}")
    @Operation(summary = "Get voucher by ID", description = "Retrieves a voucher using its unique identifier")
    public ResponseEntity<VoucherResource> getVoucherById(@PathVariable UUID voucherId) {
        validateVoucherAccess(voucherId);
        var query = new GetVoucherByIdQuery(voucherId);
        var voucher = queryService.handle(query);
        
        if (voucher.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        var voucherResource = VoucherResourceFromAggregateAssembler.toResourceFromAggregate(voucher.get());
        return ResponseEntity.ok(voucherResource);
    }

    @GetMapping(params = "branchId")
    @Operation(summary = "Get vouchers by branch", description = "Retrieves all vouchers emitted in a specific branch")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")
    public ResponseEntity<List<VoucherResource>> getVouchersByBranch(@RequestParam UUID branchId) {
        var query = new com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetVouchersByBranchIdQuery(new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId(branchId));
        var vouchers = queryService.handle(query);
        
        var voucherResources = vouchers.stream()
                .map(VoucherResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();
                
        return ResponseEntity.ok(voucherResources);
    }

    @PostMapping("/{voucherId}/payments")
    @Operation(summary = "Add a payment to a voucher", description = "Records a partial or full payment for a given voucher")
    public ResponseEntity<?> addPayment(@PathVariable UUID voucherId, @Valid @RequestBody AddPaymentResource resource) {
        validateVoucherAccess(voucherId);
        PaymentMethod paymentMethod;
        try {
            paymentMethod = resource.method() != null ? PaymentMethod.valueOf(resource.method().toUpperCase()) : PaymentMethod.CASH;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid payment method");
        }

        var command = new AddPaymentCommand(
                voucherId,
                new Money(resource.amount()),
                paymentMethod
        );

        var result = commandService.handle(command);

        if (result.isSuccess()) {
            var voucherResource = VoucherResourceFromAggregateAssembler.toResourceFromAggregate(result.success().get());
            return ResponseEntity.ok(voucherResource);
        }

        return toErrorResponse(result.failure().get());
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{voucherId}/payments/{paymentId}")
    @Operation(summary = "Remove a payment from a voucher", description = "Deletes a previously recorded payment and updates the voucher's balance and status")
    public ResponseEntity<?> removePayment(@PathVariable UUID voucherId, @PathVariable UUID paymentId) {
        validateVoucherAccess(voucherId);
        var command = new com.tuxlogic.shiftiq.platform.billing.domain.model.commands.RemovePaymentCommand(voucherId, paymentId);
        
        var result = commandService.handle(command);

        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        }

        return toErrorResponse(result.failure().get());
    }

    private ResponseEntity<?> toErrorResponse(VoucherCommandFailure failure) {
        return switch (failure) {
            case QUOTE_NOT_FOUND -> {
                String message = messageSource.getMessage("billing.error.quote.notFound", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.notFound("voucher", message));
            }
            case QUOTE_NOT_APPROVED -> {
                String message = messageSource.getMessage("billing.error.voucher.notApproved", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.conflict("voucher", message));
            }
            case INVALID_VOUCHER_DATA -> {
                String message = messageSource.getMessage("billing.error.voucher.invalidData", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.validationError("voucher", message));
            }
            case ISSUER_NOT_FOUND -> {
                String message = messageSource.getMessage("billing.error.voucher.issuerNotFound", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.unexpected("voucher", message));
            }
            case FACTOS_ISSUANCE_FAILED -> {
                String message = messageSource.getMessage("billing.error.voucher.facthubFailed", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.unexpected("voucher", message));
            }
            case VOUCHER_NOT_FOUND -> {
                String message = messageSource.getMessage("billing.error.voucher.notFound", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.notFound("voucher", message));
            }
            case VOUCHER_ALREADY_PAID -> {
                String message = messageSource.getMessage("billing.error.voucher.alreadyPaidInFull", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.conflict("voucher", message));
            }
            case VOUCHER_CANCELED -> {
                String message = messageSource.getMessage("billing.error.voucher.cannotAddPaymentCanceled", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.conflict("voucher", message));
            }
            case PAYMENT_EXCEEDS_TOTAL_DEBT -> {
                String message = messageSource.getMessage("billing.error.voucher.paymentExceedsDebt", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.validationError("voucher", message));
            }
            case PAYMENT_NOT_FOUND -> {
                String message = messageSource.getMessage("billing.error.payment.notFound", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
                yield com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler.toErrorResponseFromApplicationError(com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError.notFound("voucher", message));
            }
        };
    }
}
