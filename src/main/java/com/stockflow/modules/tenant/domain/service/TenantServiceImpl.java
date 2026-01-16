package com.stockflow.modules.tenant.domain.service;

import com.stockflow.modules.tenant.application.dto.TenantRequest;
import com.stockflow.modules.tenant.application.dto.TenantResponse;
import com.stockflow.modules.tenant.application.mapper.TenantMapper;
import com.stockflow.modules.tenant.domain.model.Tenant;
import com.stockflow.modules.tenant.domain.repository.TenantRepository;
import com.stockflow.shared.domain.exception.BadRequestException;
import com.stockflow.shared.domain.exception.ConflictException;
import com.stockflow.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantServiceImpl implements TenantService {

    private final TenantRepository repository;
    private final TenantMapper mapper;

    @Override
    public TenantResponse create(TenantRequest request) {
        log.info("Creating tenant with slug: {}", request.slug());

        // Validar slug único
        if (repository.existsBySlug(request.slug())) {
            throw new ConflictException(
                "Tenant with slug '%s' already exists",
                request.slug()
            );
        }

        Tenant tenant = mapper.toEntity(request);
        Tenant saved = repository.save(tenant);

        log.info("Tenant created successfully: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse findById(Long id) {
        Tenant tenant = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant", id));
        return mapper.toResponse(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse findBySlug(String slug) {
        Tenant tenant = repository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Tenant not found with slug: " + slug));
        return mapper.toResponse(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> findAll() {
        List<Tenant> tenants = repository.findAll();
        return mapper.toResponseList(tenants);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenantResponse> findAll(Pageable pageable) {
        Page<Tenant> page = repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> findActive() {
        List<Tenant> tenants = repository.findByIsActiveTrue();
        return mapper.toResponseList(tenants);
    }

    @Override
    public TenantResponse update(Long id, TenantRequest request) {
        log.info("Updating tenant: {}", id);

        Tenant tenant = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant", id));

        // Validar slug único (se mudou)
        if (!tenant.getSlug().equals(request.slug())) {
            if (repository.existsBySlugAndIdNot(request.slug(), id)) {
                throw new ConflictException(
                    "Tenant with slug '%s' already exists",
                    request.slug()
                );
            }
        }

        mapper.updateEntityFromRequest(request, tenant);
        Tenant updated = repository.save(tenant);

        log.info("Tenant updated successfully: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    public TenantResponse toggleActive(Long id, boolean isActive) {
        log.info("{} tenant: {}", isActive ? "Activating" : "Deactivating", id);

        Tenant tenant = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant", id));

        if (isActive) {
            tenant.activate();
        } else {
            tenant.deactivate();
        }

        Tenant saved = repository.save(tenant);

        log.info("Tenant {} successfully: {}", id, isActive ? "activated" : "deactivated");
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting tenant: {}", id);

        Tenant tenant = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant", id));

        // TODO: Validar se tenant tem usuários/antes de deletar
        if (tenant.isActive()) {
            throw new BadRequestException(
                "Cannot delete active tenant. Deactivate it first."
            );
        }

        repository.delete(tenant);

        log.info("Tenant deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean slugExists(String slug) {
        return repository.existsBySlug(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean slugExists(String slug, Long excludeId) {
        return repository.existsBySlugAndIdNot(slug, excludeId);
    }
}
