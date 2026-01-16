package com.stockflow.shared.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.shared.application.dto.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Entry point for handling authentication errors.
 *
 * <p>This class is called when an unauthenticated user tries to access a protected resource.
 * It returns a standardized JSON error response instead of the default HTML error page.</p>
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        logger.warn("Unauthorized access attempt to: {}", request.getRequestURI());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiErrorResponse errorResponse = ApiErrorResponse.of(
            "AUTH_TOKEN_INVALID",
            "Authentication is required to access this resource"
        );

        response.getOutputStream().write(objectMapper.writeValueAsBytes(errorResponse));
    }
}
