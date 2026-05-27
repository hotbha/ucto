@echo off
REM ============================================================================
REM UCTO - Run Frontend Tests with Coverage
REM ============================================================================
setlocal enabledelayedexpansion

echo ==========================================
echo  UCTO Frontend - Running Tests + Coverage
echo ==========================================

cd /d "%~dp0..\frontend"

echo Installing dependencies...
call flutter pub get

echo Running: flutter test --coverage
call flutter test --coverage

echo.
echo ==========================================
echo  Test Results
echo ==========================================

if exist coverage\lcov.info (
    echo.
    echo Coverage data: coverage\lcov.info
    echo.
    echo To view as HTML, install lcov and run:
    echo   genhtml coverage/lcov.info -o coverage/html
) else (
    echo.
    echo Coverage report not found.
)

echo.
echo ==========================================
echo  Frontend tests complete.
echo ==========================================

pause
