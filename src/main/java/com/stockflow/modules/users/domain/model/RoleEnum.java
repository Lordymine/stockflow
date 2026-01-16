package com.stockflow.modules.users.domain.model;

/**
 * Enumeration of user roles in the system.
 *
 * <p>Roles define the permission level of users within their tenant:</p>
 * <ul>
 *   <li><b>ADMIN</b>: Full administrative access within the tenant</li>
 *   <li><b>MANAGER</b>: Operational management and catalog management</li>
 *   <li><b>STAFF</b>: Basic inventory operations and read-only access</li>
 * </ul>
 *
 * @see com.stockflow.modules.users.domain.model.Role
 */
public enum RoleEnum {
    /**
     * Administrator role with full access to all tenant resources.
     * Can manage users, branches, products, and perform all operations.
     */
    ADMIN,

    /**
     * Manager role with operational access.
     * Can manage catalog, inventory movements, transfers, and view reports.
     * Cannot manage other users or tenant settings.
     */
    MANAGER,

    /**
     * Staff role with basic operational access.
     * Can perform basic stock movements (IN/OUT) and view data.
     * Cannot create products, manage transfers, or perform administrative tasks.
     */
    STAFF
}
