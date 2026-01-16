# Inventory Module

## Purpose

Responsible for stock management, stock movements, and transfers between branches.

## Responsibilities

- Stock level management per branch and product
- Stock movement tracking (IN, OUT, ADJUSTMENT)
- Transfer management between branches
- Low stock alerts
- Concurrent modification prevention (optimistic locking)

## Boundaries

**This module DOES:**
- Manage stock levels
- Track all stock movements
- Execute transfers between branches
- Enforce stock invariants
- Validate branch access
- Handle optimistic locking

**This module DOES NOT:**
- Manage products (delegated to Catalog module)
- Manage branches (delegated to Branches module)
- Manage users (delegated to Users module)
- Generate reports (delegated to Dashboard module)

## Domain Model

### BranchProductStock
- `id`: Primary key
- `tenantId`: Tenant identifier
- `branchId`: Branch reference
- `productId`: Product reference
- `quantity`: Current stock quantity
- `version`: Optimistic locking version
- `updatedAt`: Last update timestamp
- Unique constraint: (tenantId, branchId, productId)

### StockMovement
- `id`: Primary key
- `tenantId`: Tenant identifier
- `branchId`: Branch reference
- `productId`: Product reference
- `type`: Movement type (IN, OUT, ADJUSTMENT, TRANSFER)
- `reason`: Movement reason
- `quantity`: Quantity moved
- `note`: Optional notes
- `createdByUserId`: User who created the movement
- `createdAt`: Creation timestamp

## Movement Types

### IN
Increases stock for reasons:
- `PURCHASE`: Stock from supplier purchase
- `RETURN`: Customer return
- `TRANSFER_IN`: Received from transfer

### OUT
Decreases stock for reasons:
- `SALE`: Sold to customer
- `LOSS`: Lost/damaged
- `TRANSFER_OUT`: Sent via transfer

### ADJUSTMENT
Manual stock correction:
- `ADJUSTMENT_IN`: Increase stock
- `ADJUSTMENT_OUT`: Decrease stock

## API Endpoints

### Stock

#### GET /api/v1/branches/{branchId}/stock
List stock for a branch with pagination.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `search`: Search by product name or SKU
- `lowStock`: Show only items below minStock

**Response:**
```json
{
  "success": true,
  "data": {
    "data": [
      {
        "id": 1,
        "branchId": 1,
        "productId": 1,
        "product": {
          "id": 1,
          "name": "Wireless Mouse",
          "sku": "MOUSE-WL-001"
        },
        "quantity": 150,
        "updatedAt": "2024-01-16T10:30:00"
      }
    ],
    "total": 50,
    "page": 0,
    "size": 20,
    "totalPages": 3
  }
}
```

#### GET /api/v1/branches/{branchId}/stock/{productId}
Get stock for a specific product in a branch.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "branchId": 1,
    "productId": 1,
    "product": {
      "id": 1,
      "name": "Wireless Mouse",
      "sku": "MOUSE-WL-001",
      "minStock": 10
    },
    "quantity": 150,
    "isLowStock": false,
    "updatedAt": "2024-01-16T10:30:00"
  }
}
```

### Movements

#### POST /api/v1/branches/{branchId}/movements
Create a stock movement.

**Request (IN):**
```json
{
  "productId": 1,
  "type": "IN",
  "reason": "PURCHASE",
  "quantity": 100,
  "note": "Supplier purchase #12345"
}
```

**Request (OUT):**
```json
{
  "productId": 1,
  "type": "OUT",
  "reason": "SALE",
  "quantity": 5,
  "note": "Order #67890"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "branchId": 1,
    "productId": 1,
    "type": "IN",
    "reason": "PURCHASE",
    "quantity": 100,
    "note": "Supplier purchase #12345",
    "createdBy": {
      "id": 2,
      "name": "John Doe"
    },
    "createdAt": "2024-01-16T10:30:00"
  }
}
```

#### GET /api/v1/branches/{branchId}/movements
List movements for a branch with pagination.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `type`: Filter by movement type
- `reason`: Filter by reason
- `productId`: Filter by product
- `startDate`: Filter by start date
- `endDate`: Filter by end date

**Response:**
```json
{
  "success": true,
  "data": {
    "data": [...],
    "total": 500,
    "page": 0,
    "size": 20,
    "totalPages": 25
  }
}
```

### Transfers

#### POST /api/v1/transfers
Create a transfer between branches.

**Request:**
```json
{
  "originBranchId": 1,
  "destinationBranchId": 2,
  "productId": 1,
  "quantity": 50,
  "note": "Stock replenishment"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "originBranchId": 1,
    "destinationBranchId": 2,
    "productId": 1,
    "product": {
      "id": 1,
      "name": "Wireless Mouse",
      "sku": "MOUSE-WL-001"
    },
    "quantity": 50,
    "status": "COMPLETED",
    "originMovement": {
      "id": 1,
      "type": "TRANSFER_OUT",
      "quantity": 50
    },
    "destinationMovement": {
      "id": 2,
      "type": "TRANSFER_IN",
      "quantity": 50
    },
    "createdAt": "2024-01-16T10:30:00"
  }
}
```

## Business Rules

### Stock Invariants

1. **Stock Cannot Be Negative**
   - All OUT movements validate sufficient stock
   - Returns 409 if stock insufficient

2. **Optimistic Locking**
   - Stock updates use version field
   - Concurrent updates return 409
   - Client must retry with fresh data

3. **Transfer Validation**
   - Origin and destination must be different
   - Origin must have sufficient stock
   - Both movements created atomically
   - Failure rolls back entire transaction

4. **Active Resources Only**
   - Product must be active
   - Branch must be active
   - User must be active

### Low Stock Alert

A product is considered low stock when:
```java
quantity < product.minStock
```

### Transfer Process

1. Validate origin has sufficient stock (with optimistic lock)
2. Create TRANSFER_OUT movement at origin
3. Create TRANSFER_IN movement at destination
4. Update stock at both branches
5. Wrap in @Transactional with READ_COMMITTED isolation

## Database Schema

```sql
CREATE TABLE branch_product_stock (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  branch_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT DEFAULT 0 NOT NULL,
  version BIGINT DEFAULT 0 NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tenant_branch_product (tenant_id, branch_id, product_id),
  INDEX idx_branch_product (branch_id, product_id),
  INDEX fk_stock_tenant (tenant_id),
  INDEX fk_stock_branch (branch_id),
  INDEX fk_stock_product (product_id),
  FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  FOREIGN KEY (branch_id) REFERENCES branches(id),
  FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE stock_movements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  branch_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  type ENUM('IN', 'OUT', 'ADJUSTMENT', 'TRANSFER') NOT NULL,
  reason ENUM('PURCHASE', 'SALE', 'LOSS', 'RETURN', 'ADJUSTMENT_IN', 'ADJUSTMENT_OUT', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
  quantity INT NOT NULL,
  note TEXT,
  created_by_user_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_movements_tenant_branch_date (tenant_id, branch_id, created_at DESC),
  INDEX idx_tenant_product (tenant_id, product_id),
  INDEX idx_branch_date (branch_id, created_at DESC),
  INDEX fk_movements_tenant (tenant_id),
  INDEX fk_movements_branch (branch_id),
  INDEX fk_movements_product (product_id),
  INDEX fk_movements_user (created_by_user_id),
  FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  FOREIGN KEY (branch_id) REFERENCES branches(id),
  FOREIGN KEY (product_id) REFERENCES products(id),
  FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);
```

## Concurrency Handling

### Optimistic Locking Example

```java
try {
    // Read stock with version=5
    BranchProductStock stock = repository.findById(1L);

    // Update stock (version check)
    stock.setQuantity(150);
    repository.save(stock); // Checks version=5

} catch (OptimisticLockingFailureException e) {
    // Another user updated stock (version is now 6)
    throw new ConflictException(
        "STOCK_CONCURRENT_MODIFICATION",
        "Stock was modified by another user. Please refresh and try again."
    );
}
```

## Security

- All operations validate branch access
- Users can only access stock from their assigned branches
- Transfers between branches require access to both
- All operations scoped by tenant_id

## Dependencies

- **Catalog module**: For product validation
- **Branches module**: For branch access validation
- **Users module**: For user context in movements
- **Tenants module**: For tenant isolation
- **Shared kernel**: BaseEntity, exceptions, DTOs
