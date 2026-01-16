package com.stockflow.modules.branches.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BranchResponse(
    Long id,
    Long tenantId,
    String name,
    String code,
    String address,
    String phone,
    String managerName,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
