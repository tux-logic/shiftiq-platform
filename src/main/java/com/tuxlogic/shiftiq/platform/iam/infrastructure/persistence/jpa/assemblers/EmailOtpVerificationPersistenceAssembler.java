package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.EmailOtpVerification;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities.EmailOtpVerificationPersistenceEntity;

public class EmailOtpVerificationPersistenceAssembler {

    public static EmailOtpVerificationPersistenceEntity toPersistenceEntity(EmailOtpVerification domain) {
        if (domain == null) return null;
        var entity = new EmailOtpVerificationPersistenceEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setOtpHash(domain.getOtpHash());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setAttempts(domain.getAttempts());
        entity.setMaxAttempts(domain.getMaxAttempts());
        entity.setVerified(domain.isVerified());
        entity.setConsumed(domain.isConsumed());
        return entity;
    }

    public static EmailOtpVerification toDomainEntity(EmailOtpVerificationPersistenceEntity entity) {
        if (entity == null) return null;
        return new EmailOtpVerification(
                entity.getId(),
                entity.getEmail(),
                entity.getOtpHash(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getAttempts(),
                entity.getMaxAttempts(),
                entity.isVerified(),
                entity.isConsumed()
        );
    }
}
