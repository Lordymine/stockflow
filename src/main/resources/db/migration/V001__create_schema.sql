-- StockFlow PRO - Initial Schema
-- This migration creates the base schema for the multi-tenant inventory system

-- ============================================
-- Tenants Table
-- ============================================
CREATE TABLE tenants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Users Table
-- ============================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_email (tenant_id, email),
    INDEX idx_tenant_active (tenant_id, is_active),
    INDEX fk_users_tenant (tenant_id),
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Roles Table
-- ============================================
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name ENUM('ADMIN', 'MANAGER', 'STAFF') NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default roles
INSERT INTO roles (name) VALUES ('ADMIN'), ('MANAGER'), ('STAFF');

-- ============================================
-- User Roles Junction Table
-- ============================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    INDEX fk_user_roles_user (user_id),
    INDEX fk_user_roles_role (role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Branches Table
-- ============================================
CREATE TABLE branches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address TEXT,
    phone VARCHAR(20),
    manager_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    UNIQUE KEY uk_tenant_code (tenant_id, code),
    INDEX idx_tenant_active (tenant_id, is_active),
    INDEX fk_branches_tenant (tenant_id),
    CONSTRAINT fk_branches_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- User Branches Junction Table
-- ============================================
CREATE TABLE user_branches (
    user_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, branch_id),
    INDEX fk_user_branches_user (user_id),
    INDEX fk_user_branches_branch (branch_id),
    CONSTRAINT fk_user_branches_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_branches_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Categories Table
-- ============================================
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_name (tenant_id, name),
    INDEX fk_categories_tenant (tenant_id),
    CONSTRAINT fk_categories_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Products Table
-- ============================================
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    description TEXT,
    barcode VARCHAR(50),
    unit_of_measure ENUM('UN', 'KG', 'L', 'M') DEFAULT 'UN',
    image_url VARCHAR(500),
    cost_price DECIMAL(10,2),
    sale_price DECIMAL(10,2),
    min_stock INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    UNIQUE KEY uk_tenant_sku (tenant_id, sku),
    INDEX idx_products_tenant_active_name (tenant_id, is_active, name),
    INDEX idx_tenant_category (tenant_id, category_id),
    INDEX fk_products_tenant (tenant_id),
    INDEX fk_products_category (category_id),
    CONSTRAINT fk_products_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Branch Product Stock Table
-- ============================================
CREATE TABLE branch_product_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT DEFAULT 0 NOT NULL,
    version BIGINT DEFAULT 0 NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_branch_product (tenant_id, branch_id, product_id),
    INDEX idx_branch_product (branch_id, product_id),
    INDEX fk_stock_tenant (tenant_id),
    INDEX fk_stock_branch (branch_id),
    INDEX fk_stock_product (product_id),
    CONSTRAINT fk_stock_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_stock_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Stock Movements Table
-- ============================================
CREATE TABLE stock_movements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    type ENUM('IN', 'OUT', 'ADJUSTMENT', 'TRANSFER') NOT NULL,
    reason ENUM('PURCHASE', 'SALE', 'LOSS', 'RETURN', 'ADJUSTMENT_IN', 'ADJUSTMENT_OUT', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
    quantity INT NOT NULL,
    note TEXT,
    created_by_user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_movements_tenant_branch_date (tenant_id, branch_id, created_at DESC),
    INDEX idx_tenant_product (tenant_id, product_id),
    INDEX idx_branch_date (branch_id, created_at DESC),
    INDEX fk_movements_tenant (tenant_id),
    INDEX fk_movements_branch (branch_id),
    INDEX fk_movements_product (product_id),
    INDEX fk_movements_user (created_by_user_id),
    CONSTRAINT fk_movements_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_movements_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_movements_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_movements_user FOREIGN KEY (created_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Refresh Tokens Table
-- ============================================
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_token (user_id, token_hash),
    INDEX idx_expires (expires_at),
    INDEX fk_refresh_tokens_tenant (tenant_id),
    INDEX fk_refresh_tokens_user (user_id),
    CONSTRAINT fk_refresh_tokens_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
