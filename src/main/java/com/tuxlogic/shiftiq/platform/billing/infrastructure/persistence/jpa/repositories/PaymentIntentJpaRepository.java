package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentIntentJpaRepository extends JpaRepository<PaymentIntentPersistenceEntity, UUID> {
    Optional<PaymentIntentPersistenceEntity> findByQuoteId(UUID quoteId);
}
