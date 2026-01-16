-- =====================================================
-- Refresh Tokens Table
-- =====================================================
-- Armazena refresh tokens para permitir revogação
-- e controle de sessões ativas
-- =====================================================

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE COMMENT 'Token JWT refresh',
    user_id BIGINT NOT NULL COMMENT 'FK para users (será criado no Sprint 04)',
    tenant_id BIGINT NOT NULL COMMENT 'FK para tenants',
    expiry_date TIMESTAMP NOT NULL COMMENT 'Data de expiração',
    is_revoked BOOLEAN DEFAULT FALSE COMMENT 'Se foi revogado (logout)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de criação',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Última atualização',

    CONSTRAINT fk_refresh_tokens_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Refresh tokens para autenticação JWT';

-- Índices
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_tenant_id ON refresh_tokens(tenant_id);
CREATE INDEX idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);
CREATE INDEX idx_refresh_tokens_is_revoked ON refresh_tokens(is_revoked);

-- Índice composto para busca de tokens válidos de um usuário
CREATE INDEX idx_refresh_tokens_user_valid
    ON refresh_tokens(user_id, is_revoked, expiry_date);
