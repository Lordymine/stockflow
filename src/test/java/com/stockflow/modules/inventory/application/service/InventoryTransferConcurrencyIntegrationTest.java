package com.stockflow.modules.inventory.application.service;

import com.stockflow.modules.branches.domain.model.Branch;
import com.stockflow.modules.branches.domain.repository.BranchRepository;
import com.stockflow.modules.catalog.domain.model.Product;
import com.stockflow.modules.catalog.domain.repository.ProductRepository;
import com.stockflow.modules.inventory.application.dto.TransferStockRequest;
import com.stockflow.modules.inventory.domain.model.BranchProductStock;
import com.stockflow.modules.inventory.domain.repository.BranchProductStockRepository;
import com.stockflow.modules.inventory.domain.repository.StockMovementRepository;
import com.stockflow.modules.tenants.domain.model.Tenant;
import com.stockflow.modules.tenants.domain.repository.TenantRepository;
import com.stockflow.shared.domain.exception.InsufficientStockException;
import com.stockflow.shared.domain.exception.ValidationException;
import com.stockflow.shared.infrastructure.security.TenantContext;
import com.stockflow.shared.testing.TestcontainersIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrency tests for inventory transfers.
 * 
 * <p>
 * This test requires Docker/Testcontainers to properly test concurrent
 * database operations. It will be skipped if Docker is not available.
 * </p>
 */
@SpringBootTest
@DisplayName("InventoryService - Concurrency Tests")
class InventoryTransferConcurrencyIntegrationTest extends TestcontainersIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BranchProductStockRepository stockRepository;

    @Autowired
    private StockMovementRepository movementRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Long tenantId;
    private Long sourceBranchId;
    private Long destinationBranchId;
    private Long productId;

    @BeforeEach
    void setUp() {
        movementRepository.deleteAll();
        stockRepository.deleteAll();
        productRepository.deleteAll();
        branchRepository.deleteAll();
        tenantRepository.deleteAll();

        Tenant tenant = tenantRepository.save(new Tenant("Test Tenant", "test-tenant-concurrency"));
        tenantId = tenant.getId();

        Branch source = branchRepository.save(new Branch(tenantId, "Source Branch", "SRC"));
        Branch destination = branchRepository.save(new Branch(tenantId, "Destination Branch", "DST"));
        sourceBranchId = source.getId();
        destinationBranchId = destination.getId();

        Product product = new Product(tenantId, "Product A", "SKU-1", Product.UnitOfMeasure.UN);
        product.setActive(true);
        product = productRepository.save(product);
        productId = product.getId();

        stockRepository.save(new BranchProductStock(tenantId, sourceBranchId, productId, 20));
        stockRepository.save(new BranchProductStock(tenantId, destinationBranchId, productId, 0));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Concurrent transfers should preserve stock totals")
    void transferStock_ConcurrentRequests_ShouldPreserveTotals() throws Exception {
        int threads = 10;
        int initialTotal = 20;
        AtomicInteger successCount = new AtomicInteger();
        List<Exception> failures = Collections.synchronizedList(new ArrayList<>());

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                TenantContext.setTenantId(tenantId);
                ready.countDown();
                try {
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        return;
                    }
                    inventoryService.transferStock(new TransferStockRequest(
                            sourceBranchId,
                            destinationBranchId,
                            productId,
                            1,
                            "Concurrent transfer"));
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    failures.add(ex);
                } finally {
                    TenantContext.clear();
                    done.countDown();
                }
            });
        }

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        executor.shutdownNow();

        BranchProductStock sourceStock = stockRepository
                .findByTenantIdAndBranchIdAndProductId(sourceBranchId, productId, tenantId)
                .orElseThrow();
        BranchProductStock destinationStock = stockRepository
                .findByTenantIdAndBranchIdAndProductId(destinationBranchId, productId, tenantId)
                .orElseThrow();

        assertThat(sourceStock.getQuantity() + destinationStock.getQuantity()).isEqualTo(initialTotal);
        assertThat(destinationStock.getQuantity()).isEqualTo(successCount.get());
        assertThat(sourceStock.getQuantity()).isEqualTo(initialTotal - successCount.get());
        assertThat(successCount.get()).isGreaterThan(0);

        if (!failures.isEmpty()) {
            assertThat(failures).allSatisfy(
                    ex -> assertThat(ex).isInstanceOfAny(ValidationException.class, InsufficientStockException.class));
        }
    }
}
