@echo off
REM ============================================================================
REM UCTO - Run Flutter Frontend
REM ============================================================================
setlocal enabledelayedexpansion

echo ==========================================
echo  UCTO Frontend - Starting Flutter Web
echo ==========================================

cd /d "%~dp0..\frontend"

REM Load .env from project root if available
if exist "%~dp0..\.env" (
    echo Loading environment from .env
    for /f "tokens=1,* delims==" %%a in (%~dp0..\.env) do (
        if not "%%a"=="" if not "%%b"=="" (
            set "%%a=%%b"
        )
    )
)

REM Get dependencies first
echo Installing dependencies...
call flutter pub get

echo Starting Flutter web on port 3000...
call flutter run -d web --web-port 3000 ^
    --dart-define=SERVER_HOST=%SERVER_HOST% ^
    --dart-define=SERVER_PORT=%SERVER_PORT%

pause
