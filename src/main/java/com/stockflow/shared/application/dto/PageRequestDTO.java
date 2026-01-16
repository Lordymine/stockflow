package com.stockflow.shared.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Sort;

import java.util.Objects;

/**
 * Standard page request DTO for all paginated endpoints.
 *
 * <p>Provides consistent pagination parameters across all list endpoints:</p>
 * <ul>
 *   <li>page: page number (0-indexed)</li>
 *   <li>size: number of items per page</li>
 *   <li>sort: field to sort by</li>
 *   <li>direction: sort direction (ASC or DESC)</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>
 * {
 *   "page": 0,
 *   "size": 20,
 *   "sort": "createdAt",
 *   "direction": "DESC"
 * }
 * </pre>
 */
@Schema(description = "Pagination and sorting parameters")
public class PageRequestDTO {

    @Schema(description = "Page number (0-indexed)", example = "0")
    private int page = 0;

    @Schema(description = "Page size", example = "20")
    private int size = 20;

    @Schema(description = "Field to sort by", example = "createdAt")
    private String sort = "createdAt";

    @Schema(description = "Sort direction", example = "DESC", allowableValues = {"ASC", "DESC"})
    private Sort.Direction direction = Sort.Direction.DESC;

    public PageRequestDTO() {
    }

    public PageRequestDTO(int page, int size, String sort, Sort.Direction direction) {
        this.page = page;
        this.size = size;
        this.sort = sort;
        this.direction = direction;
    }

    /**
     * Creates a Spring Pageable object from this DTO.
     *
     * @return a Spring Pageable instance
     */
    public org.springframework.data.domain.Pageable toPageable() {
        return org.springframework.data.domain.PageRequest.of(page, size, Sort.by(direction, sort));
    }

    /**
     * Creates a PageRequestDTO with default values.
     *
     * @return a new PageRequestDTO with defaults
     */
    public static PageRequestDTO of() {
        return new PageRequestDTO();
    }

    /**
     * Creates a PageRequestDTO with specific page and size.
     *
     * @param page page number
     * @param size page size
     * @return a new PageRequestDTO
     */
    public static PageRequestDTO of(int page, int size) {
        return new PageRequestDTO(page, size, "createdAt", Sort.Direction.DESC);
    }

    // Getters and Setters

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        // Limit max page size to prevent performance issues
        this.size = Math.min(Math.max(1, size), 100);
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Sort.Direction getDirection() {
        return direction;
    }

    public void setDirection(Sort.Direction direction) {
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageRequestDTO that = (PageRequestDTO) o;
        return page == that.page &&
               size == that.size &&
               Objects.equals(sort, that.sort) &&
               direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, sort, direction);
    }

    @Override
    public String toString() {
        return String.format("PageRequestDTO{page=%d, size=%d, sort='%s', direction=%s}",
            page, size, sort, direction);
    }
}
