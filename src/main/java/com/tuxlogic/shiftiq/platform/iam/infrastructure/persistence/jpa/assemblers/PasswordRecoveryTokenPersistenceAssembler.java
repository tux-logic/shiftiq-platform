package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.PasswordRecoveryToken;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities.PasswordRecoveryTokenPersistenceEntity;

public final class PasswordRecoveryTokenPersistenceAssembler {

    private PasswordRecoveryTokenPersistenceAssembler() {}

    public static PasswordRecoveryTokenPersistenceEntity toEntity(PasswordRecoveryToken token, PasswordRecoveryTokenPersistenceEntity entity) {
        if (entity == null) {
            entity = new PasswordRecoveryTokenPersistenceEntity();
        }
        entity.setId(token.getId());
        entity.setTokenHash(token.getTokenHash());
        entity.setCreatedAt(token.getCreatedAt());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsed(token.isUsed());
        entity.setUserId(token.getUserId());
        return entity;
    }

    public static PasswordRecoveryToken toDomain(PasswordRecoveryTokenPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PasswordRecoveryToken(
                entity.getId(),
                entity.getTokenHash(),
                entity.getUserId(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.isUsed()
        );
    }
}
