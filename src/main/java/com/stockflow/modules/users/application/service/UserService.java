package com.stockflow.modules.users.application.service;

import com.stockflow.modules.users.application.dto.ChangePasswordRequest;
import com.stockflow.modules.users.application.dto.CreateUserRequest;
import com.stockflow.modules.users.application.dto.UpdateUserBranchesRequest;
import com.stockflow.modules.users.application.dto.UpdateUserRolesRequest;
import com.stockflow.modules.users.application.dto.UserResponse;
import com.stockflow.shared.application.dto.PaginationResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for user management operations.
 */
public interface UserService {

    /**
     * Creates a new user in the current tenant.
     *
     * @param request the create user request
     * @return the created user response
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Gets a user by ID.
     *
     * @param userId the user ID
     * @return the user response
     */
    UserResponse getUserById(Long userId);

    /**
     * Lists all users in the current tenant with pagination.
     *
     * @param pageable the pagination parameters
     * @return paginated list of users
     */
    PaginationResponse<UserResponse> listUsers(Pageable pageable);

    /**
     * Updates a user's active status.
     *
     * @param userId the user ID
     * @param isActive the active status
     * @return the updated user response
     */
    UserResponse updateUserActiveStatus(Long userId, Boolean isActive);

    /**
     * Updates user roles.
     *
     * @param userId the user ID
     * @param request the update roles request
     * @return the updated user response
     */
    UserResponse updateUserRoles(Long userId, UpdateUserRolesRequest request);

    /**
     * Updates user branch access.
     *
     * @param userId the user ID
     * @param request the update branches request
     * @return the updated user response
     */
    UserResponse updateUserBranches(Long userId, UpdateUserBranchesRequest request);

    /**
     * Changes user password.
     *
     * @param request the change password request
     */
    void changePassword(ChangePasswordRequest request);
}
