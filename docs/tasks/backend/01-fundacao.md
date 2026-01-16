# Backend - Fase 0 Fundacao

## FND-01 Bootstrap do projeto
DoD:
- Projeto sobe com Spring Boot 3.2 e Java 21.
- Dependencias base: web, security, jpa, validation, flyway, redis, actuator, mapstruct.
- `GET /actuator/health` responde.

## FND-02 Estrutura modular e limites
DoD:
- Pacotes por dominio: auth, tenants, users, branches, catalog, inventory, dashboard.
- Camadas: domain, application, infra (web/persistence/security).
- Regra de dependencia documentada (domain nao depende de infra).

## FND-03 Shared-kernel e contratos
DoD:
- Base entity com auditing e tenant-aware.
- DTOs de entrada/saida e mappers definidos.

## FND-04 Padronizacao de respostas e erros
DoD:
- Envelope `success/data/meta` e `success/error`.
- `@ControllerAdvice` com codigos do PRD.

## FND-05 Configuracao e ambiente local
DoD:
- `docker-compose` com MySQL e Redis.
- Config por ENV e profiles `dev`/`test`.
- Flyway habilitado.

## FND-06 Convencoes de codigo
DoD:
- Convencoes aplicadas conforme `docs/tasks/00-CONVENCOES.md`.
- Nomes e estruturas consistentes entre modulos.
