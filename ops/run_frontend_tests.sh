#!/usr/bin/env bash
# =============================================================================
# UCTO - Run Frontend Tests with Coverage
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo " UCTO Frontend - Running Tests + Coverage"
echo "=========================================="

cd "$PROJECT_ROOT/frontend"

# Get dependencies first
echo "Installing dependencies..."
flutter pub get

# Run tests with coverage
echo "Running: flutter test --coverage"
flutter test --coverage

echo ""
echo "=========================================="
echo " Test Results"
echo "=========================================="

# Check if coverage report exists
if [ -f coverage/lcov.info ]; then
    echo ""
    echo "Coverage data: coverage/lcov.info"
    echo ""
    echo "To view as HTML, install lcov and run:"
    echo "  genhtml coverage/lcov.info -o coverage/html"
    echo "  open coverage/html/index.html"
    
    # Display simple line coverage percentage
    if command -v lcov &> /dev/null; then
        LINES=$(lcov --summary coverage/lcov.info 2>&1 | grep "lines" | awk '{print $2}' | tr -d '%')
        echo "Line Coverage: ${LINES}%"
    fi
else
    echo "Coverage report not found."
fi

echo ""
echo "=========================================="
echo " Frontend tests complete."
echo "=========================================="
