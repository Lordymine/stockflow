#!/bin/bash

# StockFlow PRO - Quick Start Script for Linux/Mac
# This script sets up the development environment and starts the application

echo "========================================"
echo "StockFlow PRO - Quick Start"
echo "========================================"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed"
    echo "Please install Maven from https://maven.apache.org/download.cgi"
    exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "WARNING: Docker is not installed"
    echo "Please install Docker from https://www.docker.com/products/docker-desktop"
    echo "You can still use the application if you have MySQL and Redis installed locally"
    echo ""
fi

# Start Docker containers if Docker is available
if command -v docker &> /dev/null; then
    echo "Starting MySQL and Redis containers..."
    docker-compose up -d
    echo ""
    sleep 5
fi

# Compile the project
echo "Compiling the project..."
mvn clean compile -DskipTests
if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed"
    exit 1
fi
echo ""

# Run Flyway migrations
echo "Running database migrations..."
mvn flyway:migrate
if [ $? -ne 0 ]; then
    echo "WARNING: Migration failed (this is normal if database doesn't exist yet)"
    echo ""
fi
echo ""

# Start the application
echo "Starting StockFlow PRO..."
echo "Application will be available at: http://localhost:8080"
echo "Swagger UI: http://localhost:8080/swagger-ui.html"
echo "Health Check: http://localhost:8080/actuator/health"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

mvn spring-boot:run
