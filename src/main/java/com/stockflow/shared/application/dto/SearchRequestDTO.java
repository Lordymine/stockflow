package com.stockflow.shared.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * DTO para requisições com busca e paginação
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequestDTO {
    private String search;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    private String sortBy;

    private SortDirection sortDirection;

    public enum SortDirection {
        ASC, DESC
    }

    public Pageable toPageable() {
        if (size > 100) size = 100;
        if (size < 1) size = 20;

        if (sortBy != null) {
            Sort.Direction direction = sortDirection == SortDirection.DESC
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
            return PageRequest.of(page, size, Sort.by(direction, sortBy));
        }

        return PageRequest.of(page, size);
    }
}
