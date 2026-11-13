package com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.services;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.WorkshopRole;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.DefaultRolePermissions;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.UserRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleRepository;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.jspecify.annotations.NonNull;

import java.util.Set;

@Service(value = "defaultUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final WorkshopRoleRepository workshopRoleRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, WorkshopRoleRepository workshopRoleRepository) {
        this.userRepository = userRepository;
        this.workshopRoleRepository = workshopRoleRepository;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        Set<Permission> effectivePermissions;
        if (user.getWorkshopRoleId() != null) {
            effectivePermissions = workshopRoleRepository.findById(user.getWorkshopRoleId())
                    .filter(WorkshopRole::isActive)
                    .map(WorkshopRole::getPermissions)
                    .orElseGet(() -> DefaultRolePermissions.permissionsFor(user.getRole()));
        } else {
            effectivePermissions = DefaultRolePermissions.permissionsFor(user.getRole());
        }

        return UserDetailsImpl.build(user, effectivePermissions);
    }
}
