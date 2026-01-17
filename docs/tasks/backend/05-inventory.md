# Backend - Fase 4 Inventory

## INV-01 Estoque por filial com optimistic locking
DoD:
- branch_product_stock com @Version e unique por tenant/branch/product.
- Endpoints de consulta por filial/produto.

## INV-02 Movimentacoes
DoD:
- POST /api/v1/branches/{branchId}/movements com IN/OUT/ADJUSTMENT e invariantes.
- Saldo nunca negativo.

## INV-03 Transferencias
DoD:
- POST /api/v1/transfers cria TRANSFER_OUT e TRANSFER_IN na mesma transacao.
- Concorrencia tratada com optimistic locking.

## INV-04 Auditoria minima
DoD:
- created_by, created_at, reason, note persistidos.
- Movimentos imutaveis.

## INV-05 Erros de estoque
DoD:
- STOCK_INSUFFICIENT e STOCK_CONCURRENT_MODIFICATION.
