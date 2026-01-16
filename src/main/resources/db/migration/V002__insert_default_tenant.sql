-- Insert default tenant for development/testing
-- Note: tenant_id will be set to id via @PrePersist in Tenant entity
INSERT INTO tenants (name, slug, is_active, version) VALUES
('Demo Company', 'demo-company', TRUE, 0);

-- Update tenant_id to reference itself (for multi-tenancy root entity)
UPDATE tenants SET tenant_id = id WHERE id = LAST_INSERT_ID();

-- This tenant can be used for initial testing and development
-- Production environments should create tenants via the API
