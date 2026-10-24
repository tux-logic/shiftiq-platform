package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.EmailOtpVerification;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.EmailOtpVerificationRepository;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers.EmailOtpVerificationPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.repositories.EmailOtpVerificationPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class EmailOtpVerificationRepositoryImpl implements EmailOtpVerificationRepository {

    private final EmailOtpVerificationPersistenceRepository persistenceRepository;

    public EmailOtpVerificationRepositoryImpl(EmailOtpVerificationPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public EmailOtpVerification save(EmailOtpVerification verification) {
        var entity = EmailOtpVerificationPersistenceAssembler.toPersistenceEntity(verification);
        var savedEntity = persistenceRepository.save(entity);
        return EmailOtpVerificationPersistenceAssembler.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<EmailOtpVerification> findById(UUID id) {
        return persistenceRepository.findById(id)
                .map(EmailOtpVerificationPersistenceAssembler::toDomainEntity);
    }

    @Override
    public Optional<EmailOtpVerification> findTopByEmailOrderByCreatedAtDesc(String email) {
        return persistenceRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .map(EmailOtpVerificationPersistenceAssembler::toDomainEntity);
    }
}
