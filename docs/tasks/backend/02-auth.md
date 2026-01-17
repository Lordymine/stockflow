# Backend - Fase 1 Autenticacao e autorizacao

## Status: CONCLUIDO

## AUTH-01 Migrations base (tenants/users/roles/user_roles/refresh_tokens)
DoD:
- Tabelas, indices e constraints do PRD.
- Seed de roles.

## AUTH-02 Signup tenant/admin
DoD:
- Endpoint publico para criar tenant + admin inicial.
- Email unico entre tenants (login por email).

## AUTH-03 JWT + refresh token
DoD:
- Access token curto com claims: tenantId, userId, roles, branches.
- Refresh token persistido como hash e revogavel.

## AUTH-04 Spring Security + RBAC
DoD:
- Rotas `/auth/**` publicas; demais protegidas.
- Roles ADMIN/MANAGER/STAFF aplicadas.

## AUTH-05 TenantContext
DoD:
- Tenant extraido do JWT e aplicado a queries.
- Bloqueio de acesso cross-tenant.
