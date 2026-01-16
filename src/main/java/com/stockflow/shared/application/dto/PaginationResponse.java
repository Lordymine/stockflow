package com.stockflow.shared.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper para respostas paginadas usando Java 21 Records.
 *
 * Records aninhados (Meta) são perfeitos para DTOs imutáveis.
 * Factory methods fornecem API fluente para criação.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaginationResponse<T>(
    List<T> data,
    Meta meta
) {
    /**
     * Converte um Page do Spring Data para PaginationResponse.
     */
    public static <T> PaginationResponse<T> fromPage(Page<T> page) {
        return new PaginationResponse<>(
            page.getContent(),
            new Meta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
            )
        );
    }

    /**
     * Cria uma resposta vazia.
     */
    public static <T> PaginationResponse<T> empty() {
        return new PaginationResponse<>(
            List.of(),
            new Meta(0, 0, 0, 0, true, true, true)
        );
    }

    /**
     * Record aninhado para metadados de paginação.
     * Records aninhados são mais limpos do que classes estáticas.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Meta(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
    ) {
        // Builder para compatibilidade
        public static MetaBuilder builder() {
            return new MetaBuilder();
        }

        public static class MetaBuilder {
            private int page;
            private int size;
            private long totalElements;
            private int totalPages;
            private boolean first;
            private boolean last;
            private boolean empty;

            public MetaBuilder page(int page) {
                this.page = page;
                return this;
            }

            public MetaBuilder size(int size) {
                this.size = size;
                return this;
            }

            public MetaBuilder totalElements(long totalElements) {
                this.totalElements = totalElements;
                return this;
            }

            public MetaBuilder totalPages(int totalPages) {
                this.totalPages = totalPages;
                return this;
            }

            public MetaBuilder first(boolean first) {
                this.first = first;
                return this;
            }

            public MetaBuilder last(boolean last) {
                this.last = last;
                return this;
            }

            public MetaBuilder empty(boolean empty) {
                this.empty = empty;
                return this;
            }

            public Meta build() {
                return new Meta(page, size, totalElements, totalPages, first, last, empty);
            }
        }
    }

    // Builder para compatibilidade
    public static <T> PaginationResponseBuilder<T> builder() {
        return new PaginationResponseBuilder<>();
    }

    public static class PaginationResponseBuilder<T> {
        private List<T> data;
        private Meta meta;

        public PaginationResponseBuilder<T> data(List<T> data) {
            this.data = data;
            return this;
        }

        public PaginationResponseBuilder<T> meta(Meta meta) {
            this.meta = meta;
            return this;
        }

        public PaginationResponse<T> build() {
            return new PaginationResponse<>(data, meta);
        }
    }
}
