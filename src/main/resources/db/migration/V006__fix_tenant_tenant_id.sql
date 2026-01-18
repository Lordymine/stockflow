-- Fix tenant_id column in tenants table
-- For root entities like Tenant, tenant_id should equal id (self-reference)

-- First, make existing tenant_id values equal to id
UPDATE tenants SET tenant_id = id WHERE tenant_id IS NULL;

-- Now make the column NOT NULL (ignore if already NOT NULL)
-- ALTER TABLE tenants MODIFY COLUMN tenant_id BIGINT NOT NULL;

-- Drop triggers if they exist, then recreate
DROP TRIGGER IF EXISTS tenants_before_insert;
DROP TRIGGER IF EXISTS tenants_before_update;

DELIMITER $$

CREATE TRIGGER tenants_before_insert
BEFORE INSERT ON tenants
FOR EACH ROW
BEGIN
    IF NEW.tenant_id IS NULL THEN
        SET NEW.tenant_id = NEW.id;
    END IF;
END$$

CREATE TRIGGER tenants_before_update
BEFORE UPDATE ON tenants
FOR EACH ROW
BEGIN
    IF NEW.tenant_id IS NULL THEN
        SET NEW.tenant_id = NEW.id;
    END IF;
END$$

DELIMITER ;
