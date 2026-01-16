package com.stockflow.modules.users.application.service;

import com.stockflow.modules.users.application.dto.*;
import com.stockflow.modules.users.domain.model.Role;
import com.stockflow.modules.users.domain.model.RoleEnum;
import com.stockflow.modules.users.domain.model.User;
import com.stockflow.modules.users.domain.model.UserRole;
import com.stockflow.modules.users.domain.repository.RoleRepository;
import com.stockflow.modules.users.domain.repository.UserRepository;
import com.stockflow.modules.users.domain.repository.UserRoleRepository;
import com.stockflow.shared.domain.exception.BadRequestException;
import com.stockflow.shared.domain.exception.ConflictException;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());

        Long tenantId = TenantContext.getTenantId();

        if (userRepository.existsByEmailAndTenantId(request.email(), tenantId)) {
            throw new ConflictException("User with email already exists in this tenant");
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(request.password());

        // Create user
        User user = new User();
        user.setTenantId(tenantId);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setPhone(request.phone());
        user.setIsActive(request.isActive() != null ? request.isActive() : true);
        user.setIsAccountLocked(false);
        user.setFailedLoginAttempts(0);

        user = userRepository.save(user);

        // Assign default STAFF role
        Role staffRole = roleRepository.findByNameAndTenantId(RoleEnum.STAFF, tenantId)
            .orElseThrow(() -> new BadRequestException("Default role STAFF not found"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(staffRole);
        userRole.setTenantId(tenantId);

        user.getUserRoles().add(userRole);
        user = userRepository.save(user);

        log.info("User created successfully with ID: {}", user.getId());
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));

        validateTenantAccess(user.getTenantId());

        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users for tenant");

        Long tenantId = TenantContext.getTenantId();
        List<User> users = userRepository.findByTenantId(tenantId);

        return users.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        log.debug("Fetching active users for tenant");

        Long tenantId = TenantContext.getTenantId();
        List<User> users = userRepository.findByTenantIdAndIsActiveTrue(tenantId);

        return users.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(RoleEnum role) {
        log.debug("Fetching users with role: {}", role);

        Long tenantId = TenantContext.getTenantId();
        List<User> users = userRepository.findByTenantIdAndRoleAndIsActive(tenantId, role);

        return users.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));

        validateTenantAccess(user.getTenantId());

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }

        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setPasswordChangedAt(java.time.LocalDateTime.now());
        }

        user = userRepository.save(user);

        log.info("User updated successfully");
        return mapToResponse(user);
    }

    @Transactional
    public void updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        log.info("Updating roles for user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        validateTenantAccess(user.getTenantId());

        Long tenantId = user.getTenantId();

        // Clear existing roles
        userRoleRepository.deleteByUserId(userId);
        user.getUserRoles().clear();

        // Add new roles
        for (String roleName : request.roles()) {
            try {
                RoleEnum enumName = RoleEnum.valueOf(roleName.toUpperCase());
                Role role = roleRepository.findByNameAndTenantId(enumName, tenantId)
                    .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));

                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(role);
                userRole.setTenantId(tenantId);

                user.getUserRoles().add(userRole);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role name: " + roleName);
            }
        }

        userRepository.save(user);
        log.info("User roles updated successfully");
    }

    @Transactional
    public void toggleUserActive(Long id, Boolean isActive) {
        log.info("Toggling active status for user ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));

        validateTenantAccess(user.getTenantId());

        user.setIsActive(isActive);
        userRepository.save(user);

        log.info("User active status updated to: {}", isActive);
    }

    @Transactional
    public void unlockUserAccount(Long id) {
        log.info("Unlocking account for user ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));

        validateTenantAccess(user.getTenantId());

        user.resetFailedLoginAttempts();
        userRepository.save(user);

        log.info("User account unlocked successfully");
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        validateTenantAccess(user.getTenantId());

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        log.info("Password changed successfully");
    }

    @Transactional
    public void updateUserBranches(Long userId, UpdateUserBranchesRequest request) {
        log.info("Updating branches for user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        validateTenantAccess(user.getTenantId());

        Long tenantId = user.getTenantId();

        // Clear existing branches
        user.getUserBranches().clear();

        // Add new branches
        for (Long branchId : request.branchIds()) {
            com.stockflow.modules.users.domain.model.UserBranch userBranch =
                new com.stockflow.modules.users.domain.model.UserBranch();
            userBranch.setUser(user);
            userBranch.setBranchId(branchId);
            userBranch.setTenantId(tenantId);

            user.getUserBranches().add(userBranch);
        }

        userRepository.save(user);
        log.info("User branches updated successfully");
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));

        validateTenantAccess(user.getTenantId());

        userRepository.delete(user);

        log.info("User deleted successfully");
    }

    private UserResponse mapToResponse(User user) {
        Set<String> roles = user.getUserRoles().stream()
            .map(ur -> ur.getRole().getName().name())
            .collect(Collectors.toSet());

        return new UserResponse(
            user.getId(),
            user.getTenantId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getIsActive(),
            user.getIsAccountLocked(),
            user.getFailedLoginAttempts(),
            user.getLastLoginAt(),
            roles,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    private void validateTenantAccess(Long userTenantId) {
        Long currentTenantId = TenantContext.getTenantId();

        if (!userTenantId.equals(currentTenantId)) {
            throw new BadRequestException("Access denied: User belongs to a different tenant");
        }
    }
}
