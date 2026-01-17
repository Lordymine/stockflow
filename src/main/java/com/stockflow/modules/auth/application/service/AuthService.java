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
 *   <li>Tenant signup (company and admin creation)</li>
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
     * Registers a new tenant with an initial admin user.
     *
     * @param request the signup request
     * @return signup response with created entities and tokens
     */
    SignupResponse signup(SignupRequest request);
}
