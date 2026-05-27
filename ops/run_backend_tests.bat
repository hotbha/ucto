@echo off
REM ============================================================================
REM UCTO - Run Backend Tests with Coverage
REM ============================================================================
setlocal enabledelayedexpansion

echo ==========================================
echo  UCTO Backend - Running Tests + Coverage
echo ==========================================

cd /d "%~dp0..\backend"

echo Running: mvnw clean test jacoco:report
call .\mvnw.cmd clean test jacoco:report

echo.
echo ==========================================
echo  Test Results
echo ==========================================

REM Check for test results
if exist target\surefire-reports (
    echo.
    echo --- Test Summary ---
    for /r target\surefire-reports %%f in (*.txt) do (
        findstr /r "Tests run:" "%%f" 2>nul
    )
)

REM Check if coverage report exists
if exist target\site\jacoco\index.html (
    echo.
    echo Coverage Report: target\site\jacoco\index.html
    echo.
    echo Open in browser:
    echo   start target\site\jacoco\index.html
) else (
    echo.
    echo Coverage report not found (JaCoCo may not be configured).
)

echo.
echo ==========================================
echo  Backend tests complete.
echo ==========================================

pause
