# ADR 0003 - JWT + refresh token persistido

Status: accepted
Data: 2026-01-16

## Contexto
O backend deve ser stateless e suportar revogacao de sessao.

## Decisao
Usar access token JWT com TTL curto e refresh token com TTL longo.
Refresh token sera persistido como hash, com data de expira??o e revogacao.
No refresh, o token antigo e revogado e um novo e emitido (rotacao).
Claims minimas: tenantId, userId, roles e branchIds.

## Consequencias
- Escalabilidade horizontal com tokens stateless.
- Possibilidade de logout e revogacao imediata.
- Requer storage confiavel para refresh tokens.
