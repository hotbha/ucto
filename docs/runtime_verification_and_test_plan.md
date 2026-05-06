# UCTO MVP — Runtime Verification & Test Plan

## Current State (Baseline)
- **Backend tests**: 1 file (`UctoBackendApplicationTests.java`), simple context-loads only
- **Frontend tests**: None (empty `test/` directory, empty `integration_test/`)
- **Key blocker for `@SpringBootTest`**: Main `application.properties` references Postgres/Redis. Test `application.properties` uses H2 but Redis/Chargebee beans still require connection.

---

## Phase A — Runtime Verification

### A1. Fix test profile to isolate external dependencies
Add `application-test.properties` that mocks Redis, Chargebee, and uses H2.

### A2. Fix `@SpringBootTest` context loading
Add `@ActiveProfiles("test")` and exclude auto-config for Redis.
Add `@MockBean` for RedisConnectionFactory or use an embedded Redis test dependency.

### A3. Verify Flutter Web builds
```bash
cd frontend && flutter pub get && flutter analyze
```

### A4. Verify Docker Compose assumptions
Check that `docker-compose.yml` has Postgres + Redis services defined.

---

## Phase B — Backend Test Suite

### B1. Unit Tests
- `SubscriptionService` — plan selection, usage calculation, free trial activation
- `UsageMeterService` — increment/decrement logic, limit enforcement
- `ScreenController` / RequirementController — state transition validation
- `ProjectService` — authorization checks, member management
- `AuditLogService` — event creation logic
- `JwtService` — token generation/validation

### B2. Repository Tests
- `UserRepository` — find by email, findByGoogleId
- `ProjectRepository` — findByOwnerId, countByOwnerId
- `ScreenRepository` — findByProjectId
- `AgentRunRepository` — countByUserIdAndDate

### B3. Web/API Tests (MockMvc)
- Auth endpoints: register, login, refresh, OAuth
- Project CRUD + member management
- Requirement CRUD + clarification flow
- Screen CRUD + approval state machine
- Subscription: plans, current usage, upgrade
- Agent trigger endpoint
- Audit log retrieval
- Health endpoint

### B4. Integration Tests
- Full register → login → create project → add member flow
- Usage exhaustion → 402 response
- Screen revision cycle (PENDING → CHANGES_REQUESTED → PENDING → APPROVED)
- Redis Pub/Sub agent event publish (via embedded Redis or mock)

### B5. Contract Tests
- Login response shape
- Error response shape
- Usage limit exceeded body
- Screen status transition responses

---

## Phase C — Flutter Test Suite

### C1. Unit Tests
- Model parsing (Project, Requirement, Screen, User, SubscriptionPlan)
- ApiService helper methods
- JWT decode/extract logic
- BLoC event-to-state transitions

### C2. Widget Tests
- Login screen
- Dashboard shell + navigation
- Project list/create dialogs
- Requirement list
- Screen review UI
- Error/loading states

### C3. Integration Tests
- Login → dashboard → logout flow
- Project creation → requirement → screen flow
- Usage limit error display

---

## Phase D — Exhaustive Test Cases Document
Create `docs/exhaustive_test_cases.md` covering all 15 categories.

---

## Phase E — Bug Fix Pass
Fix issues discovered by tests.

---

## Phase F — Final Validation
- `mvnw test` passes
- `flutter test` passes
- `flutter build web` succeeds
- All critical flows covered
