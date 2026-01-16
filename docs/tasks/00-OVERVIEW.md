# Plano de tarefas - StockFlow PRO

## Objetivo
Quebrar o PRD em tarefas ordenadas e hierarquicas, separando backend e frontend,
respeitando Clean Architecture, separacao de dominios e iniciando por autenticacao.

## Regras de ordem e dependencias
- Backend: autenticacao e autorizacao antes de qualquer modulo funcional (catalogo, inventory, dashboard).
- Backend: tenants, usuarios e filiais antes de catalogo e inventory.
- Backend: catalogo antes de inventory; inventory antes de dashboard.
- Frontend: telas de autenticacao antes de qualquer tela protegida.
- Frontend: cadastros base (tenants/usuarios/filiais) antes de catalogo e inventory.
- Frontend: integrar telas apenas apos os endpoints do backend correspondentes existirem.
- Evitar duplicacao de codigo: shared-kernel, mappers, DTOs e validacoes reutilizaveis.

## Definicao de pronto (DoD) minimo
### Backend
- Endpoint protegido por RBAC e escopo de filial quando aplicavel.
- Validacoes e erros padronizados.
- Migrations versionadas aplicaveis do zero.
- Paginacao quando aplicavel.
- Testes minimos do modulo (unitarios e integracao quando indicado).

### Frontend
- Rotas protegidas com guards e checagem de roles/filiais.
- Integracao com API via interceptors (JWT/refresh) e tratamento de erros.
- Formularios com validacao e mensagens claras.
- Paginacao, busca e filtros quando aplicavel.
- Estados de loading/empty/error consistentes.

## Identidade do codigo
- Todas as tasks devem seguir `docs/tasks/00-CONVENCOES.md`.
- Padroes de nomes, camadas e pastas sao obrigatorios.
- Reuso via shared-kernel e componentes comuns.

## Documentos obrigatorios
- docs/README.md
- docs/00-PRD-ATUALIZADO.md
- docs/api/openapi.yaml
- docs/api/error-codes.md
- docs/security/permissions-matrix.md
- docs/testing/minimum-tests.md
- docs/ux/flows.md
- docs/config/env.md
- docs/adr/0001-arquitetura-modular-monolito.md
- docs/adr/0002-multi-tenancy.md
- docs/adr/0003-auth-jwt-refresh.md
- docs/adr/0004-inventory-concurrency.md
- docs/adr/0005-cache-invalidation.md

## Arquivos de tarefas
- docs/tasks/00-CONVENCOES.md
- docs/tasks/backend/01-fundacao.md
- docs/tasks/backend/02-auth.md
- docs/tasks/backend/03-tenants-usuarios-filiais.md
- docs/tasks/backend/04-catalogo.md
- docs/tasks/backend/05-inventory.md
- docs/tasks/backend/06-dashboard-cache.md
- docs/tasks/backend/07-qualidade-ci.md
- docs/tasks/frontend/01-fundacao.md
- docs/tasks/frontend/02-auth.md
- docs/tasks/frontend/03-tenants-usuarios-filiais.md
- docs/tasks/frontend/04-catalogo.md
- docs/tasks/frontend/05-inventory.md
- docs/tasks/frontend/06-dashboard.md
- docs/tasks/frontend/07-qualidade.md
