package com.stockflow.shared.domain.exception;

/**
 * Exceção lançada quando há erro de validação de dados.
 *
 * Marcar como final previne subclasses não autorizadas,
 * garantindo que a hierarquia de exceções permaneça controlada.
 */
public final class ValidationException extends BaseDomainException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }

    public ValidationException(String message, Object... params) {
        super(message, "VALIDATION_ERROR", params);
    }
}
