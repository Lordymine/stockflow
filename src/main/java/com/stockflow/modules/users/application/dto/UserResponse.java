package com.stockflow.modules.users.application.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
    Long id,
    Long tenantId,
    String name,
    String email,
    String phone,
    Boolean isActive,
    Boolean isAccountLocked,
    Integer failedLoginAttempts,
    LocalDateTime lastLoginAt,
    Set<String> roles,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
