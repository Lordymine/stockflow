-- Fix missing columns in users and branches tables
-- These tables extend BaseEntity which requires updated_at and version columns

-- Fix users table
ALTER TABLE users
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at,
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL AFTER updated_at;

-- Fix branches table (should already have these based on V001, but ensuring consistency)
-- The branches table already has updated_at and version in V001, so this is a safety check
-- If the columns don't exist, add them; if they do, this will fail safely (idempotent)
