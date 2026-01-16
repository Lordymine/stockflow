# Auth Module

## Purpose

Responsible for user authentication and token management.

## Responsibilities

- User login with email and password
- JWT token generation and validation
- Refresh token management
- User logout and token revocation
- Authentication context management

## Boundaries

**This module DOES:**
- Handle authentication logic
- Generate and validate tokens
- Manage refresh tokens lifecycle
- Provide authentication context

**This module DOES NOT:**
- Manage user data (delegated to Users module)
- Handle user CRUD operations
- Manage user roles or permissions (delegated to Users module)
- Handle authorization/permissions checks

## Domain Model

### RefreshToken
- `id`: Primary key
- `tenantId`: Tenant identifier
- `userId`: User reference
- `tokenHash`: Hashed refresh token
- `expiresAt`: Expiration timestamp
- `revokedAt`: Revocation timestamp (null if active)
- `createdAt`: Creation timestamp

## API Endpoints

### POST /api/v1/auth/login
Authenticate user with email and password.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

### POST /api/v1/auth/refresh
Refresh access token using refresh token.

**Request:**
```json
{
  "refreshToken": "eyJhbGci..."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

### POST /api/v1/auth/logout
Logout user and revoke refresh token.

**Request:**
```json
{
  "refreshToken": "eyJhbGci..."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "message": "Logged out successfully"
  }
}
```

## Security Considerations

- Passwords are hashed using BCrypt
- JWT tokens are signed with HS256 algorithm
- Refresh tokens are stored hashed in the database
- Access tokens have short expiration (24 hours)
- Refresh tokens have longer expiration (7 days)
- Refresh tokens can be revoked (logout)

## Dependencies

- **Users module**: For user validation
- **Shared kernel**: BaseEntity, exceptions, DTOs
- **Spring Security**: For authentication context
- **JJWT**: For JWT token generation/validation
