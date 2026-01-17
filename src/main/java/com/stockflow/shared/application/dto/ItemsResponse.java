package com.stockflow.shared.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Wrapper for list responses to keep a consistent API shape.
 *
 * @param <T> item type
 */
@Schema(description = "List response wrapper")
public record ItemsResponse<T>(
    @Schema(description = "List of items")
    List<T> items
) {
}
