# Tenants Module

## Purpose

Responsible for tenant (company) management and multi-tenancy isolation.

## Responsibilities

- Tenant creation and management
- Tenant data isolation
- Tenant context propagation
- Tenant-level configuration

## Boundaries

**This module DOES:**
- Manage tenant lifecycle (CRUD)
- Provide tenant context for request scoping
- Enforce tenant data isolation

**This module DOES NOT:**
- Manage users within tenants (delegated to Users module)
- Manage branches within tenants (delegated to Branches module)
- Handle authentication (delegated to Auth module)

## Domain Model

### Tenant
- `id`: Primary key
- `name`: Company name
- `slug`: URL-friendly identifier (unique)
- `isActive`: Active status flag
- `createdAt`: Creation timestamp

## API Endpoints

### POST /api/v1/tenants
Create a new tenant (company).

**Request:**
```json
{
  "name": "Acme Corporation",
  "slug": "acme-corp"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Acme Corporation",
    "slug": "acme-corp",
    "isActive": true,
    "createdAt": "2024-01-16T10:30:00"
  }
}
```

### GET /api/v1/tenants/me
Get current tenant information.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Acme Corporation",
    "slug": "acme-corp",
    "isActive": true,
    "createdAt": "2024-01-16T10:30:00"
  }
}
```

## Multi-Tenancy Strategy

### Column-based Multi-Tenancy
- Each table has a `tenant_id` column
- All queries automatically filter by `tenant_id`
- Tenant ID is extracted from JWT token
- Tenant isolation is enforced at database level

### Tenant Context
The `TenantContext` class holds the current tenant ID throughout the request lifecycle:

```java
// Set tenant context from JWT
TenantContext.setTenantId(tenantId);

// Get tenant ID in services
Long tenantId = TenantContext.getTenantId();

// Clear at end of request
TenantContext.clear();
```

## Data Isolation

### Automatic Filtering
All JPA repositories automatically filter by tenant:

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Automatically adds: WHERE tenant_id = ?
    List<Product> findAll();
}
```

### Indexes
All tables have indexes on `tenant_id` for performance:
```sql
CREATE INDEX idx_products_tenant ON products(tenant_id);
```

## Security

- Users can only access data from their own tenant
- Tenant ID is extracted from JWT claims
- Cross-tenant access returns 403 Forbidden
- Database queries always include tenant filter

## Dependencies

- **Shared kernel**: BaseEntity, exceptions, DTOs
- **Spring Security**: For tenant extraction from JWT
- **JPA**: For automatic tenant filtering
