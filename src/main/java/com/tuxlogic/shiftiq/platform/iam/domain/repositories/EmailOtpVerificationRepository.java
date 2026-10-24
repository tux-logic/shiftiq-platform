package com.tuxlogic.shiftiq.platform.iam.domain.repositories;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.EmailOtpVerification;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for EmailOtpVerification domain entities.
 */
public interface EmailOtpVerificationRepository {

    EmailOtpVerification save(EmailOtpVerification verification);

    Optional<EmailOtpVerification> findById(UUID id);

    Optional<EmailOtpVerification> findTopByEmailOrderByCreatedAtDesc(String email);
}
