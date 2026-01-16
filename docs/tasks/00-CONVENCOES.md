# Convencoes e identidade de codigo

## Objetivo
Manter o codigo consistente e facil de manter, mesmo com varios autores.

## Regras gerais
- Evitar duplicacao: shared-kernel e componentes reutilizaveis.
- Responsabilidade unica por classe/arquivo.
- Dependencias sempre apontam para dentro: domain -> application -> infra.
- Domain nao depende de frameworks nem de infra.
- DTOs e mappers apenas na borda (web/inbound/outbound).
- Erros seguem o catalogo do PRD.
- Logs sem dados sensiveis.

## Backend
### Estrutura por dominio
- <dominio>/domain
- <dominio>/application
- <dominio>/infra/web
- <dominio>/infra/persistence
- <dominio>/infra/security (quando aplicavel)

### Nomes e tipos
- Entidades: `*Entity` ou nome do agregado.
- Use cases: `*UseCase` (Create/Update/Transfer etc).
- Services tecnicos: `*Service`.
- Repositories: `*Repository`.
- Controllers: `*Controller`.
- DTOs: `*Request` e `*Response`.
- Mappers: `*Mapper`.
- Exceptions: `*Exception`.

### Regras de implementacao
- Validacao de entrada: Bean Validation nos DTOs.
- Invariantes: no domain.
- Transacoes: na camada application.
- MapStruct para conversoes.
- Queries sempre com tenantId e branchId quando aplicavel.
- Paginacao padrao `page/size` e ordenacao quando suportado.

## Frontend
### Estrutura
- `core` (auth, interceptors, guards, env).
- `shared` (components, pipes, directives).
- `features/<modulo>` (pages, components, services, models).

### Nomes
- Componentes de pagina: `*PageComponent`.
- Formularios: `*FormComponent`.
- Listas: `*ListComponent`.
- Servicos: `*.service.ts`.
- Guards: `*.guard.ts`.
- Interceptors: `*.interceptor.ts`.
- Modelos: `*.model.ts`.

### Regras de implementacao
- Consumir envelope de API padrao.
- Erros mapeados por codigo.
- UI respeita roles e filiais.
- Sem logica de negocio em componentes; usar services/use-cases.
- Estados de loading/empty/error padronizados.
