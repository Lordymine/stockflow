package com.stockflow.shared.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Sort;

/**
 * Search request DTO that extends pagination with search functionality.
 *
 * <p>Provides search parameters for endpoints that support filtering:</p>
 * <ul>
 *   <li>All pagination parameters from PageRequestDTO</li>
 *   <li>search: text search term for filtering results</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>
 * {
 *   "page": 0,
 *   "size": 20,
 *   "sort": "name",
 *   "direction": "ASC",
 *   "search": "product name"
 * }
 * </pre>
 */
@Schema(description = "Search and pagination parameters")
public class SearchRequestDTO extends PageRequestDTO {

    @Schema(description = "Search term for filtering results", example = "product name")
    private String search;

    public SearchRequestDTO() {
        super();
    }

    public SearchRequestDTO(int page, int size, String sort, Sort.Direction direction, String search) {
        super(page, size, sort, direction);
        this.search = search;
    }

    /**
     * Creates a SearchRequestDTO with search term.
     *
     * @param search the search term
     * @return a new SearchRequestDTO
     */
    public static SearchRequestDTO of(String search) {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setSearch(search);
        return dto;
    }

    /**
     * Creates a SearchRequestDTO with pagination and search.
     *
     * @param page   page number
     * @param size   page size
     * @param search the search term
     * @return a new SearchRequestDTO
     */
    public static SearchRequestDTO of(int page, int size, String search) {
        return new SearchRequestDTO(page, size, "createdAt", Sort.Direction.DESC, search);
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * Checks if a search term is present.
     *
     * @return true if search is not null and not empty
     */
    public boolean hasSearch() {
        return search != null && !search.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("SearchRequestDTO{page=%d, size=%d, sort='%s', direction=%s, search='%s'}",
            getPage(), getSize(), getSort(), getDirection(), search);
    }
}
