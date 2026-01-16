package com.stockflow.shared.domain.exception;

/**
 * Exceção lançada quando há erro na requisição do cliente.
 *
 * Marcar como final previne subclasses não autorizadas,
 * garantindo que a hierarquia de exceções permaneça controlada.
 */
public final class BadRequestException extends BaseDomainException {
    public BadRequestException(String message) {
        super(message, "BAD_REQUEST");
    }

    public BadRequestException(String message, Object... params) {
        super(message, "BAD_REQUEST", params);
    }
}
