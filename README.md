# StockFlow PRO

> Multi-tenant Inventory Management System with Branches

## Overview

StockFlow PRO is a professional inventory management system built with:
- **Multi-tenancy** by column (tenant_id)
- **Multi-branches** with access control
- **RBAC** (ADMIN, MANAGER, STAFF)
- **Inventory tracking** with movements and transfers
- **Redis caching** for performance
- **Modular Monolith** architecture with DDD

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.2.2
- Spring Security 6
- Spring Data JPA
- MySQL 8.0+
- Redis
- Flyway
- MapStruct
- Bean Validation
- SpringDoc OpenAPI

### Architecture
- Modular Monolith
- Domain-Driven Design (DDD)
- Clean Architecture layers (domain, application, infrastructure)

## Project Structure

```
src/main/java/com/stockflow/
├── StockFlowApplication.java          # Main application class
├── shared/                             # Shared kernel
│   ├── domain/
│   │   ├── model/BaseEntity.java      # Base entity with auditing
│   │   └── exception/                 # Domain exceptions
│   ├── application/
│   │   ├── dto/                       # Standard DTOs (ApiResponse, etc.)
│   │   └── mapper/                    # Base mapper interface
│   └── infrastructure/
│       ├── web/                       # GlobalExceptionHandler, WebConfig
│       ├── persistence/               # JPA configurations
│       └── security/                  # Security configurations
└── modules/                            # Domain modules
    ├── auth/                          # Authentication (JWT)
    ├── tenants/                       # Multi-tenancy
    ├── users/                         # Users and RBAC
    ├── branches/                      # Branches management
    ├── catalog/                       # Products and categories
    ├── inventory/                     # Stock and movements
    └── dashboard/                     # Dashboard and metrics
```

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 7+

### Using Docker Compose (Recommended)

```bash
# Start MySQL and Redis
docker-compose up -d

# Run the application
mvn spring-boot:run

# Access the application
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# Actuator: http://localhost:8080/actuator/health
```

### Manual Setup

```bash
# Install MySQL and Redis locally
# Configure connection in src/main/resources/application-dev.yml

# Run migrations
mvn flyway:migrate

# Run the application
mvn spring-boot:run
```

## Configuration

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/stockflow
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# JWT
JWT_SECRET=your-super-secret-key

# Server
SERVER_PORT=8080
```

### Profiles

- `dev`: Development profile with debug logging
- `test`: Test profile with H2 in-memory database
- `prod`: Production profile (customizable)

## API Documentation

### Swagger UI
- URL: http://localhost:8080/swagger-ui.html
- Interactive API documentation

### API Docs (JSON)
- URL: http://localhost:8080/api-docs

### Actuator Endpoints
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Info: http://localhost:8080/actuator/info

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BranchServiceTest

# Run with coverage
mvn test jacoco:report
```

## Development

### Code Conventions

Follow the conventions defined in `docs/tasks/00-CONVENCOES.md`:
- Entities: `*Entity` or domain aggregate name
- Repositories: `*Repository`
- Services: `*Service`
- Controllers: `*Controller`
- DTOs: `*Request` and `*Response`
- Mappers: `*Mapper`
- Exceptions: `*Exception`

### Layer Responsibilities

- **Domain**: Business rules, entities, repositories (interfaces)
- **Application**: Use cases, DTOs, mappers, transactions
- **Infrastructure**: Controllers, JPA implementations, security

### API Response Format

**Success:**
```json
{
  "success": true,
  "data": { ... },
  "meta": { ... }
}
```

**Error:**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Error description",
    "details": [...],
    "timestamp": "2024-01-16T10:30:00"
  }
}
```

## Database Migrations

```bash
# Run migrations
mvn flyway:migrate

# Validate migrations
mvn flyway:validate

# Check migration status
mvn flyway:info
```

Migrations are located in `src/main/resources/db/migration/`.

## Building for Production

```bash
# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/stockflow-pro-1.0.0.jar
```

## Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

## License

This project is licensed under the MIT License.

## Team

StockFlow Development Team

## Version

Current version: 1.0.0
