#!/usr/bin/env bash
# =============================================================================
# UCTO - Run Backend Tests with Coverage
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo " UCTO Backend - Running Tests + Coverage"
echo "=========================================="

cd "$PROJECT_ROOT/backend"

# Clean and run tests with coverage
echo "Running: mvn clean test jacoco:report"
./mvnw clean test jacoco:report

echo ""
echo "=========================================="
echo " Test Results"
echo "=========================================="

# Display test summary from surefire
if [ -f target/surefire-reports/*.txt ]; then
    echo ""
    echo "--- Test Summary ---"
    grep -h -E "Tests run:|BUILD" target/surefire-reports/*.txt 2>/dev/null || true
    head -5 target/surefire-reports/index.html 2>/dev/null | grep -oP 'Tests run: \d+' || true
fi

# Check if coverage report exists
if [ -f target/site/jacoco/index.html ]; then
    COVERAGE=$(grep -oP 'Total[^<]*<tfoot[^>]*>.*?<td[^>]*>\K[^<]*' target/site/jacoco/index.html 2>/dev/null || echo "N/A")
    echo ""
    echo "Coverage Report: target/site/jacoco/index.html"
    echo ""
    echo "Open coverage report:"
    echo "  start $PROJECT_ROOT/backend/target/site/jacoco/index.html (Windows)"
    echo "  open $PROJECT_ROOT/backend/target/site/jacoco/index.html (macOS)"
    echo "  xdg-open $PROJECT_ROOT/backend/target/site/jacoco/index.html (Linux)"
else
    echo "Coverage report not found (JaCoCo may not be configured)."
fi

echo ""
echo "=========================================="
echo " Backend tests complete."
echo "=========================================="
