package com.stockflow.modules.users.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record UpdateUserBranchesRequest(

    @NotEmpty(message = "At least one branch is required")
    List<@Positive(message = "Branch ID must be positive") Long> branchIds
) {
}
