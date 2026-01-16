package com.stockflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for StockFlow PRO.
 *
 * <p>StockFlow PRO is a multi-tenant inventory management system with branches support.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Multi-tenancy by column (tenant_id)</li>
 *   <li>Multi-branches with access control</li>
 *   <li>RBAC (ADMIN, MANAGER, STAFF)</li>
 *   <li>Inventory tracking with movements and transfers</li>
 *   <li>Redis caching for performance</li>
 * </ul>
 *
 * @author StockFlow Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class StockFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockFlowApplication.class, args);
    }

}
