# Guia de Validação do Sistema de Autenticação

## 📋 Resumo das Correções Aplicadas

### 1. Mapeamento @IdClass Corrigido
- **UserRole.java**: Removido campo `id` redundante
- **UserBranch.java**: Removido campo `id` redundante
- Agora os campos individuais são marcados com `@Id`

### 2. Soft Delete Adicionado
- **Branch.java**: Adicionado `@SQLDelete` e `@Where`
- Branches deletados são marcados como `is_active = false`

### 3. Schema da Tabela Tenants Corrigido
- **V003__fix_tenant_tenant_id.sql**: Migration que:
  - Atualiza registros existentes (`tenant_id = id`)
  - Altera coluna para `NOT NULL`
  - Cria triggers para manter `tenant_id = id`

### 4. Schema das Tabelas Users e Branches Corrigido
- **V004__fix_users_branches_schema.sql**: Migration que adiciona:
  - `updated_at` na tabela `users`
  - `version` na tabela `users`

---

## 🧪 Como Validar

### Pré-requisitos
1. **MySQL** rodando em `localhost:3306`
2. **Banco de dados** `stockflow` criado
3. **Java 21** instalado
4. **Maven** instalado

### Passo 1: Limpar e Recriar Banco (Opcional - se quiser começar do zero)

```bash
# Conectar ao MySQL
mysql -u root -p

# Drop e recriar banco
DROP DATABASE IF EXISTS stockflow;
CREATE DATABASE stockflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### Passo 2: Executar Aplicação

```bash
# A aplicação irá executar as migrations automaticamente
mvn spring-boot:run
```

**Logs esperados no startup:**
```
Successfully applied 4 migrations
- V001__create_schema.sql
- V002__insert_default_tenant.sql
- V003__fix_tenant_tenant_id.sql
- V004__fix_users_branches_schema.sql
```

### Passo 3: Testar via Swagger UI

1. **Acessar Swagger:**
   ```
   http://localhost:8080/swagger-ui.html
   ```

2. **Testar Signup (Cadastro de Tenant e Admin):**
   ```http
   POST /api/v1/auth/signup
   Content-Type: application/json

   {
     "tenantName": "Minha Empresa",
     "tenantSlug": "minha-empresa",
     "adminName": "Administrador",
     "adminEmail": "admin@minhaempresa.com",
     "adminPassword": "Senha123@"
   }
   ```

   **Resposta esperada (201 Created):**
   ```json
   {
     "tenant": {
       "id": 1,
       "name": "Minha Empresa",
       "slug": "minha-empresa"
     },
     "user": {
       "id": 1,
       "name": "Administrador",
       "email": "admin@minhaempresa.com",
       "roles": ["ADMIN"]
     },
     "tokens": {
       "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
       "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
       "tokenType": "Bearer",
       "expiresIn": 900
     }
   }
   ```

3. **Testar Login:**
   ```http
   POST /api/v1/auth/login
   Content-Type: application/json

   {
     "email": "admin@minhaempresa.com",
     "password": "Senha123@"
   }
   ```

   **Resposta esperada (200 OK):**
   ```json
   {
     "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "tokenType": "Bearer",
     "expiresIn": 900,
     "user": {
       "id": 1,
       "name": "Administrador",
       "email": "admin@minhaempresa.com",
       "roles": ["ADMIN"],
       "branchIds": []
     }
   }
   ```

4. **Testar Endpoint Protegido:**
   ```http
   GET /api/v1/tenants/me
   Authorization: Bearer <accessToken_do_login>
   ```

   **Resposta esperada (200 OK):**
   ```json
   {
     "id": 1,
     "name": "Minha Empresa",
     "slug": "minha-empresa",
     "isActive": true
   }
   ```

5. **Testar Refresh Token:**
   ```http
   POST /api/v1/auth/refresh
   Content-Type: application/json

   {
     "refreshToken": "<refreshToken_do_login>"
   }
   ```

   **Resposta esperada (200 OK):**
   ```json
   {
     "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "tokenType": "Bearer",
     "expiresIn": 900
   }
   ```

6. **Testar Logout:**
   ```http
   POST /api/v1/auth/logout
   Content-Type: application/json

   {
     "refreshToken": "<refreshToken_do_login>"
   }
   ```

   **Resposta esperada (200 OK):**
   ```json
   {
     "success": true,
     "message": null,
     "data": null
   }
   ```

---

## 🧪 Executar Testes de Integração

```bash
# Testes completos do módulo de autenticação
mvn test -Dtest=AuthControllerIntegrationTest

# Ver relatório de cobertura
mvn test jacoco:report
```

**Testes esperados:**
- ✅ `testSignup_Success` - Cadastro de tenant e admin
- ✅ `testSignup_TenantSlugAlreadyExists` - Impede duplicação de slug de tenant
- ✅ `testLogin_Success` - Login com credenciais válidas
- ✅ `testLogin_InvalidCredentials` - Login falha com senha errada
- ✅ `testRefreshToken_Success` - Renovação de token
- ✅ `testLogout_Success` - Logout e revogação de token
- ✅ `testGetCurrentTenant_Success` - Acesso a endpoint protegido
- ✅ `testGetCurrentTenant_Unauthorized` - Falha sem token

---

## 🔍 Verificações Manuais no Banco

```sql
-- Conectar ao banco
mysql -u root -p stockflow

-- Verificar se tenants tem tenant_id = id
SELECT id, tenant_id, name, slug FROM tenants;

-- Verificar se roles foram criadas
SELECT * FROM roles;

-- Verificar se user foi criado
SELECT id, tenant_id, name, email, is_active FROM users;

-- Verificar se user_roles foi preenchida
SELECT * FROM user_roles;

-- Verificar refresh tokens
SELECT id, tenant_id, user_id, expires_at, revoked_at FROM refresh_tokens;
```

**Resultados esperados:**
- `tenants.tenant_id` deve ser igual a `tenants.id`
- `roles` deve ter 3 registros: ADMIN, MANAGER, STAFF
- `users` deve ter 1 usuário admin
- `user_roles` deve ter 1 registro associando admin ao role ADMIN

---

## 🐛 Troubleshooting

### Erro: "Table 'stockflow.tenants' doesn't exist"
**Solução:** Verificar se as migrations foram executadas. Verificar logs do Flyway.

### Erro: "Column 'tenant_id' cannot be null"
**Solução:** Executar migration V003 para corrigir schema da tabela tenants.

### Erro: "Column 'updated_at' doesn't exist in table 'users'"
**Solução:** Executar migration V004 para adicionar coluna faltante.

### Erro: "Unknown column 'is_active' in 'where clause'"
**Solução:** Isso é esperado. O @Where do Hibernate adiciona essa cláusula automaticamente.

### Login falha com 401 Unauthorized
**Possíveis causas:**
- Email ou senha incorretos
- Usuário inativo (is_active = false)
- Tenant não encontrado no contexto

### Refresh token falha com 401 Unauthorized
**Possíveis causas:**
- Token expirado
- Token revogado
- Token inválido

---

## ✅ Checklist de Validação Completa

- [ ] Aplicação inicia sem erros
- [ ] Migrations V001, V002, V003, V004 executadas com sucesso
- [ ] Swagger UI acessível em http://localhost:8080/swagger-ui.html
- [ ] Endpoint `/api/v1/auth/signup` funciona (201)
- [ ] Endpoint `/api/v1/auth/login` funciona (200)
- [ ] Endpoint `/api/v1/auth/refresh` funciona (200)
- [ ] Endpoint `/api/v1/auth/logout` funciona (200)
- [ ] Endpoint `/api/v1/tenants/me` requer autenticação (401 sem token)
- [ ] Endpoint `/api/v1/tenants/me` retorna tenant com token válido (200)
- [ ] Testes de integração passam (8/8)
- [ ] Banco de dados tem dados consistentes (tenant_id = id na tabela tenants)

---

## 📚 Próximos Passos

Após validar que o sistema de autenticação está funcional:

1. **Implementar módulo de Categories**
2. **Implementar módulo de Products**
3. **Implementar módulo de Inventory**
4. **Implementar módulo de Stock Movements**
5. **Implementar frontend Angular**

---

**Documento atualizado em:** 2025-01-16
**Versão:** 1.0.0
