package com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.model;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final UUID id;
    private final String username;
    @JsonIgnore
    private final String password;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Set<UUID> branchIds;

    public UserDetailsImpl(UUID id, String username, String password, Collection<? extends GrantedAuthority> authorities, Set<UUID> branchIds, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.branchIds = branchIds != null ? branchIds : Collections.emptySet();
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = enabled;
    }

    public UserDetailsImpl(UUID id, String username, String password, Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this(id, username, password, authorities, Collections.emptySet(), enabled);
    }

    public static UserDetailsImpl build(User user) {
        String roleName = user.getRole() != null ? user.getRole().name() : "ROLE_USER";
        GrantedAuthority authority = new SimpleGrantedAuthority(roleName);
        boolean enabled = user.getStatus() == UserStatus.ACTIVE;

        return new UserDetailsImpl(
                user.getId().value(),
                user.getEmail().value(),
                user.getPassword().value(),
                Collections.singletonList(authority),
                user.getBranchIds(),
                enabled
        );
    }
}
