-- Add created_at for BaseEntity auditing on branch_product_stock.
ALTER TABLE branch_product_stock
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
