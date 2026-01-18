package com.stockflow.shared.infrastructure.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
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
 * <p>
 * This configuration sets up different cache TTLs according to the ADR-0005:
 * </p>
 * <ul>
 * <li>Dashboard overview: 5 minutes (300 seconds)</li>
 * <li>Top products: 10 minutes (600 seconds)</li>
 * <li>Default cache: 10 minutes (600 seconds)</li>
 * </ul>
 *
 * <p>
 * <strong>Cache Invalidation Strategy:</strong>
 * </p>
 * <ul>
 * <li>Dashboard cache is invalidated on any stock movement or transfer</li>
 * <li>Cache keys include tenant ID for multi-tenancy isolation</li>
 * <li>Branch-specific caches include branch ID in the key</li>
 * </ul>
 *
 * @see com.stockflow.modules.dashboard.application.service.DashboardService
 * @see com.stockflow.modules.inventory.application.service.InventoryService
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

        private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

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
         * Creates an ObjectMapper configured for Redis serialization.
         * 
         * <p>
         * This mapper is configured to handle polymorphic types correctly,
         * which is necessary for serializing collections and domain objects.
         * </p>
         *
         * @return configured ObjectMapper for Redis
         */
        private ObjectMapper createRedisObjectMapper() {
                ObjectMapper objectMapper = new ObjectMapper();

                // Register Java 8 date/time module
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // Configure polymorphic type handling for all non-final types
                PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                                .allowIfBaseType(Object.class)
                                .build();

                objectMapper.activateDefaultTyping(
                                ptv,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                return objectMapper;
        }

        /**
         * Configures RedisCacheManager with custom TTL settings per cache.
         *
         * @param connectionFactory Redis connection factory
         * @return configured RedisCacheManager
         */
        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                // Create serializer with properly configured ObjectMapper
                GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(
                                createRedisObjectMapper());

                RedisSerializationContext.SerializationPair<Object> serializerPair = RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer);

                // Default cache configuration
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10))
                                .disableCachingNullValues()
                                .prefixCacheNameWith(CACHE_PREFIX)
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(
                                                                new StringRedisSerializer()))
                                .serializeValuesWith(serializerPair);

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

        /**
         * Provides a custom error handler for cache operations.
         * 
         * <p>
         * This handler logs errors but allows the application to continue
         * by falling back to the underlying data source when cache operations fail.
         * </p>
         *
         * @return cache error handler that logs errors instead of throwing exceptions
         */
        @Override
        public CacheErrorHandler errorHandler() {
                return new CacheErrorHandler() {
                        @Override
                        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                                log.warn("Cache GET error for cache '{}', key '{}': {}. Falling back to database.",
                                                cache.getName(), key, exception.getMessage());
                                // Evict the corrupted cache entry
                                try {
                                        cache.evict(key);
                                        log.info("Evicted corrupted cache entry for key '{}'", key);
                                } catch (Exception e) {
                                        log.warn("Failed to evict cache entry: {}", e.getMessage());
                                }
                        }

                        @Override
                        public void handleCachePutError(RuntimeException exception, Cache cache, Object key,
                                        Object value) {
                                log.warn("Cache PUT error for cache '{}', key '{}': {}",
                                                cache.getName(), key, exception.getMessage());
                        }

                        @Override
                        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                                log.warn("Cache EVICT error for cache '{}', key '{}': {}",
                                                cache.getName(), key, exception.getMessage());
                        }

                        @Override
                        public void handleCacheClearError(RuntimeException exception, Cache cache) {
                                log.warn("Cache CLEAR error for cache '{}': {}",
                                                cache.getName(), exception.getMessage());
                        }
                };
        }
}
