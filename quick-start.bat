@echo off
REM StockFlow PRO - Quick Start Script for Windows
REM This script sets up the development environment and starts the application

echo ========================================
echo StockFlow PRO - Quick Start
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if Docker is installed
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Docker is not installed or not running
    echo Please install Docker Desktop from https://www.docker.com/products/docker-desktop
    echo You can still use the application if you have MySQL and Redis installed locally
    echo.
)

REM Start Docker containers if Docker is available
where docker >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Starting MySQL and Redis containers...
    docker-compose up -d
    echo.
    timeout /t 5 /nobreak >nul
)

REM Compile the project
echo Compiling the project...
call mvn clean compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed
    pause
    exit /b 1
)
echo.

REM Run Flyway migrations
echo Running database migrations...
call mvn flyway:migrate
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Migration failed (this is normal if database doesn't exist yet)
    echo.
)
echo.

REM Start the application
echo Starting StockFlow PRO...
echo Application will be available at: http://localhost:8080
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo Health Check: http://localhost:8080/actuator/health
echo.
echo Press Ctrl+C to stop the application
echo.
call mvn spring-boot:run

pause
