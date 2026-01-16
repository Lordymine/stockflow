-- V003: Inserir tenant default para desenvolvimento

INSERT INTO tenants (name, slug, is_active) VALUES
('Demo Company', 'demo-company', TRUE);

-- Verificar inserção
SELECT * FROM tenants WHERE slug = 'demo-company';
