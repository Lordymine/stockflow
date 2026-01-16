# ADR 0004 - Concorrencia no estoque (optimistic locking)

Status: accepted
Data: 2026-01-16

## Contexto
Operacoes de estoque sao criticas e precisam evitar saldo negativo em concorrencia.

## Decisao
Aplicar optimistic locking em `branch_product_stock` com campo `version`.
Cada movimentacao/transferencia carrega a versao atual e atualiza em transacao.
Conflitos geram `OptimisticLockException` e retornam erro `STOCK_CONCURRENT_MODIFICATION`.
Transferencias usam uma unica transacao com isolamento READ_COMMITTED.

## Consequencias
- Evita race conditions sem lock pessimista.
- Requer tratamento explicito de conflitos no backend e UI.
- Necessita testes de concorrencia para garantir consistencia.
