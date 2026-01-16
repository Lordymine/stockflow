# ADR 0001 - Arquitetura modular monolito

Status: accepted
Data: 2026-01-16

## Contexto
O produto exige evolucao rapida, regras de dominio bem definidas e facil manutencao.
Precisamos de separacao clara de dominios sem custo operacional de microservicos.

## Decisao
Adotar um modular monolith com DDD pragmatico e camadas inspiradas em Clean/Hexagonal.
Cada dominio tem `domain`, `application` e `infra`, sem dependencia de `domain` para `infra`.
Integracao entre dominios ocorre via interfaces na camada application.

## Consequencias
- Facil inicio do projeto com boundaries claros.
- Baixa duplicacao de codigo com shared-kernel.
- Possivel extracao futura de dominios para servicos.
