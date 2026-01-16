# API Error Codes

This document defines all standard error codes used in the StockFlow PRO API.

## Error Response Format

All errors follow this standard format:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": ["Detailed error message 1", "Detailed error message 2"],
    "timestamp": "2024-01-16T10:30:00"
  }
}
```

## HTTP Status Codes

| Code | Status | Description |
|------|--------|-------------|
| 200  | OK | Request succeeded |
| 201  | Created | Resource created successfully |
| 400  | Bad Request | Invalid request data or validation error |
| 401  | Unauthorized | Authentication required or failed |
| 403  | Forbidden | Insufficient permissions |
| 404  | Not Found | Resource not found |
| 409  | Conflict | Resource conflict or concurrent modification |
| 500  | Internal Server Error | Unexpected server error |

## Error Codes

### Authentication Errors (401)

| Error Code | Message | Description |
|------------|---------|-------------|
| `AUTH_INVALID_CREDENTIALS` | Invalid email or password | Login credentials are incorrect |
| `AUTH_TOKEN_EXPIRED` | Access token has expired | JWT token is no longer valid |
| `AUTH_TOKEN_INVALID` | Invalid token | Token is malformed or tampered |
| `AUTH_REFRESH_TOKEN_INVALID` | Invalid refresh token | Refresh token is not valid |
| `AUTH_REFRESH_TOKEN_EXPIRED` | Refresh token has expired | Refresh token has passed its expiration time |

### Authorization Errors (403)

| Error Code | Message | Description |
|------------|---------|-------------|
| `FORBIDDEN_BRANCH_ACCESS` | Access denied to branch {id} | User is not authorized for this branch |
| `FORBIDDEN_TENANT_ACCESS` | Access denied to tenant {id} | User is not authorized for this tenant |
| `INSUFFICIENT_PRIVILEGES` | Insufficient privileges for this operation | User lacks required role/permission |
| `FORBIDDEN_RESOURCE_ACCESS` | Access to this resource is forbidden | User cannot access this specific resource |

### Validation Errors (400)

| Error Code | Message | Description |
|------------|---------|-------------|
| `VALIDATION_ERROR` | Validation failed | Input validation failed |
| `INVALID_REQUEST` | Invalid request | Request is malformed or contains invalid data |
| `MISSING_REQUIRED_FIELD` | Required field is missing | A required field was not provided |
| `INVALID_FORMAT` | Invalid format | Field format is incorrect |

### Resource Not Found Errors (404)

| Error Code | Message | Description |
|------------|---------|-------------|
| `RESOURCE_NOT_FOUND` | Resource not found | Generic resource not found error |
| `USER_NOT_FOUND` | User not found | User with given ID does not exist |
| `PRODUCT_NOT_FOUND` | Product not found | Product with given ID does not exist |
| `BRANCH_NOT_FOUND` | Branch not found | Branch with given ID does not exist |
| `CATEGORY_NOT_FOUND` | Category not found | Category with given ID does not exist |
| `TENANT_NOT_FOUND` | Tenant not found | Tenant with given ID does not exist |
| `STOCK_NOT_FOUND` | Stock record not found | Stock record for branch/product not found |

### Conflict Errors (409)

| Error Code | Message | Description |
|------------|---------|-------------|
| `RESOURCE_ALREADY_EXISTS` | Resource already exists | Attempt to create duplicate resource |
| `PRODUCT_SKU_ALREADY_EXISTS` | Product SKU already exists | SKU must be unique per tenant |
| `BRANCH_CODE_ALREADY_EXISTS` | Branch code already exists | Branch code must be unique per tenant |
| `USER_EMAIL_ALREADY_EXISTS` | Email already exists | Email must be unique per tenant |
| `STOCK_INSUFFICIENT` | Insufficient stock | Not enough stock for this operation |
| `STOCK_CONCURRENT_MODIFICATION` | Stock was modified by another user | Optimistic locking failure |
| `CONCURRENT_MODIFICATION` | Resource was modified by another user | Generic optimistic locking failure |

### Business Logic Errors (400)

| Error Code | Message | Description |
|------------|---------|-------------|
| `STOCK_NEGATIVE_NOT_ALLOWED` | Stock cannot be negative | Attempt to set stock below zero |
| `TRANSFER_SAME_BRANCH` | Cannot transfer to same branch | Origin and destination branches must be different |
| `TRANSFER_QUANTITY_INVALID` | Invalid transfer quantity | Transfer quantity must be greater than zero |
| `PRODUCT_ACTIVE_REQUIRED` | Product must be active | Operation requires active product |
| `BRANCH_ACTIVE_REQUIRED` | Branch must be active | Operation requires active branch |
| `USER_ACTIVE_REQUIRED` | User must be active | Operation requires active user |

### Server Errors (500)

| Error Code | Message | Description |
|------------|---------|-------------|
| `INTERNAL_SERVER_ERROR` | An unexpected error occurred | Generic server error |
| `DATABASE_ERROR` | Database operation failed | Error in database operation |
| `EXTERNAL_SERVICE_ERROR` | External service error | Error calling external service |

## Error Handling Best Practices

### For Clients

1. **Always check the `success` field** in responses
2. **Handle error codes appropriately** - don't just show the message
3. **Use `details` array for field-level validation errors**
4. **Implement retry logic for `STOCK_CONCURRENT_MODIFICATION` errors**
5. **Log errors for debugging** but don't expose to end users

### Example Error Handling (JavaScript)

```javascript
try {
  const response = await fetch('/api/v1/products', {
    method: 'POST',
    body: JSON.stringify(productData)
  });

  const result = await response.json();

  if (!result.success) {
    // Handle specific error codes
    switch (result.error.code) {
      case 'PRODUCT_SKU_ALREADY_EXISTS':
        showErrorMessage('SKU already exists. Please use a different SKU.');
        break;
      case 'VALIDATION_ERROR':
        showFieldErrors(result.error.details);
        break;
      case 'STOCK_CONCURRENT_MODIFICATION':
        showErrorMessage('Please refresh and try again.');
        // Auto-retry after 1 second
        setTimeout(() => createProduct(productData), 1000);
        break;
      default:
        showErrorMessage(result.error.message);
    }
  }
} catch (error) {
  showErrorMessage('Network error. Please try again.');
}
```

## Adding New Error Codes

When adding new error codes:

1. **Choose a descriptive code name** (e.g., `PRODUCT_SKU_ALREADY_EXISTS`)
2. **Use consistent naming**: UPPER_CASE with underscores
3. **Add to appropriate section** in this document
4. **Map to correct HTTP status** (401, 403, 404, 409, 500)
5. **Create exception class** if it's a domain error
6. **Add handler in GlobalExceptionHandler** if needed
7. **Update this documentation**

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2024-01-16 | Initial error codes definition |
