package com.stockflow.shared.domain.exception;

/**
 * Exceção lançada quando um usuário tenta acessar um recurso sem permissão.
 */
public final class ForbiddenException extends BaseDomainException {

    public ForbiddenException(String message) {
        super(message, "FORBIDDEN");
    }

    public ForbiddenException(String message, Object... params) {
        super(message, "FORBIDDEN", params);
    }
}
