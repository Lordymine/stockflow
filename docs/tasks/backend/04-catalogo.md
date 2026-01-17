# Backend - Fase 3 Catalogo

## CAT-01 CRUD categorias
DoD:
- Endpoints com validacao e paginacao.
- Delete faz soft delete.

## CAT-02 CRUD produtos
DoD:
- SKU unico por tenant.
- Campos extras e status ativo.

## CAT-03 Busca e filtros
DoD:
- GET /api/v1/products?search=&categoryId=&minPrice=&maxPrice=&isActive=&sortBy=&sortOrder=&page=&size=

## CAT-04 Ativar/desativar produto
DoD:
- PATCH /api/v1/products/{id}/active com ActiveRequest.
