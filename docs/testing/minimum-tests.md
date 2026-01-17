# Testes minimos obrigatorios

## Objetivo
Garantir comportamento correto para auth, catalogo, inventory e dashboard.

## Unitarios (JUnit 5)
- Inventory: nao permite estoque negativo.
- Inventory: movimento OUT com saldo insuficiente retorna erro.
- Inventory: ADJUSTMENT respeita regras de motivo.
- Inventory: transferencia cria dois movimentos e atualiza dois estoques.
- Inventory: conflito de versao gera `STOCK_CONCURRENT_MODIFICATION`.
- Auth: geracao e validacao de JWT com claims obrigatorias.
- Auth: refresh token hash e revogacao.

## Integracao (Testcontainers)
- Tenants: signup cria tenant e admin inicial.
- Auth: login valido retorna access/refresh e claims.
- Auth: refresh invalido retorna `AUTH_TOKEN_EXPIRED`.
- RBAC: acesso negado para role sem permissao.
- Escopo de filial: usuario sem filial recebe `FORBIDDEN_BRANCH_ACCESS`.
- Catalogo: criar categoria e produto e listar com paginacao.
- Inventory: criar movimento IN e OUT e verificar saldo.
- Transferencia: transferencia bem sucedida e saldo consistente.
- Dashboard: overview retorna dados e cache funciona.

## Concorrencia
- Duas transferencias concorrentes no mesmo produto/filial:
  - uma deve falhar com `STOCK_CONCURRENT_MODIFICATION`.
  - saldo final consistente e sem negativo.

## Dados de seed para testes
- Tenant ativo.
- Branch A e Branch B.
- Usuario ADMIN com acesso a ambas filiais.
- Produtos com saldo inicial definido.

## Criterios de aceitacao
- Todos os testes passam localmente e no CI.
- Cobertura de inventario cobre invariantes criticas.
