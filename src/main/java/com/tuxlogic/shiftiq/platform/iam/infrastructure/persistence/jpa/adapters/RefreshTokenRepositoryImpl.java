package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.RefreshToken;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.RefreshTokenRepository;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers.RefreshTokenPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities.RefreshTokenPersistenceEntity;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.repositories.RefreshTokenPersistenceRepository;

import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenPersistenceRepository refreshTokenPersistenceRepository;

    public RefreshTokenRepositoryImpl(RefreshTokenPersistenceRepository refreshTokenPersistenceRepository) {
        this.refreshTokenPersistenceRepository = refreshTokenPersistenceRepository;
    }

    @Override
    public void save(RefreshToken token) {
        RefreshTokenPersistenceEntity entity;
        if (token.getId() != null) {
            entity = refreshTokenPersistenceRepository.findById(token.getId()).orElse(new RefreshTokenPersistenceEntity());
        } else {
            entity = new RefreshTokenPersistenceEntity();
        }

        RefreshTokenPersistenceAssembler.toEntity(token, entity);
        refreshTokenPersistenceRepository.save(entity);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenPersistenceRepository.findByTokenHash(tokenHash).map(RefreshTokenPersistenceAssembler::toDomain);
    }
}
