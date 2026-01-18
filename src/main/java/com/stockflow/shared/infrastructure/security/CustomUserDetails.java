package com.stockflow.shared.infrastructure.security;

import com.stockflow.modules.users.domain.model.RoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Custom implementation of Spring Security's UserDetails interface.
 *
 * <p>
 * This class wraps user information extracted from the JWT token and provides
 * the security context with the necessary authentication and authorization
 * data.
 * </p>
 *
 * <p>
 * Unlike the default UserDetailsService implementation, this is a lightweight
 * wrapper that doesn't require database access on every request.
 * </p>
 */
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final Long tenantId;
    private final String email;
    private final String password;
    private final Set<RoleEnum> roles;
    private final List<Long> branchIds;

    /**
     * Constructor for creating custom user details.
     *
     * @param userId    the user ID
     * @param tenantId  the tenant ID
     * @param email     the user's email
     * @param password  the password hash (may be null for JWT auth)
     * @param roles     set of user roles
     * @param branchIds list of branch IDs the user has access to
     */
    public CustomUserDetails(Long userId, Long tenantId, String email, String password,
            Set<RoleEnum> roles, List<Long> branchIds) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.branchIds = branchIds;
    }

    /**
     * Returns the authorities (roles) granted to the user.
     * Roles are prefixed with "ROLE_" as required by Spring Security.
     *
     * @return collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Custom getters

    public Long getUserId() {
        return userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getEmail() {
        return email;
    }

    public Set<RoleEnum> getRoles() {
        return roles;
    }

    public List<Long> getBranchIds() {
        return branchIds;
    }

    /**
     * Checks if the user has a specific role.
     *
     * @param role the role to check
     * @return true if the user has the role
     */
    public boolean hasRole(RoleEnum role) {
        return roles.contains(role);
    }

    /**
     * Checks if the user has access to a specific branch.
     *
     * @param branchId the branch ID
     * @return true if the user has access
     */
    public boolean hasAccessToBranch(Long branchId) {
        // ADMIN has access to all branches
        if (roles.contains(RoleEnum.ADMIN)) {
            return true;
        }
        return branchIds.contains(branchId);
    }
}
