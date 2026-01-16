package com.stockflow.modules.tenant.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockflow.modules.tenant.domain.model.Tenant;

import java.time.LocalDateTime;

/**
 * Record para DTO de resposta de Tenant.
 *
 * Records fornecem imutabilidade garantida e zero boilerplate.
 * Método estático from() para criação a partir da entidade.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TenantResponse(
    Long id,
    String name,
    String slug,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * Factory method para criar TenantResponse a partir da entidade.
     * Mais legível do que depender de MapStruct.
     */
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
            tenant.getId(),
            tenant.getName(),
            tenant.getSlug(),
            tenant.getIsActive(),
            tenant.getCreatedAt(),
            tenant.getUpdatedAt()
        );
    }

    /**
     * Builder pattern para compatibilidade com código existente.
     * Eventualmente pode ser removido em favor de from() ou do construtor.
     */
    public static TenantResponseBuilder builder() {
        return new TenantResponseBuilder();
    }

    public static class TenantResponseBuilder {
        private Long id;
        private String name;
        private String slug;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public TenantResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TenantResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TenantResponseBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public TenantResponseBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public TenantResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TenantResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TenantResponse build() {
            return new TenantResponse(id, name, slug, isActive, createdAt, updatedAt);
        }
    }
}
