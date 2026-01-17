package com.stockflow.shared.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for the application.
 *
 * <p>Configures CORS settings, Jackson ObjectMapper, and other web-related configurations.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures the Jackson ObjectMapper for JSON serialization.
     *
     * <p>Enables support for Java 8 date/time types (LocalDateTime, etc.)
     * and configures date formatting.</p>
     *
     * @return the configured ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Configures CORS mappings for the application.
     *
     * <p>In production, CORS should be configured to allow only specific origins.
     * Currently allows all origins for development purposes.</p>
     *
     * @param registry the CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:4200",  // Angular dev server
                "http://localhost:3000"   // Alternative dev server
            )
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
