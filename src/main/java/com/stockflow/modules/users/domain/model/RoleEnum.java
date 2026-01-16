package com.stockflow.modules.users.domain.model;

/**
 * Roles disponíveis no sistema com hierarquia.
 *
 * Hierarchy:
 * - ADMIN (acesso total)
 *   ├── MANAGER (acesso regional)
 *   │   └── STAFF (acesso limitado)
 *   └── STAFF
 */
public enum RoleEnum {
    /**
     * Administrador - Acesso total ao sistema
     */
    ADMIN("Administrator", "Full system access"),

    /**
     * Gerente - Acesso regional e de equipe
     */
    MANAGER("Manager", "Regional and team access"),

    /**
     * Funcionário - Acesso limitado
     */
    STAFF("Staff", "Limited access");

    private final String displayName;
    private final String description;

    RoleEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
