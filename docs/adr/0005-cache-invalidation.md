# ADR 0005 - Cache e invalidacao do dashboard

Status: accepted
Data: 2026-01-16

## Contexto
O dashboard agrega dados com custo alto e precisa de boa performance.

## Decisao
Usar Redis para cache de indicadores de dashboard.
TTL padrao: 300s para overview e 600s para top produtos.
Cache e invalidado sempre que houver movimentacao ou transferencia.

## Consequencias
- Reduz carga de leitura e melhora tempo de resposta.
- Exige estrategia clara de invalidacao em eventos de estoque.
- Dados podem ficar desatualizados por ate o TTL.
