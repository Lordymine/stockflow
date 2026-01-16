# StockFlow PRO

Sistema de Controle de Estoque Multi-Tenant com Multi-Filiais.

## Pré-requisitos

- Java 21
- Maven 3.9+
- Docker Desktop (para MySQL local)

## Como Rodar

### 1. Subir MySQL (Docker)

```bash
docker-compose up -d mysql
```

### 2. Rodar Aplicação

```bash
./mvnw spring-boot:run
```

### 3. Verificar Health

```bash
curl http://localhost:8080/actuator/health
```

## Stack

- Java 21
- Spring Boot 3.2.1
- MySQL 8.0
- Flyway
- Maven

## Ports

- Application: 8080
- MySQL: 3306

## Acesso

- Health: http://localhost:8080/actuator/health
- Actuator: http://localhost:8080/actuator
