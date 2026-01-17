package com.stockflow.shared.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

/**
 * Pagination metadata for list responses.
 */
@Schema(description = "Pagination metadata")
public record PageMeta(
    @Schema(description = "Current page number (0-indexed)", example = "0")
    int page,
    @Schema(description = "Page size", example = "20")
    int size,
    @Schema(description = "Total number of items", example = "100")
    long totalItems,
    @Schema(description = "Total number of pages", example = "5")
    int totalPages
) {
    public static PageMeta of(Page<?> page) {
        return new PageMeta(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
