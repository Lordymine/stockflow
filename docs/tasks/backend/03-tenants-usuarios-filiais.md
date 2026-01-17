# Backend - Fase 2 Tenants, usuarios e filiais

## TEN-01 `GET /api/v1/tenants/me`
DoD:
- Retorna tenant atual; bloqueia tenant inativo.

## USR-01 CRUD usuarios
DoD:
- `POST /api/v1/users`, `GET` paginado, `PATCH /api/v1/users/{id}/active`.
- Validacoes de email e status.

## USR-02 Gestao de roles
DoD:
- `PUT /api/v1/users/{id}/roles` restrito a ADMIN.

## BRN-01 CRUD filiais
DoD:
- `POST /api/v1/branches`, `GET /api/v1/branches`, `PATCH /api/v1/branches/{id}/active`.
- `code` unico por tenant, campos extras do PRD.

## USR-03 Vinculo usuario-filiais
DoD:
- `PUT /api/v1/users/{id}/branches`.
- Escopo por filial aplicado em endpoints com `branchId`.
