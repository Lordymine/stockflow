package com.stockflow.modules.users.application.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    String name,

    String phone,

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password
) {
}
