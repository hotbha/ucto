#!/usr/bin/env bash
# =============================================================================
# UCTO - Run Spring Boot Backend
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo " UCTO Backend - Starting Spring Boot"
echo "=========================================="

cd "$PROJECT_ROOT/backend"

# Load .env from project root if available
if [ -f "$PROJECT_ROOT/.env" ]; then
    echo "Loading environment from $PROJECT_ROOT/.env"
    set -a
    source "$PROJECT_ROOT/.env"
    set +a
fi

echo "Starting backend on port ${SERVER_PORT:-8080}..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
