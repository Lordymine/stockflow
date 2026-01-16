# Catalog Module

## Purpose

Responsible for product and category management.

## Responsibilities

- Product CRUD operations
- Category CRUD operations
- SKU uniqueness enforcement
- Product-category relationship management

## Boundaries

**This module DOES:**
- Manage product catalog
- Manage categories
- Enforce SKU uniqueness
- Handle product activation/deactivation

**This module DOES NOT:**
- Manage stock levels (delegated to Inventory module)
- Track stock movements (delegated to Inventory module)
- Handle transfers (delegated to Inventory module)

## Domain Model

### Category
- `id`: Primary key
- `tenantId`: Tenant identifier
- `name`: Category name
- `createdAt`: Creation timestamp

### Product
- `id`: Primary key
- `tenantId`: Tenant identifier
- `categoryId`: Category reference (nullable)
- `name`: Product name
- `sku`: Stock Keeping Unit (unique per tenant)
- `description`: Product description
- `barcode`: Product barcode (EAN/UPC)
- `unitOfMeasure`: Unit (UN, KG, L, M)
- `imageUrl`: Product image URL
- `costPrice`: Cost price
- `salePrice`: Sale price
- `minStock`: Minimum stock threshold
- `isActive`: Active status flag
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp
- `version`: Optimistic locking version

## API Endpoints

### Categories

#### POST /api/v1/categories
Create a new category.

**Request:**
```json
{
  "name": "Electronics"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Electronics",
    "createdAt": "2024-01-16T10:30:00"
  }
}
```

#### GET /api/v1/categories
List all categories.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Electronics",
      "createdAt": "2024-01-16T10:30:00"
    }
  ]
}
```

#### PUT /api/v1/categories/{id}
Update category.

**Request:**
```json
{
  "name": "Consumer Electronics"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Consumer Electronics"
  }
}
```

### Products

#### POST /api/v1/products
Create a new product.

**Request:**
```json
{
  "categoryId": 1,
  "name": "Wireless Mouse",
  "sku": "MOUSE-WL-001",
  "description": "Ergonomic wireless mouse",
  "barcode": "7891234567890",
  "unitOfMeasure": "UN",
  "imageUrl": "https://example.com/mouse.jpg",
  "costPrice": 25.00,
  "salePrice": 49.90,
  "minStock": 10
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "categoryId": 1,
    "name": "Wireless Mouse",
    "sku": "MOUSE-WL-001",
    "description": "Ergonomic wireless mouse",
    "barcode": "7891234567890",
    "unitOfMeasure": "UN",
    "imageUrl": "https://example.com/mouse.jpg",
    "costPrice": 25.00,
    "salePrice": 49.90,
    "minStock": 10,
    "isActive": true,
    "createdAt": "2024-01-16T10:30:00"
  }
}
```

#### GET /api/v1/products
List products with pagination and search.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `search`: Search in name, SKU, or barcode
- `categoryId`: Filter by category
- `isActive`: Filter by active status

**Response:**
```json
{
  "success": true,
  "data": {
    "data": [...],
    "total": 100,
    "page": 0,
    "size": 20,
    "totalPages": 5
  }
}
```

#### GET /api/v1/products/{id}
Get product by ID.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "categoryId": 1,
    "category": {
      "id": 1,
      "name": "Electronics"
    },
    "name": "Wireless Mouse",
    "sku": "MOUSE-WL-001",
    "isActive": true
  }
}
```

#### PUT /api/v1/products/{id}
Update product.

**Request:**
```json
{
  "name": "Wireless Mouse Pro",
  "salePrice": 59.90
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Wireless Mouse Pro",
    "salePrice": 59.90
  }
}
```

#### PATCH /api/v1/products/{id}/active
Activate or deactivate product.

**Request:**
```json
{
  "isActive": false
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "isActive": false
  }
}
```

## Business Rules

### SKU Uniqueness
- SKUs must be unique within a tenant
- Format: Alphanumeric with hyphens, 2-100 characters
- Examples: "PROD-001", "MOUSE-WL-BLK", "LAPTOP-15-HP"

### Product Activation
- Cannot delete products (soft delete via `isActive`)
- Inactive products don't appear in searches by default
- Historical data preserved for stock movements

### Unit of Measure
Supported values:
- `UN`: Unit
- `KG`: Kilogram
- `L`: Liter
- `M`: Meter

## Database Schema

```sql
CREATE TABLE categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tenant_name (tenant_id, name),
  INDEX fk_categories_tenant (tenant_id),
  FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

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
  FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

## Security

- Users can only access products from their own tenant
- SKU uniqueness enforced per tenant
- All operations scoped by tenant_id

## Dependencies

- **Tenants module**: For tenant isolation
- **Shared kernel**: BaseEntity, exceptions, DTOs
