-- Add auditing columns to stock_movements.
ALTER TABLE stock_movements ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE stock_movements ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
