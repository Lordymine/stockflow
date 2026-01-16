package com.stockflow.shared.infrastructure.web;

import com.stockflow.shared.application.dto.ApiResponse;
import com.stockflow.shared.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception handler global usando Java 21 Pattern Matching.
 *
 * Pattern Matching para switch com Sealed Classes permite:
 * - Código mais conciso e legível
 * - Pattern matching exhaustivo (compilador força cobrir todos os casos)
 * - Sem default clause necessária
 * - Guards simplificados (variáveis sem nome com __)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handler para exceções de domínio usando Pattern Matching.
     *
     * Com Sealed Classes, o compilador garante que todos os tipos
     * de exceção são tratados - não há risco de esquecer um caso.
     */
    @ExceptionHandler(BaseDomainException.class)
    public ResponseEntity<ApiResponse<Object>> handleDomainException(BaseDomainException ex) {
        log.error("Domain exception: {}", ex.getMessage());

        // Pattern Matching para switch - Java 21
        // Cada case faz type pattern matching e guarda o resultado
        // Variável __ indica que não precisamos do valor, apenas do tipo
        return switch (ex) {
            case NotFoundException nfe -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(nfe.getCode(), nfe.getMessage()));

            case ConflictException ce -> ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ce.getCode(), ce.getMessage()));

            case BadRequestException bre -> ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(bre.getCode(), bre.getMessage()));

            case ValidationException ve -> ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ve.getCode(), ve.getMessage()));

            case ForbiddenException fe -> ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(fe.getCode(), fe.getMessage()));

            // Sem default necessário - compilador força exaustividade
            // Se adicionarmos uma nova exceção, o código não compila até tratá-la aqui
        };
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation errors: {}", errors);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobal(
        Exception ex,
        WebRequest request
    ) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
