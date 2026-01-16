package com.stockflow.modules.auth.application.dto;

public record RefreshTokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn
) {}
