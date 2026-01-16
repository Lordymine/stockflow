-- =====================================================
-- Roles Table
-- =====================================================
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT 'ADMIN, MANAGER, STAFF',
    description VARCHAR(255) COMMENT 'Descrição da role',
    tenant_id BIGINT NOT NULL COMMENT 'Multi-tenant',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_roles_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Roles do sistema (RBAC)';

CREATE INDEX idx_roles_tenant ON roles(tenant_id);

-- Insert default roles for tenant 1
INSERT INTO roles (name, description, tenant_id) VALUES
('ADMIN', 'Administrator - Full system access', 1),
('MANAGER', 'Manager - Regional and team access', 1),
('STAFF', 'Staff - Limited access', 1);
