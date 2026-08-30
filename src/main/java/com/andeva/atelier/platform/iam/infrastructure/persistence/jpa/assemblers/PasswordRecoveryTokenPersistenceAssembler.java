package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iam.domain.model.entities.PasswordRecoveryToken;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.PasswordRecoveryTokenPersistenceEntity;

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
        PasswordRecoveryToken token = new PasswordRecoveryToken();
        token.setId(entity.getId());
        token.setTokenHash(entity.getTokenHash());
        token.setCreatedAt(entity.getCreatedAt());
        token.setExpiresAt(entity.getExpiresAt());
        token.setUsed(entity.isUsed());
        token.setUserId(entity.getUserId());
        return token;
    }
}
