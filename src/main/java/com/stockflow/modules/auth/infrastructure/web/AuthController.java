package com.stockflow.modules.auth.infrastructure.web;

import com.stockflow.modules.auth.application.dto.*;
import com.stockflow.modules.auth.application.service.AuthService;
import com.stockflow.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 *
 * <p>Provides endpoints for:</p>
 * <ul>
 *   <li>User login</li>
 *   <li>Token refresh</li>
 *   <li>User logout</li>
 *   <li>Tenant signup</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and token management endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param request the login request
     * @return login response with tokens
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate with email and password to receive tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param request the refresh token request
     * @return refresh response with new tokens
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Obtain a new access token using a refresh token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out a user by revoking their refresh token.
     *
     * @param request the logout request
     * @return success response
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Revoke the refresh token to logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(
            ApiResponse.empty()
        );
    }

    /**
     * Registers a new tenant with an admin user.
     *
     * @param request the signup request
     * @return signup response
     */
    @PostMapping("/signup")
    @Operation(summary = "Tenant signup", description = "Register a new tenant and admin user")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
