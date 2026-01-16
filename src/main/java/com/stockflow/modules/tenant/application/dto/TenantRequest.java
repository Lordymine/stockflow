package com.stockflow.modules.tenant.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Record para DTO de criação/atualização de Tenant.
 *
 * Records do Java 21 fornecem imutabilidade garantida, equals/hashCode/toString automáticos,
 * e sintaxe compacta. O compact constructor permite valores padrão e validação.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TenantRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    String name,

    @NotBlank(message = "Slug is required")
    @Size(min = 3, max = 100, message = "Slug must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers and hyphens")
    String slug,

    Boolean isActive
) {
    /**
     * Compact constructor - executa após validação mas antes da atribuição.
     * Permite definir valores padrão e validações customizadas.
     */
    public TenantRequest {
        if (isActive == null) {
            isActive = true;
        }
    }

    /**
     * Builder pattern para criação flexível (compatível com código existente).
     */
    public static TenantRequestBuilder builder() {
        return new TenantRequestBuilder();
    }

    public static class TenantRequestBuilder {
        private String name;
        private String slug;
        private Boolean isActive = true;

        public TenantRequestBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TenantRequestBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public TenantRequestBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public TenantRequest build() {
            return new TenantRequest(name, slug, isActive);
        }
    }
}
