package com.stockflow.modules.tenant.application.service;

import com.stockflow.modules.tenant.application.dto.TenantResponse;

/**
 * Service interface for tenant operations.
 */
public interface TenantService {

    /**
     * Gets the current tenant from the security context.
     *
     * @return the tenant response
     */
    TenantResponse getCurrentTenant();
}
