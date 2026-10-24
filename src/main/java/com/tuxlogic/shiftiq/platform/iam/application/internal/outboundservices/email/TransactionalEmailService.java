package com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email;

/**
 * Port for sending transactional emails (such as OTP codes) via an external email provider.
 */
public interface TransactionalEmailService {

    /**
     * Sends an HTML transactional email to the recipient.
     *
     * @param toEmail     recipient email address
     * @param subject     subject line
     * @param htmlContent HTML body content
     * @return true if the email was accepted for delivery, false otherwise
     */
    boolean sendEmail(String toEmail, String subject, String htmlContent);
}
