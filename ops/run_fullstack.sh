#!/usr/bin/env bash
# =============================================================================
# UCTO - Run Fullstack (Backend + Frontend)
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================="
echo " UCTO Fullstack - Starting All Services"
echo "=========================================="
echo ""

# Load .env
if [ -f "$SCRIPT_DIR/../.env" ]; then
    echo "Loading environment from .env"
    set -a
    source "$SCRIPT_DIR/../.env"
    set +a
fi

# Trap to kill both processes on exit
cleanup() {
    echo ""
    echo "Shutting down all services..."
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
    wait $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
    echo "All services stopped."
}
trap cleanup EXIT INT TERM

# Start backend
echo "Starting backend..."
"$SCRIPT_DIR/run_backend.sh" &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

# Wait for backend to be ready (check health endpoint)
echo "Waiting for backend to be ready..."
BACKEND_PORT="${SERVER_PORT:-8080}"
for i in $(seq 1 30); do
    if curl -s "http://localhost:$BACKEND_PORT/api/health" > /dev/null 2>&1; then
        echo "Backend is ready! (attempt $i)"
        break
    fi
    if [ "$i" -eq 30 ]; then
        echo "Backend failed to start within 30 seconds."
        echo "Check backend logs for details."
    fi
    sleep 2
done

# Start frontend
echo ""
echo "Starting frontend..."
"$SCRIPT_DIR/run_frontend.sh" &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"

echo ""
echo "=========================================="
echo " Both services are running."
echo " Backend:  http://localhost:$BACKEND_PORT"
echo " Frontend: http://localhost:3000"
echo "=========================================="
echo " Press Ctrl+C to stop all services."
echo ""

# Wait for either process to exit
wait $BACKEND_PID $FRONTEND_PID
