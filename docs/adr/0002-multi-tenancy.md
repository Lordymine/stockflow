# ADR 0002 - Multi-tenancy por coluna

Status: accepted
Data: 2026-01-16

## Contexto
O sistema e multi-tenant e deve isolar dados por empresa.
Precisamos de isolamento simples, performance aceitavel e baixo custo operacional.

## Decisao
Usar um banco compartilhado com `tenant_id` em todas as tabelas de dominio.
O `tenant_id` e extraido do JWT e propagado via TenantContext.
Consultas e comandos sempre filtram por `tenant_id`.
Constraints de unicidade devem incluir `tenant_id` quando aplicavel.

## Consequencias
- Isolamento garantido pela camada de aplicacao e constraints.
- Simplicidade de operacao e deploy.
- Requer cuidado para nunca executar queries sem tenant.
