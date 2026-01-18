# Documentation Index

Welcome to the StockFlow PRO repository knowledge base. This is a modular monolith inventory management system built with Spring Boot (backend) and Angular (frontend).

## Core Guides

### Getting Started
- [Project Overview](./project-overview.md) - System purpose, features, and architecture
- [Architecture Notes](./architecture.md) - Modular monolith design, DDD layers, module boundaries
- [Development Workflow](./development-workflow.md) - Branching, commits, testing, and PR process
- [Testing Strategy](./testing-strategy.md) - Unit, integration, and E2E testing approach
- [Tooling & Productivity Guide](./tooling.md) - Docker, Maven, Angular CLI, and development tools

### Reference
- [Glossary & Domain Concepts](./glossary.md) - Business terminology, entities, and domain rules
- [Data Flow & Integrations](./data-flow.md) - API contracts, authentication flow, data pipeline
- [Security & Compliance Notes](./security.md) - JWT authentication, RBAC, multi-tenancy security

## Repository Structure

```
StockFlow/
├── src/                    # Spring Boot backend (Java 21+)
│   ├── auth/              # Authentication module (JWT, refresh tokens)
│   ├── tenants/           # Multi-tenancy support
│   ├── users/             # User management with RBAC
│   ├── branches/          # Branch/filial management
│   ├── catalog/           # Product catalog (categories, products)
│   ├── inventory/         # Stock control, movements, transfers
│   ├── dashboard/         # Analytics and overview
│   └── shared/            # Shared kernel (exceptions, utils)
│
├── frontend/              # Angular 18+ frontend
│   └── src/
│       ├── core/          # Core services (auth, http, storage)
│       ├── features/      # Feature modules (auth, catalog, inventory)
│       └── shared/        # Shared components and layouts
│
├── docs/                  # Living project documentation
│   ├── adr/              # Architecture Decision Records
│   ├── api/              # OpenAPI specifications
│   └── tasks/            # Task tracking and conventions
│
├── docker/                # Docker configurations
├── pom.xml               # Maven build configuration
├── docker-compose.yml    # Local development stack
└── quick-start.sh        # Quick setup script
```

## Quick Reference

| Module | Purpose | Key Files |
|--------|---------|-----------|
| **Auth** | JWT authentication, refresh tokens | `src/auth/`, `docs/adr/0003-auth-jwt-refresh.md` |
| **Tenants** | Multi-tenancy isolation | `src/tenants/`, `docs/adr/0002-multi-tenancy.md` |
| **Catalog** | Products and categories | `src/catalog/`, `frontend/src/features/catalog/` |
| **Inventory** | Stock, movements, transfers | `src/inventory/`, `docs/adr/0004-inventory-concurrency.md` |
| **Dashboard** | Analytics and overview | `src/dashboard/`, `frontend/src/features/dashboard/` |

## Technology Stack

**Backend:**
- Java 21+ with Spring Boot 3.x
- Spring Security with JWT
- Spring Data JPA with Hibernate
- PostgreSQL (production) / H2 (testing)
- Flyway for migrations
- MapStruct for DTO mapping

**Frontend:**
- Angular 18+ with TypeScript
- Angular Material for UI components
- RxJS for reactive programming
- HttpClient with interceptors

**Infrastructure:**
- Docker & Docker Compose
- PostgreSQL
- Maven for backend build
- npm/yarn for frontend build

## Important ADRs (Architecture Decision Records)

1. **[ADR-0001: Modular Monolith Architecture](../../docs/adr/0001-arquitetura-modular-monolito.md)** - Why modular monolith over microservices
2. **[ADR-0002: Multi-Tenancy](../../docs/adr/0002-multi-tenancy.md)** - Tenant isolation strategy
3. **[ADR-0003: JWT with Refresh Tokens](../../docs/adr/0003-auth-jwt-refresh.md)** - Authentication flow
4. **[ADR-0004: Inventory Concurrency](../../docs/adr/0004-inventory-concurrency.md)** - Optimistic locking for stock
5. **[ADR-0005: Cache Invalidation](../../docs/adr/0005-cache-invalidation.md)** - Caching strategy

## Development Commands

**Backend:**
```bash
# Build and run backend
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build with Maven
./mvnw clean package
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev      # Development server
npm run build    # Production build
npm run test     # Run tests
```

**Full Stack:**
```bash
# Start all services (PostgreSQL + backend + frontend)
docker-compose up -d

# Or use quick-start script
./quick-start.sh
```

## Related Documentation

- [CLAUDE.md](../../CLAUDE.md) - Development rules and conventions
- [AGENTS.md](../../AGENTS.md) - AI agent collaboration guide
- [SETUP.md](../../SETUP.md) - Environment setup instructions
- [docs/tasks/00-OVERVIEW.md](../../docs/tasks/00-OVERVIEW.md) - Task tracking overview
