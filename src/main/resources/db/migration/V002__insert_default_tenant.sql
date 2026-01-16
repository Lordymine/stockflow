-- Insert default tenant for development/testing
INSERT INTO tenants (name, slug, is_active) VALUES ('Demo Company', 'demo-company', TRUE);

-- This tenant can be used for initial testing and development
-- Production environments should create tenants via the API
