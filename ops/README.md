# UCTO Operations Scripts

This folder contains scripts to run, test, and check coverage for the UCTO (Unicornator) platform.

## Prerequisites

- **Backend**: Java 21+, Maven Wrapper (`./mvnw` in `backend/`)
- **Frontend**: Flutter SDK 3.x with Dart 3.x
- **Environment**: Copy `.env.example` to `.env` and fill in required values

## Scripts Overview

### Running Services

| Script | Purpose |
|---|---|
| `run_backend.sh` / `run_backend.bat` | Start the Spring Boot backend (dev profile, port 8080) |
| `run_frontend.sh` / `run_frontend.bat` | Start the Flutter frontend (web, port 3000) |
| `run_fullstack.sh` / `run_fullstack.bat` | Start both backend and frontend concurrently |

### Running Tests

| Script | Purpose |
|---|---|
| `run_backend_tests.sh` / `run_backend_tests.bat` | Run all backend tests with JaCoCo coverage |
| `run_frontend_tests.sh` / `run_frontend_tests.bat` | Run all Flutter frontend with coverage |
| `run_all_tests.sh` / `run_all_tests.bat` | Run ALL tests (backend + frontend) and show results |

## Coverage Reports

After running test scripts, coverage reports are available at:

- **Backend JaCoCo**: `backend/target/site/jacoco/index.html`
- **Frontend Flutter**: `frontend/coverage/lcov.info` (use `genhtml` to generate HTML)

## Quick Start

```bash
# Run all tests first
ops/run_all_tests.bat

# Start fullstack
ops/run_fullstack.bat
```
