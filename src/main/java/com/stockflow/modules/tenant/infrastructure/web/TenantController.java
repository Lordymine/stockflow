package com.stockflow.modules.tenant.infrastructure.web;

import com.stockflow.modules.tenant.application.dto.TenantRequest;
import com.stockflow.modules.tenant.application.dto.TenantResponse;
import com.stockflow.modules.tenant.domain.service.TenantService;
import com.stockflow.shared.application.dto.ApiResponse;
import com.stockflow.shared.application.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService service;

    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponse>> create(
        @Valid @RequestBody TenantRequest request
    ) {
        TenantResponse response = service.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> findById(
        @PathVariable Long id
    ) {
        TenantResponse response = service.findById(id);
        return ApiResponse.okResponse(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<TenantResponse>> findBySlug(
        @PathVariable String slug
    ) {
        TenantResponse response = service.findBySlug(slug);
        return ApiResponse.okResponse(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantResponse>>> findAll() {
        List<TenantResponse> response = service.findAll();
        return ApiResponse.okResponse(response);
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PaginationResponse<TenantResponse>>> findAllPaginated(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<TenantResponse> responsePage = service.findAll(pageable);

        PaginationResponse<TenantResponse> response = PaginationResponse.fromPage(responsePage);
        return ApiResponse.okResponse(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TenantResponse>>> findActive() {
        List<TenantResponse> response = service.findActive();
        return ApiResponse.okResponse(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> update(
        @PathVariable Long id,
        @Valid @RequestBody TenantRequest request
    ) {
        TenantResponse response = service.update(id, request);
        return ApiResponse.okResponse(response);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<TenantResponse>> toggleActive(
        @PathVariable Long id,
        @RequestParam boolean isActive
    ) {
        TenantResponse response = service.toggleActive(id, isActive);
        return ApiResponse.okResponse(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @PathVariable Long id
    ) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
