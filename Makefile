# =============================================================================
# UCTO - Build & Development Makefile
# =============================================================================

.PHONY: help backend-build backend-run frontend-get frontend-analyze frontend-build docker-build docker-up clean

help:
	@echo "UCTO Build Commands:"
	@echo "  make backend-build    - Compile the Spring Boot backend"
	@echo "  make backend-run      - Run the Spring Boot backend (dev profile)"
	@echo "  make frontend-get     - Install Flutter dependencies"
	@echo "  make frontend-analyze - Run Flutter static analysis"
	@echo "  make frontend-build   - Build Flutter web (release)"
	@echo "  make docker-build     - Build all Docker images"
	@echo "  make docker-up        - Start all containers with Docker Compose"
	@echo "  make clean            - Clean all build artifacts"

# Backend
backend-build:
	cd backend && ./mvnw compile -B

backend-run:
	cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
frontend-get:
	cd frontend && flutter pub get

frontend-analyze:
	cd frontend && flutter analyze

frontend-build:
	cd frontend && flutter build web --release \
		--dart-define=SERVER_HOST=192.168.1.100 \
		--dart-define=SERVER_PORT=8080

# Docker
docker-build:
	docker compose build

docker-up:
	docker compose up -d

# Clean
clean:
	cd backend && ./mvnw clean
	rm -rf frontend/build
	rm -rf frontend/.dart_tool
