package com.stockflow.modules.auth.application.service;

import com.stockflow.modules.auth.application.dto.*;

/**
 * Service interface for authentication operations.
 *
 * <p>Provides use cases for:</p>
 * <ul>
 *   <li>User login with email and password</li>
 *   <li>Token refresh using refresh token</li>
 *   <li>User logout (refresh token revocation)</li>
 *   <li>System bootstrap (initial tenant and admin creation)</li>
 * </ul>
 */
public interface AuthService {

    /**
     * Authenticates a user with email and password.
     *
     * @param request the login request containing credentials
     * @return login response with tokens and user info
     */
    LoginResponse login(LoginRequest request);

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param request the refresh token request
     * @return refresh response with new tokens
     */
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logs out a user by revoking their refresh token.
     *
     * @param request the logout request containing refresh token
     */
    void logout(LogoutRequest request);

    /**
     * Bootstraps the system with initial tenant and admin user.
     * Only valid when no tenants exist yet.
     *
     * @param request the bootstrap request
     * @return bootstrap response with created entities and tokens
     */
    BootstrapResponse bootstrap(BootstrapRequest request);
}
