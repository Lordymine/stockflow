package com.stockflow.modules.tenant.application.service;

import com.stockflow.modules.tenant.application.dto.TenantResponse;
import com.stockflow.modules.tenant.domain.model.Tenant;
import com.stockflow.modules.tenant.domain.repository.TenantRepository;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of tenant service.
 */
@Service
public class TenantServiceImpl implements TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantServiceImpl.class);

    private final TenantRepository tenantRepository;

    public TenantServiceImpl(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getCurrentTenant() {
        Long tenantId = TenantContext.requireTenantId();

        Tenant tenant = tenantRepository.findActiveById(tenantId)
            .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND",
                "Tenant not found with ID: " + tenantId));

        logger.debug("Retrieved tenant: {}", tenant.getName());

        return new TenantResponse(
            tenant.getId(),
            tenant.getName(),
            tenant.getSlug(),
            tenant.getIsActive(),
            tenant.getCreatedAt()
        );
    }
}
