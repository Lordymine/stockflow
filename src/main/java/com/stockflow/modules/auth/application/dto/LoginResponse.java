package com.stockflow.modules.auth.application.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    Long userId,
    Long tenantId,
    String email
) {}
