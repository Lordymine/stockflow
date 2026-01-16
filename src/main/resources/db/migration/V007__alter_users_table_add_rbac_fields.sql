-- =====================================================
-- Alter Users Table - Add RBAC fields
-- =====================================================

-- Rename password_hash to password
ALTER TABLE users CHANGE COLUMN password_hash password VARCHAR(255) NOT NULL COMMENT 'BCrypt hash';

-- Add phone column
ALTER TABLE users ADD COLUMN phone VARCHAR(20) COMMENT 'Telefone' AFTER password;

-- Add account lock fields
ALTER TABLE users ADD COLUMN is_account_locked BOOLEAN DEFAULT FALSE COMMENT 'Conta bloqueada?' AFTER is_active;
ALTER TABLE users ADD COLUMN failed_login_attempts INT DEFAULT 0 COMMENT 'Tentativas falhas' AFTER is_account_locked;

-- Add tracking fields
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP NULL COMMENT 'Último login' AFTER failed_login_attempts;
ALTER TABLE users ADD COLUMN password_changed_at TIMESTAMP NULL COMMENT 'Última troca de senha' AFTER last_login_at;

-- Add audit fields if not exists
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de criação' AFTER password_changed_at;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Última atualização' AFTER created_at;

-- Add audit tracking
ALTER TABLE users ADD COLUMN created_by BIGINT COMMENT 'Quem criou' AFTER updated_at;
ALTER TABLE users ADD COLUMN updated_by BIGINT COMMENT 'Quem atualizou' AFTER created_by;

-- Update constraint name to match entity
ALTER TABLE users DROP INDEX uk_users_tenant_email;
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email, tenant_id);

-- Insert default admin user for tenant 1
-- Password: admin123 (BCrypt hash)
INSERT INTO users (tenant_id, name, email, password, phone, is_active, created_at)
VALUES (1, 'Admin User', 'admin@stockflow.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '11999999999', TRUE, NOW())
ON DUPLICATE KEY UPDATE name = 'Admin User';
