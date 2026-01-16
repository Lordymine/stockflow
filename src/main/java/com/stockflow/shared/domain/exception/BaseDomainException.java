package com.stockflow.shared.domain.exception;

/**
 * Exceção base de domínio usando Java 21 Sealed Classes.
 *
 * Sealed classes permitem:
 * - Restrição de subclasses em tempo de compilação
 * - Pattern matching exhaustivo em switch expressions
 * - Melhor design de APIs com hierarquias conhecidas
 *
 * Todas as subclasses permitidas devem estar no mesmo módulo (package no nosso caso).
 */
public sealed abstract class BaseDomainException extends RuntimeException
    permits NotFoundException, ConflictException, BadRequestException, ValidationException, ForbiddenException {

    private final String code;
    private final Object[] params;

    protected BaseDomainException(String message, String code) {
        super(message);
        this.code = code;
        this.params = null;
    }

    protected BaseDomainException(String message, String code, Object... params) {
        super(message);
        this.code = code;
        this.params = params;
    }

    public String getCode() {
        return code;
    }

    public Object[] getParams() {
        return params;
    }
}
