package com.stockflow.shared.infrastructure.security;

import com.stockflow.modules.users.domain.model.Role;
import com.stockflow.modules.users.domain.model.RoleEnum;
import com.stockflow.modules.users.domain.model.User;
import com.stockflow.modules.users.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 *
 * <p>This service loads user details from the database for authentication.
 * It's used by the authentication manager to verify credentials during login.</p>
 *
 * <p>The service loads the user with their roles and branch associations
 * to provide complete authorization data.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their email address.
     *
     * <p>This method is called by Spring Security during authentication.
     * It loads the user with their roles and branch associations.</p>
     *
     * @param email the user's email address
     * @return the user details
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);

        // Get tenant ID from context (set by AuthService before authentication)
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            logger.error("No tenant context available when loading user");
            throw new UsernameNotFoundException("No tenant context available");
        }

        // Load user with roles and branches
        User user = userRepository.findByEmailAndTenantId(email, tenantId)
            .orElseThrow(() -> {
                logger.warn("User not found: {} for tenant: {}", email, tenantId);
                return new UsernameNotFoundException(
                    String.format("User not found: %s for tenant: %d", email, tenantId));
            });

        if (!user.isActive()) {
            logger.warn("User is inactive: {} for tenant: {}", email, tenantId);
            throw new UsernameNotFoundException(
                String.format("User is inactive: %s", email));
        }

        // Convert roles to enum set
        Set<RoleEnum> roleEnums = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

        // Extract branch IDs
        var branchIds = user.getBranches().stream()
            .map(branch -> branch.getId())
            .toList();

        logger.debug("User loaded successfully: {} with roles: {} and {} branches",
            email, roleEnums, branchIds.size());

        return new CustomUserDetails(
            user.getId(),
            user.getTenantId(),
            user.getEmail(),
            user.getPasswordHash(),
            roleEnums,
            branchIds
        );
    }

    /**
     * Loads a user by their ID.
     * Useful for operations where you have the user ID but need full details.
     *
     * @param userId the user ID
     * @return the user details
     * @throws UsernameNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        logger.debug("Loading user by ID: {}", userId);

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            logger.error("No tenant context available when loading user by ID");
            throw new UsernameNotFoundException("No tenant context available");
        }

        User user = userRepository.findByIdWithRolesAndBranches(userId, tenantId)
            .orElseThrow(() -> {
                logger.warn("User not found: {} for tenant: {}", userId, tenantId);
                return new UsernameNotFoundException(
                    String.format("User not found: %d for tenant: %d", userId, tenantId));
            });

        if (!user.isActive()) {
            logger.warn("User is inactive: {} for tenant: {}", userId, tenantId);
            throw new UsernameNotFoundException(
                String.format("User is inactive: %d", userId));
        }

        Set<RoleEnum> roleEnums = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

        var branchIds = user.getBranches().stream()
            .map(branch -> branch.getId())
            .toList();

        logger.debug("User loaded by ID successfully: {} ({})", user.getEmail(), userId);

        return new CustomUserDetails(
            user.getId(),
            user.getTenantId(),
            user.getEmail(),
            user.getPasswordHash(),
            roleEnums,
            branchIds
        );
    }
}
