package com.stockflow.modules.tenant.domain.service;

import com.stockflow.modules.tenant.application.dto.TenantRequest;
import com.stockflow.modules.tenant.application.dto.TenantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TenantService {

    /**
     * Criar novo tenant
     */
    TenantResponse create(TenantRequest request);

    /**
     * Buscar tenant por ID
     */
    TenantResponse findById(Long id);

    /**
     * Buscar tenant por slug
     */
    TenantResponse findBySlug(String slug);

    /**
     * Listar todos os tenants
     */
    List<TenantResponse> findAll();

    /**
     * Listar tenants com paginação
     */
    Page<TenantResponse> findAll(Pageable pageable);

    /**
     * Listar apenas tenants ativos
     */
    List<TenantResponse> findActive();

    /**
     * Atualizar tenant
     */
    TenantResponse update(Long id, TenantRequest request);

    /**
     * Ativar/desativar tenant
     */
    TenantResponse toggleActive(Long id, boolean isActive);

    /**
     * Deletar tenant
     */
    void delete(Long id);

    /**
     * Verificar se slug existe
     */
    boolean slugExists(String slug);

    /**
     * Verificar se slug existe ignorando um ID
     */
    boolean slugExists(String slug, Long excludeId);
}
