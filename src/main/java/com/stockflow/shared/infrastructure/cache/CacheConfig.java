package com.stockflow.shared.infrastructure.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Redis cache manager with custom TTL settings.
 *
 * <p>This configuration sets up different cache TTLs according to the ADR-0005:</p>
 * <ul>
 *   <li>Dashboard overview: 5 minutes (300 seconds)</li>
 *   <li>Top products: 10 minutes (600 seconds)</li>
 *   <li>Default cache: 10 minutes (600 seconds)</li>
 * </ul>
 *
 * <p><strong>Cache Invalidation Strategy:</strong></p>
 * <ul>
 *   <li>Dashboard cache is invalidated on any stock movement or transfer</li>
 *   <li>Cache keys include tenant ID for multi-tenancy isolation</li>
 *   <li>Branch-specific caches include branch ID in the key</li>
 * </ul>
 *
 * @see com.stockflow.modules.dashboard.application.service.DashboardService
 * @see com.stockflow.modules.inventory.application.service.InventoryService
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache name for dashboard overview metrics.
     * TTL: 5 minutes (300 seconds).
     */
    public static final String DASHBOARD_OVERVIEW = "dashboardOverview";

    /**
     * Cache name for top products by movement.
     * TTL: 10 minutes (600 seconds).
     */
    public static final String TOP_PRODUCTS = "topProducts";

    /**
     * Cache name for dashboard metrics by branch.
     * TTL: 5 minutes (300 seconds).
     */
    public static final String DASHBOARD_BRANCH = "dashboardBranch";

    /**
     * Default TTL for dashboard cache in seconds.
     */
    public static final int DASHBOARD_TTL_SECONDS = 300;

    /**
     * TTL for top products cache in seconds.
     */
    public static final int TOP_PRODUCTS_TTL_SECONDS = 600;

    /**
     * Prefix for all cache keys to avoid conflicts.
     */
    private static final String CACHE_PREFIX = "stockflow:";

    /**
     * Configures RedisCacheManager with custom TTL settings per cache.
     *
     * @param connectionFactory Redis connection factory
     * @return configured RedisCacheManager
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Create serializer configuration
        RedisSerializationContext.SerializationPair<Object> jsonSerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                );

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .prefixCacheNameWith(CACHE_PREFIX)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(jsonSerializer);

        // Per-cache configurations with specific TTLs
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // Dashboard overview: 5 minutes TTL
        cacheConfigs.put(DASHBOARD_OVERVIEW, defaultConfig
                .entryTtl(Duration.ofSeconds(DASHBOARD_TTL_SECONDS)));

        // Dashboard by branch: 5 minutes TTL
        cacheConfigs.put(DASHBOARD_BRANCH, defaultConfig
                .entryTtl(Duration.ofSeconds(DASHBOARD_TTL_SECONDS)));

        // Top products: 10 minutes TTL
        cacheConfigs.put(TOP_PRODUCTS, defaultConfig
                .entryTtl(Duration.ofSeconds(TOP_PRODUCTS_TTL_SECONDS)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }
}
