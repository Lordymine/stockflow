package com.stockflow.shared.infrastructure.security;

/**
 * Thread-local context for storing tenant information throughout the request lifecycle.
 *
 * <p>This class provides a thread-safe way to store and retrieve the current tenant ID
 * extracted from the JWT token. The tenant ID is used to scope all database queries
 * for multi-tenant isolation.</p>
 *
 * <p>The context is automatically cleared at the end of each request by the
 * {@link com.stockflow.shared.infrastructure.web.TenantCleanupFilter}.</p>
 *
 * <p><b>Important:</b> Always clear the context after use to prevent thread contamination
 * in thread pool environments.</p>
 */
public class TenantContext {

    /**
     * ThreadLocal storage for tenant ID.
     * Each thread has its own isolated copy.
     */
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private TenantContext() {
    }

    /**
     * Sets the current tenant ID for this thread.
     *
     * @param tenantId the tenant ID to set
     */
    public static void setTenantId(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Gets the current tenant ID for this thread.
     *
     * @return the tenant ID, or null if not set
     */
    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Checks if a tenant is set in the current context.
     *
     * @return true if a tenant ID is set
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }

    /**
     * Clears the tenant ID from the current thread.
     * <p>Should be called at the end of each request to prevent memory leaks.</p>
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * Gets the tenant ID or throws an exception if not set.
     * Useful for methods that require tenant context.
     *
     * @return the tenant ID
     * @throws IllegalStateException if no tenant is set
     */
    public static Long requireTenantId() {
        Long tenantId = getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available. " +
                "This operation requires an authenticated request.");
        }
        return tenantId;
    }
}
