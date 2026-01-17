package com.stockflow.modules.users.infrastructure.web;

import com.stockflow.modules.users.application.dto.*;
import com.stockflow.modules.users.application.service.UserService;
import com.stockflow.shared.application.dto.ActiveRequest;
import com.stockflow.shared.application.dto.ApiResponse;
import com.stockflow.shared.application.dto.ItemsResponse;
import com.stockflow.shared.application.dto.PageMeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management operations.
 *
 * <p>Most endpoints require ADMIN role.</p>
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new user.
     * Requires ADMIN role.
     *
     * @param request the create user request
     * @return the created user response
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create user", description = "Create a new user in the current tenant (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /**
     * Gets a user by ID.
     * Requires ADMIN role.
     *
     * @param userId the user ID
     * @return the user response
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieve user information (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /**
     * Lists all users in the current tenant.
     * Requires ADMIN role.
     *
     * @param page page number (default: 0)
     * @param size page size (default: 20)
     * @param sortBy sort field (default: name)
     * @param sortDirection sort direction (default: ASC)
     * @return paginated list of users
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "List users", description = "List all users in the current tenant with pagination (ADMIN only)")
    public ResponseEntity<ApiResponse<ItemsResponse<UserResponse>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> response = userService.listUsers(pageable);
        return ResponseEntity.ok(
            ApiResponse.of(new ItemsResponse<>(response.getContent()), PageMeta.of(response))
        );
    }

    /**
     * Updates a user's active status.
     * Requires ADMIN role.
     *
     * @param userId the user ID
     * @param isActive the active status
     * @return the updated user response
     */
    @PatchMapping("/{userId}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update user active status", description = "Activate or deactivate a user (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserActiveStatus(
            @PathVariable Long userId,
            @Valid @RequestBody ActiveRequest request) {

        UserResponse response = userService.updateUserActiveStatus(userId, request.isActive());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /**
     * Updates user roles.
     * Requires ADMIN role.
     *
     * @param userId the user ID
     * @param request the update roles request
     * @return the updated user response
     */
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update user roles", description = "Set the roles for a user (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {

        UserResponse response = userService.updateUserRoles(userId, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /**
     * Updates user branch access.
     * Requires ADMIN role.
     *
     * @param userId the user ID
     * @param request the update branches request
     * @return the updated user response
     */
    @PutMapping("/{userId}/branches")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update user branches", description = "Set the branches a user can access (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserBranches(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserBranchesRequest request) {

        UserResponse response = userService.updateUserBranches(userId, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /**
     * Changes the current user's password.
     * Any authenticated user can change their own password.
     *
     * @param request the change password request
     * @return success response
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change the current user's password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(
            ApiResponse.empty()
        );
    }
}
