package com.stockflow.modules.users.infrastructure.web;

import com.stockflow.modules.users.application.dto.*;
import com.stockflow.modules.users.application.service.UserService;
import com.stockflow.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User Management API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {
        log.info("Creating user with email: {}", request.email());
        UserResponse response = userService.createUser(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "List users", description = "Get all users for current tenant (ADMIN, MANAGER)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("Fetching all users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @GetMapping("/active")
    @Operation(summary = "List active users", description = "Get all active users for current tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsers() {
        log.info("Fetching active users");
        List<UserResponse> users = userService.getActiveUsers();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "List users by role", description = "Get users by specific role")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable String role) {
        log.info("Fetching users with role: {}", role);
        try {
            com.stockflow.modules.users.domain.model.RoleEnum roleEnum =
                com.stockflow.modules.users.domain.model.RoleEnum.valueOf(role.toUpperCase());
            List<UserResponse> users = userService.getUsersByRole(roleEnum);
            return ResponseEntity.ok(ApiResponse.ok(users));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(null, "Invalid role: " + role));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user details by ID (ADMIN, MANAGER)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user details (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        log.info("Updating user with ID: {}", id);
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Update user roles", description = "Update user roles (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRoles(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        log.info("Updating roles for user ID: {}", id);
        userService.updateUserRoles(id, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/branches")
    @Operation(summary = "Update user branches", description = "Update user branch access (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserBranches(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserBranchesRequest request
    ) {
        log.info("Updating branches for user ID: {}", id);
        userService.updateUserBranches(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Toggle user active status", description = "Activate or deactivate user (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleUserActive(
        @PathVariable Long id,
        @RequestParam Boolean isActive
    ) {
        log.info("Toggling active status for user ID: {}", id);
        userService.toggleUserActive(id, isActive);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/unlock")
    @Operation(summary = "Unlock user account", description = "Unlock a locked user account (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unlockUserAccount(@PathVariable Long id) {
        log.info("Unlocking account for user ID: {}", id);
        userService.unlockUserAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Change user password", description = "Change user password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
        @PathVariable Long id,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        log.info("Changing password for user ID: {}", id);
        userService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete user (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
