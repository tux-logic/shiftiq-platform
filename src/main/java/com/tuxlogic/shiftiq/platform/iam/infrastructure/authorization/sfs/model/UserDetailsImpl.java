package com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.model;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.DefaultRolePermissions;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserStatus;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class UserDetailsImpl implements UserDetails, AuthenticatedPrincipal {

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

    @Override
    public boolean hasRole(String role) {
        return authorities != null && authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    @Override
    public boolean hasPermission(String permission) {
        return permission != null && authorities != null && authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission));
    }

    @Override
    public Set<String> getPermissions() {
        if (authorities == null) {
            return Collections.emptySet();
        }
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean hasBranch(UUID branchId) {
        return branchId != null && branchIds != null && branchIds.contains(branchId);
    }

    @Override
    public Set<UUID> getBranchIds() {
        return Collections.unmodifiableSet(branchIds != null ? branchIds : Collections.emptySet());
    }

    public static UserDetailsImpl build(User user) {
        return build(user, DefaultRolePermissions.permissionsFor(user.getRole()));
    }

    public static UserDetailsImpl build(User user, Set<Permission> effectivePermissions) {
        String roleName = user.getRole() != null ? user.getRole().name() : "ROLE_USER";
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(roleName));
        if (effectivePermissions != null) {
            effectivePermissions.stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.name()))
                    .forEach(grantedAuthorities::add);
        }
        boolean enabled = user.getStatus() == UserStatus.ACTIVE;

        return new UserDetailsImpl(
                user.getId().value(),
                user.getEmail().value(),
                user.getPassword().value(),
                Collections.unmodifiableList(grantedAuthorities),
                user.getBranchIds(),
                enabled
        );
    }
}
