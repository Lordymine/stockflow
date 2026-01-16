package com.stockflow.shared.domain.exception;

/**
 * Exceção lançada quando um recurso não é encontrado.
 *
 * Marcar como final previne subclasses não autorizadas,
 * garantindo que a hierarquia de exceções permaneça controlada.
 */
public final class NotFoundException extends BaseDomainException {
    public NotFoundException(String resource, Object id) {
        super(String.format("%s with id %s not found", resource, id), "NOT_FOUND");
    }

    public NotFoundException(String message) {
        super(message, "NOT_FOUND");
    }
}
