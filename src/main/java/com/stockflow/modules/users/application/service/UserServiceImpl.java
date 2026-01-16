package com.stockflow.modules.users.application.service;

import com.stockflow.modules.users.application.dto.*;
import com.stockflow.modules.users.domain.model.Branch;
import com.stockflow.modules.users.domain.model.Role;
import com.stockflow.modules.users.domain.model.RoleEnum;
import com.stockflow.modules.users.domain.model.User;
import com.stockflow.modules.users.domain.repository.BranchRepository;
import com.stockflow.modules.users.domain.repository.RoleRepository;
import com.stockflow.modules.users.domain.repository.UserRepository;
import com.stockflow.shared.application.dto.PaginationResponse;
import com.stockflow.shared.domain.exception.ConflictException;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.domain.exception.ValidationException;
import com.stockflow.shared.infrastructure.security.CustomUserDetails;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of user management service.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                          RoleRepository roleRepository,
                          BranchRepository branchRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        Long tenantId = TenantContext.requireTenantId();

        // Check if email already exists
        if (userRepository.existsByEmailAndTenantId(request.email(), tenantId)) {
            throw new ConflictException("USER_EMAIL_ALREADY_EXISTS",
                "User with this email already exists in the tenant");
        }

        // Create user
        User user = new User(
            tenantId,
            request.name(),
            request.email(),
            passwordEncoder.encode(request.password())
        );

        // Assign roles
        if (request.roles() != null && !request.roles().isEmpty()) {
            for (String roleName : request.roles()) {
                RoleEnum roleEnum = RoleEnum.valueOf(roleName.toUpperCase());
                Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new ValidationException("VALIDATION_ERROR",
                        "Role not found: " + roleName));
                user.addRole(role);
            }
        } else {
            // Default role: STAFF
            Role staffRole = roleRepository.findByName(RoleEnum.STAFF)
                .orElseThrow(() -> new ValidationException("VALIDATION_ERROR",
                    "STAFF role not found"));
            user.addRole(staffRole);
        }

        // Assign branches
        if (request.branchIds() != null && !request.branchIds().isEmpty()) {
            for (Long branchId : request.branchIds()) {
                Branch branch = branchRepository.findByIdAndTenantId(branchId, tenantId)
                    .orElseThrow(() -> new NotFoundException("BRANCH_NOT_FOUND",
                        "Branch not found: " + branchId));
                user.addBranch(branch);
            }
        }

        user = userRepository.save(user);
        logger.info("Created user: {} in tenant: {}", user.getEmail(), tenantId);

        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        Long tenantId = TenantContext.requireTenantId();

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND",
                "User not found with ID: " + userId));

        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserResponse> listUsers(Pageable pageable) {
        Long tenantId = TenantContext.requireTenantId();

        Page<User> users = userRepository.findAllByTenantId(tenantId, pageable);

        List<UserResponse> userResponses = users.getContent().stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());

        return new PaginationResponse<>(
            userResponses,
            users.getTotalElements(),
            users.getNumber(),
            users.getTotalPages(),
            users.getSize()
        );
    }

    @Override
    @Transactional
    public UserResponse updateUserActiveStatus(Long userId, Boolean isActive) {
        Long tenantId = TenantContext.requireTenantId();

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND",
                "User not found with ID: " + userId));

        user.setActive(isActive);
        user = userRepository.save(user);

        logger.info("Updated user {} active status to: {}", user.getEmail(), isActive);

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        Long tenantId = TenantContext.requireTenantId();

        User user = userRepository.findByIdWithRolesAndBranches(userId, tenantId)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND",
                "User not found with ID: " + userId));

        // Clear existing roles
        user.setRoles(Set.of());

        // Add new roles
        for (String roleName : request.roles()) {
            RoleEnum roleEnum;
            try {
                roleEnum = RoleEnum.valueOf(roleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("VALIDATION_ERROR",
                    "Invalid role: " + roleName);
            }

            Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new ValidationException("VALIDATION_ERROR",
                    "Role not found: " + roleName));
            user.addRole(role);
        }

        user = userRepository.save(user);

        logger.info("Updated roles for user: {} to: {}", user.getEmail(), request.roles());

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserBranches(Long userId, UpdateUserBranchesRequest request) {
        Long tenantId = TenantContext.requireTenantId();

        User user = userRepository.findByIdWithRolesAndBranches(userId, tenantId)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND",
                "User not found with ID: " + userId));

        // Clear existing branches
        user.setBranches(Set.of());

        // Add new branches
        for (Long branchId : request.branchIds()) {
            Branch branch = branchRepository.findByIdAndTenantId(branchId, tenantId)
                .orElseThrow(() -> new NotFoundException("BRANCH_NOT_FOUND",
                    "Branch not found: " + branchId));
            user.addBranch(branch);
        }

        user = userRepository.save(user);

        logger.info("Updated branches for user: {} to: {}", user.getEmail(), request.branchIds());

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // Get current user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ValidationException("VALIDATION_ERROR",
                "Could not identify current user");
        }

        Long tenantId = TenantContext.requireTenantId();
        User user = userRepository.findByIdAndTenantId(userDetails.getUserId(), tenantId)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND",
                "User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ValidationException("AUTH_INVALID_CREDENTIALS",
                "Current password is incorrect");
        }

        // Set new password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        logger.info("Password changed for user: {}", user.getEmail());
    }

    // Private helper methods

    private UserResponse mapToUserResponse(User user) {
        // Reload user with roles and branches if not already loaded
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user = userRepository.findByIdWithRolesAndBranches(user.getId(), user.getTenantId())
                .orElse(user);
        }

        List<String> roleNames = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toList());

        List<Long> branchIds = user.getBranches().stream()
            .map(Branch::getId)
            .collect(Collectors.toList());

        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getIsActive(),
            roleNames,
            branchIds,
            user.getCreatedAt()
        );
    }
}
