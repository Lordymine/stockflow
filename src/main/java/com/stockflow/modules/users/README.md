# Users Module

## Purpose

Responsible for user management and role-based access control (RBAC).

## Responsibilities

- User CRUD operations
- Role assignment and management
- Branch access control (ABAC - Attribute-Based Access Control)
- User activation/deactivation
- Password management

## Boundaries

**This module DOES:**
- Manage user lifecycle (create, update, deactivate)
- Assign roles to users
- Grant/revoke branch access
- Handle password changes

**This module DOES NOT:**
- Handle authentication/login (delegated to Auth module)
- Manage tenant data (delegated to Tenants module)
- Manage branch data (delegated to Branches module)

## Domain Model

### User
- `id`: Primary key
- `tenantId`: Tenant identifier
- `name`: User full name
- `email`: Email address (unique per tenant)
- `passwordHash`: BCrypt password hash
- `isActive`: Active status flag
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp
- `version`: Optimistic locking version

### Role
- `id`: Primary key
- `name`: Role name (ADMIN, MANAGER, STAFF)

### UserRole (Join Table)
- `userId`: User reference
- `roleId`: Role reference
- Composite Primary Key: (userId, roleId)

### UserBranch (Join Table)
- `userId`: User reference
- `branchId`: Branch reference
- Composite Primary Key: (userId, branchId)

## Roles

### ADMIN
- Full system access
- Can manage all users
- Can manage all branches
- Can perform all operations

### MANAGER
- Can manage inventory
- Can view reports
- Can manage users with STAFF role
- Can access assigned branches

### STAFF
- Can perform stock operations
- Can view stock in assigned branches
- Cannot manage users
- Cannot access sensitive reports

## API Endpoints

### POST /api/v1/users
Create a new user (ADMIN only).

**Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "roleIds": [2],
  "branchIds": [1, 2]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "name": "John Doe",
    "email": "john@example.com",
    "isActive": true,
    "roles": [{"id": 2, "name": "MANAGER"}],
    "branches": [{"id": 1, "name": "Main Warehouse"}],
    "createdAt": "2024-01-16T10:30:00"
  }
}
```

### GET /api/v1/users
List users with pagination (ADMIN/MANAGER).

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)

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

### PUT /api/v1/users/{id}/roles
Update user roles (ADMIN only).

**Request:**
```json
{
  "roleIds": [1, 2]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "roles": [
      {"id": 1, "name": "ADMIN"},
      {"id": 2, "name": "MANAGER"}
    ]
  }
}
```

### PUT /api/v1/users/{id}/branches
Update user branch access (ADMIN only).

**Request:**
```json
{
  "branchIds": [1, 2, 3]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "branches": [
      {"id": 1, "name": "Main Warehouse"},
      {"id": 2, "name": "North Store"}
    ]
  }
}
```

### PATCH /api/v1/users/{id}/active
Activate or deactivate user (ADMIN only).

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
    "id": 2,
    "isActive": false
  }
}
```

## Branch Access Control

### How it Works
1. Users are assigned specific branches they can access
2. All stock operations are scoped to user's accessible branches
3. Attempting to access unauthorized branch returns 403

### Implementation
```java
@BranchAccess(required = true)
public StockMovement createMovement(Long branchId, ...) {
    // Automatically validates user has access to branchId
    // Throws ForbiddenException if not authorized
}
```

## Password Management

### Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character

### Password Hashing
- BCrypt algorithm with strength 10
- Passwords are never stored in plain text
- Passwords never logged or returned in API responses

## Security

- Users can only access users from their own tenant
- ADMIN can manage all users
- MANAGER can manage STAFF users
- Users can only access data from their assigned branches
- Email must be unique within tenant

## Dependencies

- **Auth module**: For password hashing (BCrypt)
- **Tenants module**: For tenant isolation
- **Branches module**: For branch access control
- **Shared kernel**: BaseEntity, exceptions, DTOs
