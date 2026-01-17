@echo off
echo ========================================
echo Running Dashboard Module Tests
echo ========================================
echo.

cd /d "%~dp0"

echo Test: DashboardControllerIntegrationTest
call mvnw.cmd test -Dtest=DashboardControllerIntegrationTest -Dspring.profiles.active=test
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ DashboardControllerIntegrationTest FAILED
    exit /b 1
)

echo.
echo Test: DashboardServiceImplTest
call mvnw.cmd test -Dtest=DashboardServiceImplTest -Dspring.profiles.active=test
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ DashboardServiceImplTest FAILED
    exit /b 1
)

echo.
echo ========================================
echo ✅ All Dashboard tests PASSED!
echo ========================================
pause
