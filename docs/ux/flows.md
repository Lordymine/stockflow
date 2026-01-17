# Fluxos de UX e estados

## Signup inicial (setup)
1) Usuario cria tenant com admin inicial.
2) Em sucesso, redirecionar para login.
3) Admin cria filial inicial apos login.

## Login
1) Usuario informa email e senha.
2) Em sucesso, salvar access/refresh e carregar tenant/branches.
3) Em erro 401, exibir mensagem de credenciais invalidas.

## Selecao de filial
1) Mostrar lista de filiais permitidas.
2) Usuario escolhe filial ativa (currentBranchId).
3) Todas as telas de inventory usam a filial ativa.
4) Se usuario nao tiver filiais, exibir estado vazio.

## Gestao de usuarios (ADMIN)
1) Criar usuario com nome, email e senha.
2) Definir roles.
3) Vincular filiais.
4) Ativar/desativar conforme necessidade.

## Catalogo
- Categorias: listar, criar e editar.
- Produtos: listar com busca/paginacao e criar/editar com campos extras.
- Status: permitir ativar/desativar produto.

## Movimentacoes de estoque
1) Selecionar produto na filial atual.
2) Informar tipo e motivo.
3) Informar quantidade > 0.
4) Em erro de saldo, mostrar `STOCK_INSUFFICIENT`.

## Transferencias
1) Selecionar filial origem e destino diferentes.
2) Selecionar produto e quantidade > 0.
3) Em erro de concorrencia, mostrar `STOCK_CONCURRENT_MODIFICATION` e sugerir reprocessar.

## Dashboard
- Exibir indicadores basicos e ultimas movimentacoes.
- Suportar filtro por filial.

## Estados padrao
- Loading: skeleton ou spinner.
- Empty: mensagem clara e acao sugerida.
- Error: exibir codigo e mensagem do backend.
