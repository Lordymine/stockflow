-- Create branches table
CREATE TABLE branches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address TEXT NULL,
    phone VARCHAR(20) NULL,
    manager_name VARCHAR(100) NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT fk_branches_tenant FOREIGN KEY (tenant_id)
        REFERENCES tenants(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_tenant_active ON branches(tenant_id, is_active);
CREATE INDEX idx_tenant_code ON branches(tenant_id, code);
