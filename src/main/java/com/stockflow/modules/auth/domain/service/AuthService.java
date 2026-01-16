package com.stockflow.modules.auth.domain.service;

import com.stockflow.modules.auth.application.dto.LoginRequest;
import com.stockflow.modules.auth.application.dto.LoginResponse;
import com.stockflow.modules.auth.application.dto.LogoutRequest;
import com.stockflow.modules.auth.application.dto.RefreshTokenRequest;
import com.stockflow.modules.auth.application.dto.RefreshTokenResponse;

public interface AuthService {

    /**
     * Autentica usuário e retorna tokens
     */
    LoginResponse login(LoginRequest request);

    /**
     * Renova access token usando refresh token
     */
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    /**
     * Revoga refresh token (logout)
     */
    void logout(LogoutRequest request);
}
