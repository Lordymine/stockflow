package com.stockflow.shared.domain.exception;

/**
 * Exceção lançada quando há conflito de dados (ex: slug duplicado).
 *
 * Marcar como final previne subclasses não autorizadas,
 * garantindo que a hierarquia de exceções permaneça controlada.
 */
public final class ConflictException extends BaseDomainException {
    public ConflictException(String message) {
        super(message, "CONFLICT");
    }

    public ConflictException(String message, Object... params) {
        super(message, "CONFLICT", params);
    }
}
