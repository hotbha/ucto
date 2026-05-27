@echo off
REM ============================================================================
REM UCTO - Run Spring Boot Backend
REM ============================================================================
setlocal enabledelayedexpansion

echo ==========================================
echo  UCTO Backend - Starting Spring Boot
echo ==========================================

cd /d "%~dp0..\backend"

REM Load .env from project root if available
if exist "%~dp0..\.env" (
    echo Loading environment from .env
    for /f "tokens=1,* delims==" %%a in (%~dp0..\.env) do (
        if not "%%a"=="" if not "%%b"=="" (
            set "%%a=%%b"
        )
    )
)

echo Starting backend on port %SERVER_PORT%...
call .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

pause
