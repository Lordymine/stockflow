package com.stockflow.modules.catalog.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for category operations.
 *
 * <p>Contains the data required to create or update a category.</p>
 */
@Schema(description = "Category request payload")
public record CategoryRequest(

    @Schema(description = "Category name", example = "Electronics", required = true)
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    String name
) {
}
