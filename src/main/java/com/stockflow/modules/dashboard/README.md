# Dashboard Module

## Overview

The Dashboard module provides aggregated metrics and analytics for the StockFlow PRO system. It implements RF-010 from the PRD with Redis caching for performance optimization (ADR-0005).

## Architecture

### Domain Layer

**Models:**
- `DashboardMetrics` - Value object containing dashboard metrics (products count, low stock, movements)
- `TopProductMovement` - Value object for top product movement statistics

**Repository:**
- `DashboardRepository` - Interface for dashboard data aggregation queries
- `DashboardRepositoryImpl` - JPA implementation with optimized SQL queries

### Application Layer

**DTOs:**
- `DashboardOverviewResponse` - Complete dashboard response with metrics and top products

**Services:**
- `DashboardService` - Interface for dashboard operations
- `DashboardServiceImpl` - Implementation with Redis caching (@Cacheable)

### Infrastructure Layer

**Controllers:**
- `DashboardController` - REST endpoints for dashboard operations

## Features

### 1. Dashboard Overview Metrics

**Endpoint:** `GET /api/dashboard/overview?branchId={branchId}`

**Metrics provided:**
- Total active products for the tenant
- Products with stock below minimum threshold
- Total stock movements (all time)
- Recent movements (last 7 days)
- Top 10 most moved products

**Cache Strategy:**
- TTL: 5 minutes (300 seconds)
- Cache key includes tenantId and optionally branchId
- Automatically invalidated on stock movements

### 2. Branch-Specific Dashboard

**Endpoint:** `GET /api/dashboard/overview?branchId={branchId}`

Provides the same metrics scoped to a specific branch, useful for:
- Branch managers
- Operational dashboards
- Branch-level analytics

## Cache Configuration

### Cache Names

```java
DASHBOARD_OVERVIEW = "dashboardOverview"  // 5 minutes TTL
DASHBOARD_BRANCH = "dashboardBranch"      // 5 minutes TTL
TOP_PRODUCTS = "topProducts"              // 10 minutes TTL
```

### Cache Invalidation

Cache is automatically evicted when:
- Stock movements are created (`InventoryService.createMovement`)
- Stock transfers are executed (`InventoryService.transferStock`)

This ensures dashboard data remains near real-time while still benefiting from caching.

## Performance Optimizations

### Database Indexes Used

The following indexes (defined in PRD v2.0) are leveraged:

1. **Products:**
   - `idx_products_tenant_active_name` - Fast filtering of active products
   - `uk_tenant_sku` - Unique constraint for SKU lookup

2. **Stock:**
   - `uk_tenant_branch_product` - Fast stock lookups
   - `idx_branch_product` - Branch-product queries

3. **Movements:**
   - `idx_movements_tenant_branch_date` - Recent movement queries
   - `idx_tenant_product` - Product movement aggregations

### Query Optimizations

- Native SQL for optimal performance
- Aggregations performed at database level
- COALESCE for handling NULL values
- Proper JOINs for efficient data retrieval

## Security

- All endpoints require authentication (JWT)
- Data automatically scoped to current tenant
- Tenant isolation enforced at all layers
- Optional branchId parameter for branch-level filtering

## API Documentation

### Get Dashboard Overview

```http
GET /api/dashboard/overview
Authorization: Bearer {jwt_token}
```

**Query Parameters:**
- `branchId` (optional): Filter metrics by branch

**Response:**
```json
{
  "metrics": {
    "totalActiveProducts": 150,
    "lowStockItems": 12,
    "totalMovements": 5430,
    "recentMovements": 230
  },
  "topProducts": [
    {
      "productId": 1,
      "productName": "Wireless Mouse",
      "productSku": "WM-001",
      "movementCount": 45,
      "totalQuantity": 320
    }
  ]
}
```

## Implementation Details

### Module Structure

```
modules/dashboard/
├── domain/
│   ├── model/
│   │   ├── DashboardMetrics.java       # Value object for metrics
│   │   └── TopProductMovement.java     # Value object for top products
│   └── repository/
│       └── DashboardRepository.java    # Repository interface
├── application/
│   ├── dto/
│   │   └── DashboardOverviewResponse.java  # Response DTO
│   └── service/
│       ├── DashboardService.java       # Service interface
│       └── DashboardServiceImpl.java   # Service with @Cacheable
└── infrastructure/
    ├── persistence/
    │   └── DashboardRepositoryImpl.java # JPA implementation
    └── web/
        └── DashboardController.java    # REST controller
```

### Key Design Decisions

1. **Value Objects for Immutable Data**
   - `DashboardMetrics` and `TopProductMovement` are immutable value objects
   - Ensures thread-safety and prevents accidental modifications

2. **Aggressive Caching**
   - Dashboard queries are expensive (multiple aggregations)
   - Cache TTL of 5 minutes balances freshness and performance
   - Top products cached longer (10 minutes) as they change less frequently

3. **Cache Invalidation Strategy**
   - `@CacheEvict` on `InventoryService` methods
   - `allEntries = true` ensures complete cache cleanup
   - Simplicity over granular invalidation (acceptable trade-off)

4. **Native SQL for Performance**
   - Complex aggregations optimized at database level
   - Better performance than JPQL for aggregations
   - Leverages database-specific optimizations

## Dependencies

- Spring Data Redis - Cache implementation
- Spring Cache - Abstraction for caching
- JPA/Hibernate - Database queries
- Jackson - JSON serialization

## Future Enhancements

Potential improvements for future iterations:

1. **Additional Metrics:**
   - Sales trends over time
   - Inventory turnover ratio
   - Branch performance comparison
   - Product velocity analysis

2. **Time-Series Data:**
   - Historical metrics storage
   - Trend analysis
   - Predictive analytics

3. **Custom Dashboards:**
   - User-configurable dashboards
   - Saved filters and views
   - Export functionality

4. **Real-Time Updates:**
   - WebSocket support for live updates
   - Push notifications for critical alerts
   - Real-time stock level monitoring

## References

- PRD RF-010 - Dashboard requirements
- ADR-0005 - Cache and invalidation strategy
- [CONVENCOES.md](../../../../../../../../docs/tasks/00-CONVENCOES.md) - Code conventions
- Cache configuration in `shared/infrastructure/cache/CacheConfig.java`

## Related Modules

- **Catalog** - Provides product data
- **Inventory** - Provides stock and movement data
- **Shared** - Provides caching infrastructure
