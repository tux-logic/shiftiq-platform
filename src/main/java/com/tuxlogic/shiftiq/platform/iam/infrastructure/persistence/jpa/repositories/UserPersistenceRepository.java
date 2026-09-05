package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPersistenceRepository extends JpaRepository<UserPersistenceEntity, UUID> {

    @Query("SELECT u FROM UserPersistenceEntity u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<UserPersistenceEntity> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM UserPersistenceEntity u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM UserPersistenceEntity u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<UserPersistenceEntity> findByIdAndNotDeleted(@Param("id") UUID id);
}
