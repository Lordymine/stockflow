package com.stockflow.shared.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for JWT-based authentication.
 *
 * <p>This configuration:</p>
 * <ul>
 *   <li>Disables CSRF and CORS for stateless API</li>
 *   <li>Configures public and private endpoints</li>
 *   <li>Sets up JWT authentication filter</li>
 *   <li>Configures authentication provider with BCrypt</li>
 *   <li>Enables method-level security with @PreAuthorize</li>
 * </ul>
 *
 * <p>Public endpoints (no authentication required):</p>
 * <ul>
 *   <li>{@code /api/v1/auth/login} - User login</li>
 *   <li>{@code /api/v1/auth/refresh} - Token refresh</li>
 *   <li>{@code /api/v1/auth/bootstrap} - Initial tenant setup</li>
 *   <li>{@code /actuator/health} - Health check</li>
 *   <li>{@code /swagger-ui/**} - API documentation</li>
 *   <li>{@code /v3/api-docs/**} - OpenAPI spec</li>
 * </ul>
 *
 * <p>All other endpoints require authentication.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(AbstractHttpConfigurer::disable)

            // Disable CORS (configure separately if needed)
            .cors(AbstractHttpConfigurer::disable)

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public authentication endpoints
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/bootstrap"
                ).permitAll()

                // Public actuator endpoint
                .requestMatchers("/actuator/health").permitAll()

                // Public API documentation
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()

                // Test endpoint
                .requestMatchers("/api/v1/test/**").permitAll()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Configure exception handling
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )

            // Set session management to stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures the authentication provider with BCrypt password encoder.
     *
     * @return the configured AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Provides the authentication manager for login authentication.
     *
     * @param config the authentication configuration
     * @return the AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Provides BCrypt password encoder for secure password hashing.
     *
     * @return the PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
