#!/usr/bin/env bash
# =============================================================================
# UCTO - Run ALL Tests (Backend + Frontend) with Coverage
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================="
echo " UCTO - Running ALL Tests + Coverage"
echo "=========================================="
echo ""

TOTAL_FAILURES=0
BACKEND_FAILURES=0
FRONTEND_FAILURES=0

# ---- Backend Tests ----
echo "=========================================="
echo " [1/2] Backend Tests"
echo "=========================================="
cd "$SCRIPT_DIR/../backend"

echo "Running: mvn clean test jacoco:report"
./mvnw clean test jacoco:report && BACKEND_RESULT=$? || BACKEND_RESULT=$?

if [ $BACKEND_RESULT -ne 0 ]; then
    echo "WARNING: Backend tests had failures (exit code: $BACKEND_RESULT)"
    BACKEND_FAILURES=1
    TOTAL_FAILURES=1
else
    echo "Backend tests: PASSED"
fi

# Show test count summary
echo ""
echo "--- Backend Test Summary ---"
if ls target/surefire-reports/*.txt 2>/dev/null; then
    grep -h -E "^Tests run:" target/surefire-reports/*.txt 2>/dev/null | head -5 || true
fi

# Show coverage
if [ -f target/site/jacoco/index.html ]; then
    echo ""
    echo "Backend Coverage Report: target/site/jacoco/index.html"
fi

echo ""
echo ""

# ---- Frontend Tests ----
echo "=========================================="
echo " [2/2] Frontend Tests"
echo "=========================================="
cd "$SCRIPT_DIR/../frontend"

echo "Running: flutter test --coverage"
if flutter test --coverage; then
    echo "Frontend tests: PASSED"
else
    echo "WARNING: Frontend tests had failures"
    FRONTEND_FAILURES=1
    TOTAL_FAILURES=1
fi

# Show coverage
if [ -f coverage/lcov.info ]; then
    echo ""
    echo "Frontend Coverage Data: coverage/lcov.info"
fi

echo ""
echo ""

# ---- Final Summary ----
echo "=========================================="
echo " FINAL SUMMARY"
echo "=========================================="
if [ $TOTAL_FAILURES -eq 0 ]; then
    echo " All tests PASSED"
else
    echo " Some tests FAILED"
    [ $BACKEND_FAILURES -eq 1 ] && echo "  - Backend: FAILURES"
    [ $FRONTEND_FAILURES -eq 1 ] && echo "  - Frontend: FAILURES"
fi
echo ""
echo "Coverage Reports:"
echo "  Backend:  backend/target/site/jacoco/index.html"
echo "  Frontend: frontend/coverage/lcov.info (use genhtml for HTML)"
echo "=========================================="

exit $TOTAL_FAILURES
