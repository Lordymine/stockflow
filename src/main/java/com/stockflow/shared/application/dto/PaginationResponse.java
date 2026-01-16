package com.stockflow.shared.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Standard pagination response wrapper.
 *
 * <p>All list endpoints should return data wrapped in this structure
 * to provide consistent pagination metadata.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {
 *   "data": [ ... ],
 *   "total": 100,
 *   "page": 1,
 *   "size": 20,
 *   "totalPages": 5
 * }
 * </pre>
 *
 * @param <T> the type of data in the list
 */
@Schema(description = "Paginated response wrapper")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationResponse<T> {

    @Schema(description = "List of items")
    private final List<T> data;

    @Schema(description = "Total number of items", example = "100")
    private final long total;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private final int page;

    @Schema(description = "Page size", example = "20")
    private final int size;

    @Schema(description = "Total number of pages", example = "5")
    private final int totalPages;

    /**
     * Creates a new pagination response.
     *
     * @param data       the list of items
     * @param total      total number of items
     * @param page       current page number
     * @param size       page size
     * @param totalPages total number of pages
     */
    public PaginationResponse(List<T> data, long total, int page, int size, int totalPages) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
    }

    /**
     * Creates a pagination response from a Spring Data Page.
     *
     * @param page the Spring Data Page object
     * @param <T>  the type of data
     * @return a new PaginationResponse instance
     */
    public static <T> PaginationResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return new PaginationResponse<>(
            page.getContent(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages()
        );
    }

    /**
     * Creates an empty pagination response.
     *
     * @param page requested page number
     * @param size requested page size
     * @param <T>  the type of data
     * @return a new PaginationResponse instance with empty data
     */
    public static <T> PaginationResponse<T> empty(int page, int size) {
        return new PaginationResponse<>(List.of(), 0, page, size, 0);
    }

    public List<T> getData() {
        return data;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    @Schema(description = "Indicates if there is a next page", example = "true")
    public boolean isHasNext() {
        return page < totalPages - 1;
    }

    @Schema(description = "Indicates if there is a previous page", example = "false")
    public boolean isHasPrevious() {
        return page > 0;
    }
}
