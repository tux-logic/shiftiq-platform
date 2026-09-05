package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.UserRepository;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers.UserPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserPersistenceRepository userPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserRepositoryImpl(UserPersistenceRepository userPersistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.userPersistenceRepository = userPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void save(User user) {
        UserPersistenceEntity entity;
        if (user.getId() != null) {
            entity = userPersistenceRepository.findById(user.getId().value()).orElse(new UserPersistenceEntity());
        } else {
            entity = new UserPersistenceEntity();
        }
        
        UserPersistenceAssembler.toEntity(user, entity);
        userPersistenceRepository.save(entity);

        // Publish domain events post-save
        user.domainEvents().forEach(eventPublisher::publishEvent);
        user.clearDomainEvents();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userPersistenceRepository.findByIdAndNotDeleted(id).map(UserPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userPersistenceRepository.findByEmail(email).map(UserPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userPersistenceRepository.existsByEmail(email);
    }
}
