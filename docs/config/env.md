# Variaveis de ambiente

## Backend
- APP_NAME=stockflow
- SERVER_PORT=8080
- DB_URL=jdbc:mysql://localhost:3306/stockflow
- DB_USERNAME=stockflow
- DB_PASSWORD=stockflow
- JPA_DDL=validate
- FLYWAY_ENABLED=true
- JWT_SECRET=change-me
- JWT_ISSUER=stockflow
- JWT_ACCESS_TTL_MINUTES=15
- JWT_REFRESH_TTL_DAYS=30
- REDIS_HOST=localhost
- REDIS_PORT=6379
- CACHE_DASHBOARD_TTL_SECONDS=300
- CACHE_TOP_PRODUCTS_TTL_SECONDS=600
- CORS_ALLOWED_ORIGINS=http://localhost:4200
- LOG_LEVEL_ROOT=INFO

## Testes
- TESTCONTAINERS_REUSE_ENABLE=true

## Observacoes
- `JWT_SECRET` deve ter entropia alta.
- `DB_URL` deve incluir `useSSL=false&serverTimezone=UTC` conforme o driver.
