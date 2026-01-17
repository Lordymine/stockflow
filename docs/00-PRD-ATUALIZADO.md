# StockFlow PRO — PRD ATUALIZADO (v2.0)

> **Resumo**
>
> **StockFlow PRO** é uma aplicação web de **controle de estoque multiempresa (multi-tenant)** com **multi-filiais**, **controle de acesso (RBAC + escopo por filial)**, movimentações auditáveis e **transferência entre filiais**, construída com **Backend Java (Spring Boot)**, **MySQL** e **Frontend Angular**.
>
> O objetivo principal é servir como **case profissional completo**, com arquitetura escalável, manutenível e com código limpo, pronta para deploy.
>
> **v2.0 - Atualizações do Backend Architect:**
> - ✅ Adicionado optimistic locking para prevenir race conditions
> - ✅ Ajustado PK composta para surrogate key + unique constraint
> - ✅ Adicionados índices críticos para performance
> - ✅ Campos adicionais em products e branches
> - ✅ Strategy para cache com Redis

---

## 1) Objetivos do Produto

### 1.1 Objetivo principal
Entregar um sistema de estoque pronto para uso, com:
- Controle de produtos e categorias
- Estoque por filial
- Movimentações (entrada/saída/ajuste)
- Transferência entre filiais com consistência transacional
- Dashboard operacional por empresa/filial
- Autenticação JWT + refresh token
- RBAC (roles)
- Escopo multiempresa e multi-filial

### 1.2 Objetivos secundários
- Servir como **case de arquitetura** (módulos por domínio)
- Permitir evolução futura para:
  - relatórios avançados
  - integrações via webhooks
  - exportação de dados
  - auditoria avançada

### 1.3 Não-objetivos (fora do escopo inicial)
- Emissão fiscal / NF-e
- Integração com ERP externo
- Controle financeiro completo (DRE, contas a pagar/receber)
- Inventário com leitura por código de barras (pode entrar depois)

---

## 2) Público-alvo

- Pequenas e médias empresas que precisam controlar estoque
- Times internos que operam com múltiplas filiais
- Usuários administrativos e operacionais (estoquista)

---

## 3) Visão Geral de Arquitetura (alto nível)

### 3.1 Estilo arquitetural recomendado
**Modular Monolith + DDD pragmático + camadas inspiradas em Clean/Hexagonal**

- **Modules** separados por domínios (auth, users, branches, catalog, inventory, dashboard)
- **Domain** com regras e invariantes (principalmente no módulo de inventory)
- **Application** com casos de uso (use cases)
- **Infra** com adapters (web/controllers, persistence/JPA, security)

### 3.2 Princípios
- **Separação clara de responsabilidades**
- **Código orientado a domínio**
- **Imutabilidade de histórico (movements não são "editados")**
- **Transações ACID para operações críticas**
- **Optimistic Locking para prevenir race conditions**
- **Padronização de erros e respostas**
- **Configuração via variáveis de ambiente**
- **Paginação, filtros e validações desde o MVP**

---

## 4) Requisitos Funcionais (RF)

### RF-001 — Multiempresa (Tenant)
- O sistema deve suportar múltiplas empresas (tenants).
- Todo dado do domínio deve ser isolado por `tenant_id`.
- Um usuário pertence a uma empresa (tenant) e não pode acessar dados de outra.

### RF-002 — Multi-filiais (Branches)
- A empresa deve poder criar e gerenciar múltiplas filiais.
- Operações de estoque devem ser sempre executadas no contexto de uma filial.

### RF-003 — Autenticação (JWT + Refresh Token)
- Login com email e senha.
- Access token (curto) e refresh token (longo).
- Refresh token deve ser revogável (logout).

### RF-004 — Autorização (RBAC)
- Roles suportadas:
  - `ADMIN`
  - `MANAGER`
  - `STAFF`
- O sistema deve restringir rotas conforme role.
- O sistema deve restringir escopo por filial (branch access).

### RF-005 — Gestão de Usuários
- Admin cria usuários na empresa.
- Admin gerencia roles do usuário.
- Admin define quais filiais o usuário pode acessar.
- Usuários podem ser ativados/desativados.

### RF-006 — Catálogo (Produtos e Categorias)
- CRUD de categorias
- CRUD de produtos
- Produto deve ter SKU único por empresa
- Produto pode ser ativado/desativado

### RF-007 — Estoque por Filial
- O sistema deve armazenar e exibir o saldo de estoque por filial e produto.
- Deve suportar estoque mínimo por produto (alerta de baixo estoque por filial).
- **NOVO:** Estoque deve usar optimistic locking para prevenir race conditions.

### RF-008 — Movimentações de Estoque
- Tipos:
  - `IN` (entrada)
  - `OUT` (saída)
  - `ADJUSTMENT` (ajuste)
  - `TRANSFER` (transferência - usado internamente pelo fluxo de transferência)
- Motivos (recomendado como enum):
  - `PURCHASE`, `SALE`, `LOSS`, `RETURN`, `ADJUSTMENT_IN`, `ADJUSTMENT_OUT`, `TRANSFER_IN`, `TRANSFER_OUT`
- Saída não pode deixar saldo negativo.

### RF-009 — Transferência entre Filiais
- Transferir quantidade de um produto de uma filial origem para filial destino.
- Deve criar dois movimentos:
  - origem: `TRANSFER_OUT`
  - destino: `TRANSFER_IN`
- Deve ser transação atômica (falha em qualquer etapa cancela tudo).
- **NOVO:** Deve usar optimistic locking para prevenir concorrência.

### RF-010 — Dashboard
- Indicadores por empresa/filial:
  - total de produtos ativos
  - itens com estoque abaixo do mínimo
  - últimas movimentações
  - top produtos mais movimentados (simples)
- **NOVO:** Dashboard deve usar cache para performance.

### RF-011 — Auditoria mínima
- Movimentações devem registrar:
  - usuário responsável
  - data/hora
  - motivo
  - observação
- Históricos não devem ser editados (apenas estornados via nova movimentação se necessário).

### RF-012 — Observabilidade básica
- Endpoints de saúde e métricas via Actuator
- Logs estruturados e consistentes

---

## 5) Requisitos Não Funcionais (RNF)

### RNF-001 — Performance e escalabilidade
- Paginação obrigatória em listagens
- **Índices compostos em campos críticos (tenant, branch, date)**
- **Cache Redis para dashboard e queries frequentes**
- Operações críticas transacionais
- Preparado para escalar horizontalmente (stateless)

### RNF-002 — Segurança
- Senhas com hash forte (BCrypt ou Argon2)
- JWT assinado com secret seguro (ENV)
- Refresh token persistido como hash
- Proteção de CORS configurável
- Não logar tokens nem credenciais

### RNF-003 — Manutenibilidade
- Arquitetura por módulos/domínios
- DTOs para entrada/saída
- Validação com Bean Validation
- Camada de exceções padronizada
- Mappers (MapStruct recomendado)

### RNF-004 — Deploy
- Config por ENV
- Docker Compose para dev (MySQL + Redis)
- Compatível com deploy em provedores gerenciados (Railway/Render/Fly.io)

### RNF-005 — Testabilidade
- Testes de unidade em services/UseCases críticos (inventory/transfer)
- Testes de integração para endpoints principais (mínimo para o case)
- **NOVO:** Testes de concorrência para transferências

---

## 6) Domínios / Módulos

### 6.1 Auth
**Responsável por:** login, refresh, logout, tokens, autenticação

### 6.2 Users
**Responsável por:** usuários, roles, vínculo com filiais, status ativo

### 6.3 Tenants (Empresa)
**Responsável por:** cadastro da empresa e isolamento de dados (multi-tenant)

### 6.4 Branches (Filiais)
**Responsável por:** filiais e governança de acessos

### 6.5 Catalog
**Responsável por:** produtos e categorias

### 6.6 Inventory
**Responsável por:** estoque, movimentações, transferências, regras e invariantes

### 6.7 Dashboard
**Responsável por:** visões agregadas e indicadores

---

## 7) Modelo de Dados (MySQL) - ATUALIZADO

> **Nota:** todos os registros de domínio devem conter `tenant_id`, exceto onde o `tenant_id` é implícito por PK composta.

### 7.1 Tenants
`tenants`
- `id` (PK, BIGINT AUTO_INCREMENT)
- `name` (VARCHAR 255, NOT NULL)
- `slug` (VARCHAR 100, UNIQUE, NOT NULL)
- `is_active` (BOOLEAN, DEFAULT TRUE)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)

**Índices:**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_slug` (`slug`)
- INDEX `idx_active` (`is_active`)

### 7.2 Users / RBAC
`users`
- `id` (PK, BIGINT AUTO_INCREMENT)
- `tenant_id` (FK)
- `name` (VARCHAR 255, NOT NULL)
- `email` (VARCHAR 255, NOT NULL)
- `password_hash` (VARCHAR 255, NOT NULL)
- `is_active` (BOOLEAN, DEFAULT TRUE)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)

**Índices:**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_tenant_email` (`tenant_id`, `email`)
- INDEX `idx_tenant_active` (`tenant_id`, `is_active`)
- INDEX `fk_users_tenant` (`tenant_id`)

`roles`
- `id` (PK, BIGINT AUTO_INCREMENT)
- `name` (ENUM('ADMIN', 'MANAGER', 'STAFF'), NOT NULL)

**Índices:**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_name` (`name`)

`user_roles`
- `user_id` (FK)
- `role_id` (FK)
- PK composta `(user_id, role_id)`

**Índices:**
- PRIMARY KEY (`user_id`, `role_id`)
- INDEX `fk_user_roles_user` (`user_id`)
- INDEX `fk_user_roles_role` (`role_id`)

`branches`
- `id` (PK, BIGINT AUTO_INCREMENT)
- `tenant_id` (FK)
- `name` (VARCHAR 255, NOT NULL)
- `code` (VARCHAR 50, NOT NULL)
- `address` (TEXT, NULL) **NOVO**
- `phone` (VARCHAR 20, NULL) **NOVO**
- `manager_name` (VARCHAR 100, NULL) **NOVO**
- `is_active` (BOOLEAN, DEFAULT TRUE)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)

**Índices:**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`)
- INDEX `idx_tenant_active` (`tenant_id`, `is_active`)
- INDEX `fk_branches_tenant` (`tenant_id`)

`user_branches`
- `user_id` (FK)
- `branch_id` (FK)
- PK composta `(user_id, branch_id)`

**Índices:**
- PRIMARY KEY (`user_id`, `branch_id`)
- INDEX `fk_user_branches_user` (`user_id`)
- INDEX `fk_user_branches_branch` (`branch_id`)

### 7.3 Catálogo
`categories`
- `id` (PK, BIGINT AUTO_INCREMENT)
- `tenant_id` (FK)
- `name` (VARCHAR 255, NOT NULL)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)

**Índices:**
- PRIMARY KEY (`id`)
- INDEX `idx_tenant_name` (`tenant_id`, `name`)
- INDEX `fk_categories_tenant` (`tenant_id`)

`products`
- `id` (PK, BIGINT AUTO_INCREMENT)
- `tenant_id` (FK)
- `category_id` (FK, NULL)
- `name` (VARCHAR 255, NOT NULL)
- `sku` (VARCHAR 100, NOT NULL)
- `description` (TEXT, NULL) **NOVO**
- `barcode` (VARCHAR 50, NULL) **NOVO**
- `unit_of_measure` (ENUM('UN', 'KG', 'L', 'M'), DEFAULT 'UN') **NOVO**
- `image_url` (VARCHAR 500, NULL) **NOVO**
- `cost_price` (DECIMAL 10,2)
- `sale_price` (DECIMAL 10,2)
- `min_stock` (INT, DEFAULT 0)
- `is_active` (BOOLEAN, DEFAULT TRUE)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)

**Índices:**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_tenant_sku` (`tenant_id`, `sku`)
- INDEX `idx_products_tenant_active_name` (`tenant_id`, `is_active`, `name`) **NOVO**
- INDEX `idx_tenant_category` (`tenant_id`, `category_id`)
- INDEX `fk_products_tenant` (`tenant_id`)
- INDEX `fk_products_category` (`category_id`)

### 7.4 Estoque
`branch_product_stock`
- `id` (PK, BIGINT AUTO_INCREMENT) **ALTERADO: era PK composta**
- `tenant_id` (FK, NOT NULL)
- `branch_id` (FK, NOT NULL)
- `product_id` (FK, NOT NULL)
- `quantity` (INT, DEFAULT 0, NOT NULL)
- `version` (INT, DEFAULT 0, NOT NULL) **NOVO: optimistic locking**
- `updated_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

**Índices:**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_tenant_branch_product` (`tenant_id`, `branch_id`, `product_id`) **NOVO**
- INDEX `idx_branch_product` (`branch_id`, `product_id`)
- INDEX `fk_stock_tenant` (`tenant_id`)
- INDEX `fk_stock_branch` (`branch_id`)
- INDEX `fk_stock_product` (`product_id`)

### 7.5 Movimentações
`stock_movements`
- `id` (PK, BIGINT AUTO_INCREMENT)
- `tenant_id` (FK, NOT NULL)
- `branch_id` (FK, NOT NULL)
- `product_id` (FK, NOT NULL)
- `type` (ENUM('IN', 'OUT', 'ADJUSTMENT', 'TRANSFER'), NOT NULL)
- `reason` (ENUM('PURCHASE', 'SALE', 'LOSS', 'RETURN', 'ADJUSTMENT_IN', 'ADJUSTMENT_OUT', 'TRANSFER_IN', 'TRANSFER_OUT'), NOT NULL)
- `quantity` (INT, NOT NULL)
- `note` (TEXT, NULL)
- `created_by_user_id` (FK)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)

**Índices:**
- PRIMARY KEY (`id`)
- INDEX `idx_movements_tenant_branch_date` (`tenant_id`, `branch_id`, `created_at` DESC) **NOVO**
- INDEX `idx_tenant_product` (`tenant_id`, `product_id`)
- INDEX `idx_branch_date` (`branch_id`, `created_at` DESC)
- INDEX `fk_movements_tenant` (`tenant_id`)
- INDEX `fk_movements_branch` (`branch_id`)
- INDEX `fk_movements_product` (`product_id`)
- INDEX `fk_movements_user` (`created_by_user_id`)

### 7.6 Tokens
`refresh_tokens`
- `id` (PK, BIGINT AUTO_INCREMENT)
- `tenant_id` (FK)
- `user_id` (FK)
- `token_hash` (VARCHAR 255, NOT NULL)
- `expires_at` (TIMESTAMP, NOT NULL)
- `revoked_at` (TIMESTAMP, NULL)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)

**Índices:**
- PRIMARY KEY (`id`)
- INDEX `idx_user_token` (`user_id`, `token_hash`)
- INDEX `idx_expires` (`expires_at`)
- INDEX `fk_refresh_tokens_tenant` (`tenant_id`)
- INDEX `fk_refresh_tokens_user` (`user_id`)

---

## 8) Regras de Negócio (Domínio Inventory)

### 8.1 Invariantes
- Estoque por filial nunca pode ser negativo
- **Estoque usa optimistic locking para prevenir race conditions**
- Movimentações são registradas sempre com `created_by`
- Transferência cria dois movimentos correlacionados

### 8.2 Validações de movimentação
- `IN`: soma ao saldo
- `OUT`: subtrai do saldo (exibe saldo >= qty + **optimistic lock**)
- `ADJUSTMENT_IN`: soma (exibe motivo)
- `ADJUSTMENT_OUT`: subtrai (exibe saldo >= qty + **optimistic lock**)
- `TRANSFER_OUT`: subtrai (exibe saldo >= qty + **optimistic lock**)
- `TRANSFER_IN`: soma

### 8.3 Transações
- Movimentações e transferências são executadas dentro de uma transação do banco.
- **Transferências usam @Transactional com isolation READ_COMMITTED**

### 8.4 Cache Strategy **NOVO**
- Dashboard usa Redis cache (TTL 5 minutos)
- Produtos mais movimentados usa cache (TTL 10 minutos)
- Cache invalidado em toda movimentação de estoque

---

## 9) API (Endpoints)

### 9.1 Auth
- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

### 9.2 Tenants
- `GET /api/v1/tenants/me` (dados do tenant atual)

### 9.3 Users (ADMIN)
- `POST /api/v1/users`
- `GET /api/v1/users?page=&size=`
- `PATCH /api/v1/users/{id}/active`
- `PUT /api/v1/users/{id}/roles`
- `PUT /api/v1/users/{id}/branches`

### 9.4 Branches
- `POST /api/v1/branches`
- `GET /api/v1/branches`
- `PATCH /api/v1/branches/{id}/active`

### 9.5 Catalog
**Categories**
- `POST /api/v1/categories`
- `GET /api/v1/categories`
- `PUT /api/v1/categories/{id}`

**Products**
- `POST /api/v1/products`
- `GET /api/v1/products?search=&page=&size=`
- `GET /api/v1/products/{id}`
- `PUT /api/v1/products/{id}`
- `PATCH /api/v1/products/{id}/active`

### 9.6 Inventory
**Stock**
- `GET /api/v1/branches/{branchId}/stock?page=&size=`
- `GET /api/v1/branches/{branchId}/stock/{productId}`

**Movements**
- `POST /api/v1/branches/{branchId}/movements`
- `GET /api/v1/branches/{branchId}/movements?page=&size=&type=&reason=`

**Transfers**
- `POST /api/v1/transfers`

### 9.7 Dashboard
- `GET /api/v1/dashboard/overview?branchId=`

---

## 10) Respostas e Erros (Padronização)

### 10.1 Resposta padrão (sucesso)
```json
{
  "success": true,
  "data": {},
  "meta": {}
}
```

### 10.2 Resposta padrão (erro)
```json
{
  "success": false,
  "error": {
    "code": "STOCK_INSUFFICIENT",
    "message": "Not enough stock for this operation",
    "details": []
  }
}
```

### 10.3 Códigos de erro recomendados
- `AUTH_INVALID_CREDENTIALS`
- `AUTH_TOKEN_EXPIRED`
- `FORBIDDEN_BRANCH_ACCESS`
- `PRODUCT_SKU_ALREADY_EXISTS`
- `STOCK_INSUFFICIENT`
- `STOCK_CONCURRENT_MODIFICATION` **NOVO**
- `VALIDATION_ERROR`

---

## 11) Stack Tecnológico Definitivo

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.2.x**
- **Spring Security 6**
- **Spring Data JPA**
- **Flyway** (migrations)
- **MySQL 8.0+**
- **Redis** (cache)
- **MapStruct** (mappers)
- **Bean Validation**
- **JUnit 5** (testes)
- **Testcontainers** (integração)

### Frontend
- **Angular 17+** (standalone components)
- **Angular Material**
- **RxJS**
- **RxAngular** (performance)

### DevOps
- **Docker** + Docker Compose
- **GitHub Actions** (CI/CD)
- **Railway/Render** (deploy)

---

## 12) Critérios de Pronto (Definition of Done)

- Código lintado e formatado
- Endpoints com validação de input
- Erros padronizados
- Migrações versionadas (Flyway)
- **Índices criados antes de testar performance**
- **Optimistic locking implementado**
- Paginação onde necessário
- Segurança aplicada por roles + branch access
- Logs consistentes
- **Cache implementado para dashboard**
- Testes de unidade em services críticos
- Testes de integração para endpoints principais
- **Testes de concorrência para transferências**
- Projeto deployado e documentado

---

## 13) Roadmap de Evolução (pós-case)

- Multi-depósitos por filial (warehouse dentro de branch)
- Leitor de código de barras
- Exportação CSV/Excel
- Relatórios por período
- Webhooks para integrações
- Auditoria avançada (eventos e trilha completa)
- Notificações (email/discord/slack)

---

## 14) Glossário

- **Tenant**: empresa (isolamento de dados)
- **Branch**: filial
- **Stock Movement**: registro de entrada/saída/ajuste
- **Transfer**: envio de estoque entre filiais
- **RBAC**: controle de acesso baseado em roles
- **ABAC (branch scope)**: restrição baseada em atributo (filial permitida)
- **Optimistic Locking**: estratégia de controle de concorrência usando version

---

## 15) Principais Mudanças v1.0 → v2.0

### Segurança e Concorrência
- ✅ Adicionado `@Version` para optimistic locking em `branch_product_stock`
- ✅ Transferências com controle de concorrência
- ✅ Testes de concorrência obrigatórios

### Performance
- ✅ PK composta trocada por surrogate key + unique constraint
- ✅ Índices compostos em queries críticas (dashboard, movimentos)
- ✅ Cache Redis para dashboard
- ✅ Estratégia de cache invalidation

### Modelo de Dados
- ✅ Campos adicionais em `products` (barcode, unit_of_measure, image_url, description)
- ✅ Campos adicionais em `branches` (address, phone, manager_name)
- ✅ Índices otimizados para queries de listagem e filtros

### Testes
- ✅ Testes de concorrência adicionados aos critérios de pronto
- ✅ Testes de integração com Testcontainers

### Documentação
- ✅ ADRs para decisões arquiteturais
- ✅ Migrations com rollback procedures
- ✅ Estrutura de sprint com tasks independentes
