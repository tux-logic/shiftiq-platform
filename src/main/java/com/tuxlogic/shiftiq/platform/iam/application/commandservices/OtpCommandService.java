package com.tuxlogic.shiftiq.platform.iam.application.commandservices;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SendOtpEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.VerifyOtpCodeCommand;

/**
 * Application Command Service for generating, sending, and validating OTP codes.
 */
public interface OtpCommandService {

    /**
     * Pre-checks the email deliverability, generates an OTP, and dispatches it via Brevo.
     *
     * @param command the send OTP command
     */
    void handle(SendOtpEmailCommand command);

    /**
     * Verifies the submitted OTP code against the active record for that email address.
     *
     * @param command the verification command
     * @return true if verified successfully
     */
    boolean handle(VerifyOtpCodeCommand command);
}
