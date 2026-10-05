package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.RefreshToken;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities.RefreshTokenPersistenceEntity;

public final class RefreshTokenPersistenceAssembler {

    private RefreshTokenPersistenceAssembler() {}

    public static RefreshTokenPersistenceEntity toEntity(RefreshToken token, RefreshTokenPersistenceEntity entity) {
        if (entity == null) {
            entity = new RefreshTokenPersistenceEntity();
        }
        entity.setId(token.getId());
        entity.setUserId(token.getUserId());
        entity.setTokenHash(token.getTokenHash());
        entity.setCreatedAt(token.getCreatedAt());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsedAt(token.getUsedAt());
        entity.setRevokedAt(token.getRevokedAt());
        return entity;
    }

    public static RefreshToken toDomain(RefreshTokenPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new RefreshToken(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getRevokedAt()
        );
    }
}
