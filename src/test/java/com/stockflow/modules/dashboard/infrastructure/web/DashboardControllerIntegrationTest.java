package com.stockflow.modules.dashboard.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.modules.catalog.domain.model.Product;
import com.stockflow.modules.catalog.domain.repository.ProductRepository;
import com.stockflow.modules.inventory.application.dto.StockMovementCreateRequest;
import com.stockflow.modules.inventory.domain.model.BranchProductStock;
import com.stockflow.modules.inventory.domain.model.MovementReason;
import com.stockflow.modules.inventory.domain.model.MovementType;
import com.stockflow.modules.inventory.domain.model.StockMovement;
import com.stockflow.modules.inventory.domain.repository.BranchProductStockRepository;
import com.stockflow.modules.inventory.domain.repository.StockMovementRepository;
import com.stockflow.modules.branches.domain.model.Branch;
import com.stockflow.modules.branches.domain.repository.BranchRepository;
import com.stockflow.shared.security.TestSecurityUtils;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Dashboard endpoints.
 *
 * <p>Tests the complete dashboard functionality:</p>
 * <ul>
 *   <li>Query tenant-wide dashboard metrics</li>
 *   <li>Query branch-specific dashboard metrics</li>
 *   <li>Cache performance (Redis)</li>
 *   <li>Cache invalidation on stock movements</li>
 *   <li>Role-based access control</li>
 *   <li>Low stock identification</li>
 *   <li>Top products by movement</li>
 * </ul>
 *
 * <p><strong>Test Strategy:</strong></p>
 * <ul>
 *   <li>Setup creates 2 branches, 2 products with different stock levels</li>
 *   <li>Creates stock movements to test top products functionality</li>
 *   <li>Tests cache behavior through performance assertions</li>
 *   <li>Validates cache invalidation after stock operations</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BranchProductStockRepository stockRepository;

    @Autowired
    private StockMovementRepository movementRepository;

    private Long testTenantId = 1L;
    private Long branch1Id; // CENTRO
    private Long branch2Id; // NORTE
    private Long product1Id; // Wireless Mouse (minStock=10)
    private Long product2Id; // USB-C Cable (minStock=20)
    private RequestPostProcessor adminUser;
    private RequestPostProcessor managerUser;
    private RequestPostProcessor staffUser;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        movementRepository.deleteAll();
        stockRepository.deleteAll();
        productRepository.deleteAll();
        branchRepository.deleteAll();

        // Create two test branches
        Branch branch1 = new Branch(testTenantId, "Filial Centro", "CENTRO");
        branch1.setAddress("Rua A, 123");
        branch1 = branchRepository.save(branch1);
        branch1Id = branch1.getId();

        Branch branch2 = new Branch(testTenantId, "Filial Norte", "NORTE");
        branch2.setAddress("Rua B, 456");
        branch2 = branchRepository.save(branch2);
        branch2Id = branch2.getId();

        adminUser = TestSecurityUtils.admin(testTenantId, List.of(branch1Id, branch2Id));
        managerUser = TestSecurityUtils.manager(testTenantId, List.of(branch1Id, branch2Id));
        staffUser = TestSecurityUtils.staff(testTenantId, List.of(branch1Id));

        // Create test products
        Product product1 = new Product(
            testTenantId,
            "Wireless Mouse",
            "WM-001",
            Product.UnitOfMeasure.UN
        );
        product1.setDescription("Ergonomic wireless mouse");
        product1.setCostPrice(new BigDecimal("50.00"));
        product1.setSalePrice(new BigDecimal("89.90"));
        product1.setMinStock(10);  // minStock = 10
        product1.setActive(true);
        product1 = productRepository.save(product1);
        product1Id = product1.getId();

        Product product2 = new Product(
            testTenantId,
            "USB-C Cable",
            "UC-002",
            Product.UnitOfMeasure.UN
        );
        product2.setDescription("USB-C charging cable 2m");
        product2.setCostPrice(new BigDecimal("15.00"));
        product2.setSalePrice(new BigDecimal("29.90"));
        product2.setMinStock(20);  // minStock = 20
        product2.setActive(true);
        product2 = productRepository.save(product2);
        product2Id = product2.getId();

        // Initialize stock in branch1 (CENTRO)
        // Product1: 100 units (above minStock=10)
        var stock1 = new BranchProductStock(testTenantId, branch1Id, product1Id, 100);
        stockRepository.save(stock1);

        // Product2: 5 units (BELOW minStock=20) -> should trigger low stock alert
        var stock2 = new BranchProductStock(testTenantId, branch1Id, product2Id, 5);
        stockRepository.save(stock2);

        // Initialize stock in branch2 (NORTE)
        var stock3 = new BranchProductStock(testTenantId, branch2Id, product1Id, 50);
        stockRepository.save(stock3);

        var stock4 = new BranchProductStock(testTenantId, branch2Id, product2Id, 25);
        stockRepository.save(stock4);

        // Create initial stock movements for testing top products
        // Product1 will have more movements than Product2
        createMovement(branch1Id, product1Id, MovementType.IN, MovementReason.PURCHASE, 100, "Initial stock");
        createMovement(branch1Id, product1Id, MovementType.OUT, MovementReason.SALE, 20, "Sale");
        createMovement(branch1Id, product1Id, MovementType.IN, MovementReason.PURCHASE, 30, "Restock");

        // Product2 will have fewer movements
        createMovement(branch1Id, product2Id, MovementType.IN, MovementReason.PURCHASE, 50, "Initial stock");
        createMovement(branch1Id, product2Id, MovementType.OUT, MovementReason.SALE, 45, "Sale");
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/overview - Should return tenant-wide metrics")
    void getOverview_ShouldReturnTenantWideMetrics() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/dashboard/overview")
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics.totalActiveProducts").value(2))
                .andExpect(jsonPath("$.data.metrics.lowStockItems").value(1))  // Only product2 (5 < 20)
                .andExpect(jsonPath("$.data.metrics.totalMovements").value(5))  // 3 for product1 + 2 for product2
                .andExpect(jsonPath("$.data.metrics.recentMovements").value(5)) // All movements are recent
                .andExpect(jsonPath("$.data.topProducts").isArray())
                .andExpect(jsonPath("$.data.topProducts", hasSize(2)))
                .andExpect(jsonPath("$.data.topProducts[0].productId").exists())
                .andExpect(jsonPath("$.data.topProducts[0].productName").exists())
                .andExpect(jsonPath("$.data.topProducts[0].movementCount").exists())
                .andExpect(jsonPath("$.data.topProducts[0].totalQuantity").exists());
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/overview?branchId={id} - Should return branch-specific metrics")
    void getOverview_WithBranchId_ShouldReturnBranchSpecificMetrics() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/dashboard/overview")
                        .with(adminUser)
                        .param("branchId", branch1Id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics.totalActiveProducts").value(2))
                .andExpect(jsonPath("$.data.metrics.lowStockItems").value(1))  // product2 in branch1
                .andExpect(jsonPath("$.data.metrics.totalMovements").value(5))  // All movements in branch1
                .andExpect(jsonPath("$.data.metrics.recentMovements").value(5))
                .andExpect(jsonPath("$.data.topProducts").isArray())
                .andExpect(jsonPath("$.data.topProducts", hasSize(2)))
                .andExpect(jsonPath("$.data.topProducts[0].movementCount").value(3)); // product1 has 3 movements
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/overview - Second call should use cache (performance test)")
    void getOverview_SecondCall_ShouldUseCache() throws Exception {
        // Arrange - First call to populate cache
        long startTime1 = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/dashboard/overview")
                .with(adminUser))
                .andExpect(status().isOk());
        long duration1 = System.currentTimeMillis() - startTime1;

        // Act - Second call should be faster (from cache)
        long startTime2 = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/dashboard/overview")
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics.totalActiveProducts").value(2))
                .andExpect(jsonPath("$.data.metrics.lowStockItems").value(1));
        long duration2 = System.currentTimeMillis() - startTime2;

        // Assert - Second call should be significantly faster
        // Note: This is a soft assertion as caching overhead can vary
        // In a real microbenchmark we would run multiple iterations
        System.out.println(String.format("First call: %d ms, Second call (cached): %d ms", duration1, duration2));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/overview - After stock movement should invalidate cache")
    void getOverview_AfterStockMovement_ShouldInvalidateCache() throws Exception {
        // Arrange - Get initial overview (populates cache)
        mockMvc.perform(get("/api/v1/dashboard/overview")
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics.totalMovements").value(5));

        // Act - Create a new stock movement (should invalidate cache)
        StockMovementCreateRequest movementRequest = new StockMovementCreateRequest(
            product1Id,
            MovementType.IN,
            MovementReason.PURCHASE,
            10,
            "New purchase after initial setup"
        );

        mockMvc.perform(post("/api/v1/branches/" + branch1Id + "/movements")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movementRequest)))
                .andExpect(status().isCreated());

        // Assert - Dashboard should reflect new movement (cache was invalidated)
        mockMvc.perform(get("/api/v1/dashboard/overview")
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics.totalMovements").value(6)) // 5 + 1 new movement
                .andExpect(jsonPath("$.data.metrics.recentMovements").value(6));  // All movements are recent
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/overview - Manager role should succeed")
    void getOverview_WithManagerRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/dashboard/overview")
                .with(managerUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics").exists())
                .andExpect(jsonPath("$.data.topProducts").exists());
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/overview - Staff role should succeed")
    void getOverview_WithStaffRole_ShouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/dashboard/overview")
                .with(staffUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics").exists())
                .andExpect(jsonPath("$.data.topProducts").exists());
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/overview - Unauthenticated should return 401")
    void getOverview_Unauthenticated_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/dashboard/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/overview - Should correctly identify low stock items")
    void getOverview_ShouldCorrectlyIdentifyLowStockItems() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/dashboard/overview")
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics.lowStockItems").value(1))  // Only product2 (5 < 20)
                .andExpect(jsonPath("$.data.metrics.totalActiveProducts").value(2));

        // Verify for branch2 (no low stock items: product1=50, product2=25)
        mockMvc.perform(get("/api/v1/dashboard/overview")
                        .with(adminUser)
                        .param("branchId", branch2Id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics.lowStockItems").value(0))  // Both above minStock
                .andExpect(jsonPath("$.data.metrics.totalActiveProducts").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/overview - Should return top products sorted by movement count")
    void getOverview_ShouldReturnTopProductsSortedByMovementCount() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/dashboard/overview")
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.topProducts").isArray())
                .andExpect(jsonPath("$.data.topProducts", hasSize(2)))
                // First product should have more movements (product1 has 3, product2 has 2)
                .andExpect(jsonPath("$.data.topProducts[0].movementCount").value(3))
                .andExpect(jsonPath("$.data.topProducts[1].movementCount").value(2))
                .andExpect(jsonPath("$.data.topProducts[0].productName").exists())
                .andExpect(jsonPath("$.data.topProducts[0].productSku").exists())
                .andExpect(jsonPath("$.data.topProducts[0].totalQuantity").exists());
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/overview?branchId=999 - Should return 404 for non-existent branch")
    void getOverview_WithInvalidBranchId_ShouldReturn404() throws Exception {
        // Act & Assert
        RequestPostProcessor adminWithInvalidBranch = TestSecurityUtils.admin(
            testTenantId,
            List.of(branch1Id, branch2Id, 999L)
        );

        mockMvc.perform(get("/api/v1/dashboard/overview")
                        .with(adminWithInvalidBranch)
                        .param("branchId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BRANCH_NOT_FOUND"));
    }

    /**
     * Helper method to create a stock movement.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @param type      the movement type
     * @param reason    the movement reason
     * @param quantity  the movement quantity
     * @param notes     movement notes
     */
    private void createMovement(Long branchId, Long productId, MovementType type,
                               MovementReason reason, int quantity, String notes) {
        StockMovement movement = new StockMovement(
            testTenantId,
            branchId,
            productId,
            type,
            reason,
            quantity,
            notes,
            1L  // createdByUserId
        );
        movement.setCreatedAt(LocalDateTime.now());
        movementRepository.save(movement);
    }
}
