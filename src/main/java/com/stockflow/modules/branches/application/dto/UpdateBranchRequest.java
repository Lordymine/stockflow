package com.stockflow.modules.branches.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateBranchRequest(

    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    String name,

    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address,

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    String phone,

    @Size(max = 100, message = "Manager name must not exceed 100 characters")
    String managerName
) {
}
