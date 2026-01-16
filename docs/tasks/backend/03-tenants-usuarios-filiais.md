# Backend - Fase 2 Tenants, usuarios e filiais

## TEN-01 `GET /tenants/me`
DoD:
- Retorna tenant atual; bloqueia tenant inativo.

## USR-01 CRUD usuarios
DoD:
- `POST`, `GET` paginado, `PATCH active`.
- Validacoes de email e status.

## USR-02 Gestao de roles
DoD:
- `PUT /users/{id}/roles` restrito a ADMIN.

## BRN-01 CRUD filiais
DoD:
- `POST`, `GET`, `PATCH active`.
- `code` unico por tenant, campos extras do PRD.

## USR-03 Vinculo usuario-filiais
DoD:
- `PUT /users/{id}/branches`.
- Escopo por filial aplicado em endpoints com `branchId`.
