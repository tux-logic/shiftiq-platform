package com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.iam.domain.model.events.UserDeactivatedEvent;
import com.tuxlogic.shiftiq.platform.iam.domain.model.events.UserEmailChangedEvent;
import com.tuxlogic.shiftiq.platform.iam.domain.model.events.UserPasswordChangedEvent;
import com.tuxlogic.shiftiq.platform.iam.domain.model.events.UserSignedUpEvent;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.GoogleId;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Roles;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User aggregate root.
 * Represents an authentication account in the system.
 */
@Getter
public class User extends AbstractDomainAggregateRoot<User> {

    private UserId id;
    private EmailAddress email;
    private Password password;
    private GoogleId googleId;
    private UserStatus status;
    private Roles role;
    private Set<UUID> branchIds = new HashSet<>();
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Long version;

    public User() {
        this.id = new UserId(UUID.randomUUID());
        this.status = UserStatus.ACTIVE;
        this.role = Roles.ROLE_USER;
        this.branchIds = new HashSet<>();
    }

    public User(UserId id, EmailAddress email, Password password, GoogleId googleId, UserStatus status, Roles role, Set<UUID> branchIds, Instant createdAt, Instant updatedAt, Instant deletedAt, Long version) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.googleId = googleId;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.role = role != null ? role : Roles.ROLE_USER;
        this.branchIds = branchIds != null ? new HashSet<>(branchIds) : new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public User(UserId id, EmailAddress email, Password password, GoogleId googleId, UserStatus status, Roles role, Instant createdAt, Instant updatedAt, Instant deletedAt, Long version) {
        this(id, email, password, googleId, status, role, null, createdAt, updatedAt, deletedAt, version);
    }

    public User(EmailAddress email, Password password) {
        this();
        if (email == null) {
            throw new IllegalArgumentException("iam.error.email.required");
        }
        if (password == null) {
            throw new IllegalArgumentException("iam.error.password.required");
        }
        this.email = email;
        this.password = password;
        this.registerDomainEvent(new UserSignedUpEvent(this, this.id.value(), this.email.value()));
    }

    public User(EmailAddress email, Password password, GoogleId googleId) {
        this(email, password);
        this.googleId = googleId;
    }

    public Roles getRole() {
        return this.role != null ? this.role : Roles.ROLE_USER;
    }

    public Set<UUID> getBranchIds() {
        return Collections.unmodifiableSet(this.branchIds != null ? this.branchIds : Collections.emptySet());
    }

    public void assignRole(Roles role) {
        if (role == null) {
            throw new IllegalArgumentException("iam.error.role.required");
        }
        this.role = role;
    }

    public void assignBranch(UUID branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("iam.error.branchId.required");
        }
        if (this.branchIds == null) {
            this.branchIds = new HashSet<>();
        }
        this.branchIds.add(branchId);
    }

    public void removeBranch(UUID branchId) {
        if (branchId != null && this.branchIds != null) {
            this.branchIds.remove(branchId);
        }
    }

    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            return;
        }
        this.status = UserStatus.INACTIVE;
        this.deletedAt = Instant.now();
        this.registerDomainEvent(new UserDeactivatedEvent(this, this.id.value()));
    }

    public void changePassword(Password newPassword) {
        if (newPassword == null || newPassword.value() == null || newPassword.value().isBlank()) {
            throw new IllegalArgumentException("iam.error.password.required");
        }
        if (this.password != null && this.password.value().equals(newPassword.value())) {
            throw new IllegalArgumentException("iam.error.password.sameAsCurrent");
        }
        this.password = newPassword;
        this.registerDomainEvent(new UserPasswordChangedEvent(this, this.id.value()));
    }

    public void changeEmail(EmailAddress newEmail) {
        if (newEmail == null || newEmail.value() == null || newEmail.value().isBlank()) {
            throw new IllegalArgumentException("iam.error.email.required");
        }
        String oldEmail = this.email != null ? this.email.value() : null;
        this.email = newEmail;
        this.registerDomainEvent(new UserEmailChangedEvent(this, this.id.value(), oldEmail, newEmail.value()));
    }

    public void linkGoogleAccount(GoogleId googleId) {
        if (googleId == null || googleId.value() == null || googleId.value().isBlank()) {
            throw new IllegalArgumentException("iam.error.googleId.required");
        }
        this.googleId = googleId;
    }
}
