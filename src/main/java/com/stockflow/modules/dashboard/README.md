# Dashboard Module

## Purpose

Responsible for aggregating and presenting operational metrics and insights.

## Responsibilities

- Generate overview metrics
- Calculate stock statistics
- Identify low stock items
- Track recent movements
- Rank top products
- Cache expensive computations

## Boundaries

**This module DOES:**
- Aggregate data from other modules
- Calculate metrics and KPIs
- Present summary views
- Cache results for performance

**This module DOES NOT:**
- Manage stock (delegated to Inventory module)
- Manage products (delegated to Catalog module)
- Manage branches (delegated to Branches module)
- Perform business logic (delegates to domain modules)

## API Endpoints

### GET /api/v1/dashboard/overview
Get dashboard overview metrics.

**Query Parameters:**
- `branchId`: Optional branch filter (default: all accessible branches)

**Response:**
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalProducts": 250,
      "activeProducts": 245,
      "totalBranches": 5,
      "lowStockItems": 12
    },
    "recentMovements": [
      {
        "id": 100,
        "type": "IN",
        "productName": "Wireless Mouse",
        "branchName": "Main Warehouse",
        "quantity": 100,
        "reason": "PURCHASE",
        "createdAt": "2024-01-16T09:30:00"
      }
    ],
    "lowStockProducts": [
      {
        "productId": 5,
        "productName": "USB-C Cable",
        "sku": "CABLE-USB-C-001",
        "totalStock": 3,
        "minStock": 20,
        "branchesAffected": [
          {
            "branchId": 1,
            "branchName": "Main Warehouse",
            "quantity": 3
          }
        ]
      }
    ],
    "topProducts": [
      {
        "productId": 1,
        "productName": "Wireless Mouse",
        "sku": "MOUSE-WL-001",
        "totalMovements": 150,
        "totalQuantity": 5000
      }
    ],
    "branchStockSummary": [
      {
        "branchId": 1,
        "branchName": "Main Warehouse",
        "totalProducts": 200,
        "totalItems": 15000
      }
    ],
    "generatedAt": "2024-01-16T10:30:00"
  }
}
```

## Metrics

### Summary Metrics

| Metric | Description | Source |
|--------|-------------|--------|
| `totalProducts` | Total active products in catalog | Catalog module |
| `activeProducts` | Active products (isActive=true) | Catalog module |
| `totalBranches` | Total accessible branches | Branches module |
| `lowStockItems` | Products below minStock | Inventory module |

### Recent Movements
- Last 10 movements across all accessible branches
- Ordered by date (most recent first)
- Includes product and branch details

### Low Stock Products
- Products where total quantity < minStock
- Shows affected branches
- Ordered by severity (lowest first)

### Top Products
- Most moved products in last 30 days
- Ordered by total movement count
- Includes total quantity moved

### Branch Stock Summary
- Total products per branch
- Total items (sum of quantities) per branch

## Caching Strategy

### Cache Configuration
- TTL: 5 minutes (300 seconds)
- Key pattern: `dashboard:overview:tenant:{tenantId}:branch:{branchId}`
- Invalidation: On any stock movement or product update

### Cache Implementation

```java
@Cacheable(
    value = "dashboard",
    key = "'overview:' + #tenantId + ':' + (#branchId ?: 'all')"
)
public DashboardOverviewDTO getOverview(Long tenantId, Long branchId) {
    // Expensive computation
    return computeDashboard(tenantId, branchId);
}

@CacheEvict(
    value = "dashboard",
    allEntries = true
)
public void invalidateCache() {
    // Called on stock movements
}
```

### Cache Keys Examples

```
dashboard:overview:1:all         # All branches for tenant 1
dashboard:overview:1:5           # Branch 5 for tenant 1
dashboard:overview:2:all         # All branches for tenant 2
```

## Performance Considerations

### Expensive Operations

1. **Top Products Calculation**
   - Requires aggregating movements from last 30 days
   - Can be slow on systems with many movements
   - Cached for 10 minutes separately

2. **Low Stock Detection**
   - Requires checking all products across all branches
   - Optimize with database indexes on (tenantId, branchId, productId)
   - Cached with main overview

3. **Movement Aggregation**
   - Join movements with products and branches
   - Index on (tenantId, branchId, createdAt) critical
   - Limit to 10 most recent

### Optimization Tips

1. **Use Materialized Views** (for very large deployments)
   - Pre-compute top products nightly
   - Refresh incrementally

2. **Batch Queries**
   - Fetch all data in 3-4 queries
   - Assemble in application layer

3. **Denormalize**
   - Store movement count on product table
   - Update via triggers or events

## Database Queries

### Recent Movements
```sql
SELECT
    m.id,
    m.type,
    m.reason,
    m.quantity,
    m.created_at,
    p.name AS product_name,
    p.sku,
    b.name AS branch_name
FROM stock_movements m
JOIN products p ON m.product_id = p.id
JOIN branches b ON m.branch_id = b.id
WHERE m.tenant_id = :tenantId
  AND (:branchId IS NULL OR m.branch_id = :branchId)
ORDER BY m.created_at DESC
LIMIT 10;
```

### Low Stock Products
```sql
SELECT
    p.id AS product_id,
    p.name AS product_name,
    p.sku,
    p.min_stock,
    SUM(s.quantity) AS total_stock
FROM products p
LEFT JOIN branch_product_stock s ON p.id = s.product_id
WHERE p.tenant_id = :tenantId
  AND p.is_active = true
  AND (:branchId IS NULL OR s.branch_id = :branchId)
GROUP BY p.id, p.name, p.sku, p.min_stock
HAVING SUM(s.quantity) < p.min_stock
ORDER BY total_stock ASC;
```

### Top Products
```sql
SELECT
    p.id AS product_id,
    p.name AS product_name,
    p.sku,
    COUNT(m.id) AS total_movements,
    SUM(m.quantity) AS total_quantity
FROM products p
JOIN stock_movements m ON p.id = m.product_id
WHERE p.tenant_id = :tenantId
  AND m.tenant_id = :tenantId
  AND m.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
  AND (:branchId IS NULL OR m.branch_id = :branchId)
GROUP BY p.id, p.name, p.sku
ORDER BY total_movements DESC
LIMIT 10;
```

## Security

- Metrics filtered by tenant_id
- Branch filter respects user's branch access
- No cross-tenant data leakage
- Cached data scoped by tenant

## Dependencies

- **Catalog module**: For product data
- **Inventory module**: For stock and movement data
- **Branches module**: For branch data
- **Tenants module**: For tenant isolation
- **Shared kernel**: DTOs
- **Redis**: For caching

## Future Enhancements

1. **Custom Date Ranges**
   - Allow users to specify date ranges for metrics

2. **Export Functionality**
   - Export dashboard data as CSV/PDF

3. **Real-time Updates**
   - WebSocket for live dashboard updates

4. **Advanced Analytics**
   - Trends over time
   - Forecasting
   - Anomaly detection
