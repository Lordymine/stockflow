package com.stockflow.modules.branches.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateBranchRequest(

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    String name,

    @NotBlank(message = "Code is required")
    @Size(min = 2, max = 50, message = "Code must be between 2 and 50 characters")
    String code,

    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address,

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    String phone,

    @Size(max = 100, message = "Manager name must not exceed 100 characters")
    String managerName,

    Boolean isActive
) {
}
