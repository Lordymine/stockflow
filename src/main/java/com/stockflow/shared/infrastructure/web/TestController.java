package com.stockflow.shared.infrastructure.web;

import com.stockflow.shared.application.dto.ApiResponse;
import com.stockflow.shared.application.dto.ApiErrorResponse;
import com.stockflow.shared.domain.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Test controller for verifying API responses and error handling.
 *
 * <p>This controller provides test endpoints to verify that:</p>
 * <ul>
 *   <li>Success responses work correctly</li>
 *   <li>Error handling works correctly</li>
 *   <li>The application is running</li>
 * </ul>
 *
 * <p><strong>NOTE:</strong> This controller should be disabled or removed in production.</p>
 */
@Tag(name = "Test", description = "Test endpoints for API verification")
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    /**
     * Health check endpoint.
     *
     * @return success response with current timestamp
     */
    @Operation(summary = "Health check", description = "Verifies that the API is running")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "application", "StockFlow PRO"
        );

        return ResponseEntity.ok(ApiResponse.of(data));
    }

    /**
     * Test success response with data.
     *
     * @return success response with sample data
     */
    @Operation(summary = "Test success response", description = "Returns a sample success response")
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<Map<String, String>>> testSuccess() {
        Map<String, String> data = Map.of(
            "message", "API is working correctly!",
            "status", "success"
        );

        return ResponseEntity.ok(ApiResponse.of(data));
    }

    /**
     * Test error response.
     *
     * @return error response
     */
    @Operation(summary = "Test error response", description = "Returns a sample error response")
    @GetMapping("/error")
    public ResponseEntity<ApiErrorResponse> testError() {
        ApiErrorResponse response = ApiErrorResponse.of(
            "TEST_ERROR",
            "This is a test error message"
        );

        return ResponseEntity.status(500).body(response);
    }

    /**
     * Test not found exception.
     *
     * @return 404 error response
     */
    @Operation(summary = "Test not found", description = "Returns a 404 error response")
    @GetMapping("/not-found")
    public ResponseEntity<Void> testNotFound() {
        throw NotFoundException.of("TestResource", 999L);
    }
}
