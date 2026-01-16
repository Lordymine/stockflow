# Branches Module

## Purpose

Responsible for branch (filial) management and branch access governance.

## Responsibilities

- Branch CRUD operations
- Branch code uniqueness enforcement
- Branch activation/deactivation
- Branch access validation

## Boundaries

**This module DOES:**
- Manage branch lifecycle
- Maintain branch metadata
- Provide branch access validation
- Enforce branch code uniqueness

**This module DOES NOT:**
- Manage stock within branches (delegated to Inventory module)
- Manage users with branch access (delegated to Users module)
- Handle products (delegated to Catalog module)

## Domain Model

### Branch
- `id`: Primary key
- `tenantId`: Tenant identifier
- `name`: Branch name
- `code`: Branch code (unique per tenant)
- `address`: Physical address
- `phone`: Contact phone number
- `managerName`: Branch manager name
- `isActive`: Active status flag
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp
- `version`: Optimistic locking version

## API Endpoints

### POST /api/v1/branches
Create a new branch.

**Request:**
```json
{
  "name": "Main Warehouse",
  "code": "WH001",
  "address": "123 Industrial Ave",
  "phone": "+55 11 99999-9999",
  "managerName": "John Smith"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Main Warehouse",
    "code": "WH001",
    "address": "123 Industrial Ave",
    "phone": "+55 11 99999-9999",
    "managerName": "John Smith",
    "isActive": true,
    "createdAt": "2024-01-16T10:30:00"
  }
}
```

### GET /api/v1/branches
List branches with pagination.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `search`: Search in name or code

**Response:**
```json
{
  "success": true,
  "data": {
    "data": [
      {
        "id": 1,
        "name": "Main Warehouse",
        "code": "WH001",
        "isActive": true
      }
    ],
    "total": 10,
    "page": 0,
    "size": 20,
    "totalPages": 1
  }
}
```

### GET /api/v1/branches/{id}
Get branch by ID.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Main Warehouse",
    "code": "WH001",
    "address": "123 Industrial Ave",
    "phone": "+55 11 99999-9999",
    "managerName": "John Smith",
    "isActive": true,
    "createdAt": "2024-01-16T10:30:00"
  }
}
```

### PATCH /api/v1/branches/{id}/active
Activate or deactivate branch.

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

## Branch Access Control

### User-Branch Relationship
Users are granted access to specific branches through the `user_branches` join table.

### Access Validation
The `@BranchAccess` aspect validates branch access:
```java
@BranchAccess(required = true)
public ProductStock getStock(Long branchId, Long productId) {
    // Validates user has access to branchId
    // Throws ForbiddenException if not authorized
}
```

### Branch Filtering
Queries automatically filter by user's accessible branches:
```java
// Only returns branches user has access to
List<Branch> branches = branchRepository.findAllAccessibleByUser(userId);
```

## Business Rules

### Branch Code Uniqueness
- Branch codes must be unique within a tenant
- Format: Alphanumeric, 2-50 characters
- Example: "WH001", "STORE-01", "DC-NORTH"

### Branch Activation
- Cannot deactivate branch with active stock
- Cannot deactivate branch with pending transfers
- Deactivation prevents new stock movements

### Branch Deletion
- Branches are never deleted (soft delete via `isActive`)
- Maintains referential integrity with historical data

## Database Schema

```sql
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
  FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);
```

## Security

- Users can only access branches from their own tenant
- Users can only access branches they are explicitly granted access to
- ADMIN role bypasses branch access checks
- Branch access is validated at service layer

## Dependencies

- **Users module**: For branch access validation
- **Tenants module**: For tenant isolation
- **Shared kernel**: BaseEntity, exceptions, DTOs
