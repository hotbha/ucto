@echo off
REM ============================================================================
REM UCTO - Run Fullstack (Backend + Frontend)
REM ============================================================================
setlocal enabledelayedexpansion

echo ==========================================
echo  UCTO Fullstack - Starting All Services
echo ==========================================
echo.

REM Load .env from project root
if exist "%~dp0..\.env" (
    echo Loading environment from .env
    for /f "tokens=1,* delims==" %%a in (%~dp0..\.env) do (
        if not "%%a"=="" if not "%%b"=="" (
            set "%%a=%%b"
        )
    )
)

echo.
echo Opening two command windows for backend and frontend...
echo.

REM Start backend in a new window
start "UCTO Backend" cmd /c "%~dp0run_backend.bat"

REM Start frontend in a new window
start "UCTO Frontend" cmd /c "%~dp0run_frontend.bat"

echo.
echo ==========================================
echo  Both services are starting.
echo  Backend:  http://localhost:%SERVER_PORT%
echo  Frontend: http://localhost:3000
echo ==========================================
echo.
echo Close the service windows to stop.
pause
