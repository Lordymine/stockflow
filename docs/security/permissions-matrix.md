# Matriz de permissoes e escopo

## Roles
- ADMIN: acesso administrativo dentro do tenant.
- MANAGER: gestao operacional e cadastros principais.
- STAFF: operacao basica de estoque e consulta.

## Regras globais
- `tenantId` vem do JWT e sempre filtra dados.
- Endpoints exigem Authorization: Bearer, exceto login/refresh e bootstrap de tenant.
- Escopo de filial e aplicado quando houver `branchId` na rota ou no body.
- Todos os usuarios (incluindo ADMIN) precisam de filial atribuida para operar em endpoints por filial.
- Se nao houver acesso a filial, retornar `FORBIDDEN_BRANCH_ACCESS`.

## Escopo de filial
- `/branches/{branchId}/...`: validar `branchId` em `user_branches`.
- `/transfers`: validar `sourceBranchId` e `destinationBranchId`.
- `/branches` GET: filtrar para filiais permitidas do usuario.

## Matriz por endpoint

### Auth
| Endpoint | Roles | Observacoes |
|---|---|---|
| POST /api/v1/auth/login | Publico | Credenciais validas |
| POST /api/v1/auth/refresh | Publico | Refresh token valido |
| POST /api/v1/auth/logout | ADMIN, MANAGER, STAFF | Revoga refresh token |

### Tenants
| Endpoint | Roles | Observacoes |
|---|---|---|
| POST /api/v1/tenants | Publico | Apenas quando nao existir tenant (bootstrap com admin e filial) |
| GET /api/v1/tenants/me | ADMIN, MANAGER, STAFF | Tenant atual |

### Users
| Endpoint | Roles | Observacoes |
|---|---|---|
| POST /api/v1/users | ADMIN | Criar usuario |
| GET /api/v1/users | ADMIN | Listagem paginada |
| PATCH /api/v1/users/{id}/active | ADMIN | Ativar/desativar |
| PUT /api/v1/users/{id}/roles | ADMIN | Definir roles |
| PUT /api/v1/users/{id}/branches | ADMIN | Definir filiais |

### Branches
| Endpoint | Roles | Observacoes |
|---|---|---|
| POST /api/v1/branches | ADMIN | Criar filial |
| GET /api/v1/branches | ADMIN, MANAGER, STAFF | Filtrado por filiais permitidas |
| PATCH /api/v1/branches/{id}/active | ADMIN | Ativar/desativar |

### Catalogo
| Endpoint | Roles | Observacoes |
|---|---|---|
| POST /api/v1/categories | ADMIN, MANAGER | Criar categoria |
| GET /api/v1/categories | ADMIN, MANAGER, STAFF | Listar categorias |
| PUT /api/v1/categories/{id} | ADMIN, MANAGER | Atualizar categoria |
| POST /api/v1/products | ADMIN, MANAGER | Criar produto |
| GET /api/v1/products | ADMIN, MANAGER, STAFF | Listar produtos |
| GET /api/v1/products/{id} | ADMIN, MANAGER, STAFF | Detalhe produto |
| PUT /api/v1/products/{id} | ADMIN, MANAGER | Atualizar produto |
| PATCH /api/v1/products/{id}/active | ADMIN, MANAGER | Ativar/desativar |

### Inventory
| Endpoint | Roles | Observacoes |
|---|---|---|
| GET /api/v1/branches/{branchId}/stock | ADMIN, MANAGER, STAFF | Requer acesso a filial |
| GET /api/v1/branches/{branchId}/stock/{productId} | ADMIN, MANAGER, STAFF | Requer acesso a filial |
| POST /api/v1/branches/{branchId}/movements | ADMIN, MANAGER, STAFF | STAFF apenas IN/OUT |
| GET /api/v1/branches/{branchId}/movements | ADMIN, MANAGER, STAFF | Requer acesso a filial |
| POST /api/v1/transfers | ADMIN, MANAGER | Origem e destino permitidos |

### Dashboard
| Endpoint | Roles | Observacoes |
|---|---|---|
| GET /api/v1/dashboard/overview | ADMIN, MANAGER, STAFF | Filial opcional via query |

## Regras especificas de movimentos
- STAFF pode criar apenas movimentos `IN` e `OUT`.
- `ADJUSTMENT` e `TRANSFER` exigem ADMIN ou MANAGER.
