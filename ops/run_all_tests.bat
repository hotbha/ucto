@echo off
REM ============================================================================
REM UCTO - Run ALL Tests (Backend + Frontend) with Coverage
REM ============================================================================
setlocal enabledelayedexpansion

echo ==========================================
echo  UCTO - Running ALL Tests + Coverage
echo ==========================================
echo.

set TOTAL_FAILURES=0

REM ---- Backend Tests ----
echo ==========================================
echo  [1/2] Backend Tests
echo ==========================================
cd /d "%~dp0..\backend"

echo Running: mvnw clean test jacoco:report
call .\mvnw.cmd clean test jacoco:report

if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Backend tests had failures (exit code: %ERRORLEVEL%)
    set TOTAL_FAILURES=1
) else (
    echo Backend tests: PASSED
)

echo.

REM Show test count from surefire
if exist target\surefire-reports (
    echo --- Backend Test Summary ---
    for /r target\surefire-reports %%f in (*.txt) do (
        findstr /r "^Tests run:" "%%f" 2>nul
    )
)

if exist target\site\jacoco\index.html (
    echo.
    echo Backend Coverage Report: target\site\jacoco\index.html
)

echo.
echo.

REM ---- Frontend Tests ----
echo ==========================================
echo  [2/2] Frontend Tests
echo ==========================================
cd /d "%~dp0..\frontend"

echo Running: flutter test --coverage
call flutter test --coverage

if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Frontend tests had failures
    set TOTAL_FAILURES=1
) else (
    echo Frontend tests: PASSED
)

if exist coverage\lcov.info (
    echo.
    echo Frontend Coverage Data: coverage\lcov.info
)

echo.
echo.

REM ---- Final Summary ----
echo ==========================================
echo  FINAL SUMMARY
echo ==========================================
if %TOTAL_FAILURES% EQU 0 (
    echo  All tests PASSED
) else (
    echo  Some tests FAILED
)
echo.
echo Coverage Reports:
echo   Backend:  backend\target\site\jacoco\index.html
echo   Frontend: frontend\coverage\lcov.info (use genhtml for HTML)
echo ==========================================

pause
