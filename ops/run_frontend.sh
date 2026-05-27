#!/usr/bin/env bash
# =============================================================================
# UCTO - Run Flutter Frontend
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo " UCTO Frontend - Starting Flutter Web"
echo "=========================================="

cd "$PROJECT_ROOT/frontend"

# Get dependencies first
echo "Installing dependencies..."
flutter pub get

# Default host for backend API (can override with SERVER_HOST env)
SERVER_HOST="${SERVER_HOST:-localhost}"
SERVER_PORT="${SERVER_PORT:-8080}"

echo "Starting Flutter web on port 3000..."
echo "Backend API: http://$SERVER_HOST:$SERVER_PORT"

flutter run -d web --web-port 3000 \
    --dart-define=SERVER_HOST=$SERVER_HOST \
    --dart-define=SERVER_PORT=$SERVER_PORT
