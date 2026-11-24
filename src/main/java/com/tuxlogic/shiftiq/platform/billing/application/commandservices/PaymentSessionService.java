package com.tuxlogic.shiftiq.platform.billing.application.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentCheckoutSessionResult;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;
import com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Application service orchestrating provider-agnostic virtual payment checkout sessions.
 */
public interface PaymentSessionService {

    Result<PaymentCheckoutSessionResult, ApplicationError> createCheckoutSession(
            UUID quoteId,
            PaymentProvider provider,
            String type,
            String customerDocumentType,
            String customerDocumentNumber,
            String customerName,
            Authentication authentication
    );
}
