package com.stockflow.shared.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * DTO para receber parâmetros de paginação
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageRequestDTO {
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    private List<String> sort;

    private Sort.Direction sortDirection;

    /**
     * Valida e retorna um Pageable
     */
    public Pageable toPageable() {
        // Validar size máximo
        if (size > 100) {
            size = 100;
        }
        if (size < 1) {
            size = 20;
        }

        // Criar PageRequest
        if (sort != null && !sort.isEmpty()) {
            Sort.Direction direction = sortDirection != null ? sortDirection : Sort.Direction.ASC;
            return PageRequest.of(page, size, Sort.by(direction, sort.toArray(new String[0])));
        }

        return PageRequest.of(page, size);
    }

    /**
     * PageRequest com valores padrão
     */
    public static PageRequestDTO of(int page, int size) {
        return PageRequestDTO.builder()
            .page(page)
            .size(size)
            .build();
    }
}
