package com.stockflow.shared.infrastructure.web;

import com.stockflow.shared.application.dto.*;
import com.stockflow.shared.domain.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/success")
    public ResponseEntity<ApiResponse<String>> testSuccess() {
        return ApiResponse.okResponse("Success!");
    }

    @GetMapping("/success-with-meta")
    public ResponseEntity<ApiResponse<Map<String, String>>> testSuccessWithMeta() {
        Map<String, String> data = Map.of("key", "value");
        Map<String, Object> meta = Map.of("version", "1.0");
        return ApiResponse.okResponse(data, meta);
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PaginationResponse<String>>> testPaginated(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        List<String> items = List.of("item1", "item2", "item3");
        Page<String> pageResult = new PageImpl<>(items);

        PaginationResponse<String> response = PaginationResponse.fromPage(pageResult);

        return ApiResponse.okResponse(response);
    }

    @GetMapping("/error")
    public ResponseEntity<ApiResponse<Void>> testError() {
        return ApiResponse.errorResponse(
            "TEST_ERROR",
            "This is a test error",
            org.springframework.http.HttpStatus.BAD_REQUEST
        );
    }

    @GetMapping("/not-found")
    public void testNotFound() {
        throw new NotFoundException("TestResource", 123L);
    }

    @GetMapping("/bad-request")
    public void testBadRequest() {
        throw new BadRequestException("Invalid parameter");
    }

    @GetMapping("/conflict")
    public void testConflict() {
        throw new ConflictException("Resource already exists");
    }
}
