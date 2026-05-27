# Testing Plan & Status

## Overview
This document tracks the testing strategy for the Unicornator (UCTO) system. Tests are organized by layer: unit, integration, and end-to-end. Each test is linked to agile user stories, acceptance criteria, and the relevant agent.

## Agile Testing Framework
Derived from [docs/definition_of_ready_done.md](definition_of_ready_done.md):
- **DoD Criterion 1**: All acceptance criteria must pass.
- **DoD Criterion 3**: All tests pass (unit, integration, e2e).
- **DoD Criterion 4**: No P0/P1 defects open.
- Agent: **Tester / QA Agent** owns test derivation and traceability.

## Test Coverage by Layer

### Backend Unit Tests (JUnit 5 + Mockito) — 24 Classes

| # | Test Class | Status | Coverage | Notes |
|---|-----------|--------|----------|-------|
| 1 | `AuthControllerTest` | ✅ PASSING | Controller layer | JWT auth, registration, login |
| 2 | `BAChatControllerTest` | ✅ PASSING | Controller layer | BA chat endpoint validation |
| 3 | `BAChatServiceTest` | ✅ PASSING | Service layer | BA processing logic (max 3 rounds, escalation) |
| 4 | `HealthControllerTest` | ✅ PASSING | Controller layer | Health endpoint returns 200 |
| 5 | `ProjectControllerTest` | ✅ PASSING | Controller layer | Project CRUD |
| 6 | `RequirementControllerTest` | ✅ PASSING | Controller layer | CRUD + clarification rounds |
| 7 | `ScreenControllerTest` | ✅ PASSING | Controller layer | Screen approval workflow |
| 8 | `SecurityEdgeCaseTest` | ✅ PASSING | Security layer | XSS, CSRF, path traversal |
| 9 | `AgentOrchestrationServiceTest` | ✅ PASSING | Service layer | Agent message routing |
| 10 | `AuditLogServiceTest` | ✅ PASSING | Service layer | Audit record creation |
| 11 | `BootstrapServiceTest` | ✅ PASSING | Service layer | Prompt-to-app skeleton |
| 12 | `DeepSeekAgentClientTest` | ✅ PASSING | Service layer | Circuit breaker states |
| 13 | `GlobalExceptionHandlerTest` | ✅ PASSING | Error handling | Consistent error responses |
| 14 | `JwtServiceTest` | ✅ PASSING | Service layer | JWT generation & validation |
| 15 | `ProjectServiceTest` | ✅ PASSING | Service layer | Project business logic |
| 16 | `PromptCatalogTest` | ✅ PASSING | Service layer | Prompt catalog operations |
| 17 | `QualityGateServiceTest` | ✅ PASSING | Service layer | Coverage threshold enforcement |
| 18 | `RepoWorkspaceServiceTest` | ✅ PASSING | Service layer | Repo workspace operations |
| 19 | `SimulatedSprintServiceTest` | ✅ PASSING | Integration | 5-agent sprint simulation |
| 20 | `SimulatedSprintServiceDbFailureTest` | ✅ PASSING | Integration | DB failure handling in sprint |
| 21 | `StubLLMAgentClientTest` | ✅ PASSING | Service layer | Fallback when no API key |
| 22 | `SubscriptionServiceTest` | ✅ PASSING | Service layer | Subscription management |
| 23 | `UsageMeterServiceTest` | ✅ PASSING | Service layer | Usage tracking & tier enforcement |
| 24 | `UctoBackendApplicationTests` | ✅ PASSING | Integration | Context load test |

### Backend Tests — Planned / In Progress

| Test Class | Status | Layer | Notes |
|-----------|--------|-------|-------|
| `PmControllerTest` | 🆕 TODO | Controller | PM/Scrum Master endpoints |
| `DocControllerTest` | 🆕 TODO | Controller | Documentation agent endpoints |
| `OrchestratorControllerTest` | 🆕 TODO | Controller | Orchestrator evaluation |
| `GuardrailServiceTest` | 🆕 TODO | Service | Anti-hallucination guardrails |
| `DoRValidatorTest` | 🆕 TODO | Service | Definition of Ready validation |
| `DoDValidatorTest` | 🆕 TODO | Service | Definition of Done validation |
| Redis Pub/Sub Integration | 🆕 TODO | Integration | Agent event routing |

### Backend Integration Tests

| Test | Status | Notes |
|-----|--------|-------|
| BA Chat → DB Integration | ✅ PASSING | Message persistence |
| Requirement CRUD Integration | ✅ PASSING | Full lifecycle |
| Simulated Sprint (5-agent) | ✅ PASSING | Full sprint simulation |
| Simulated Sprint DB Failure | ✅ PASSING | Graceful DB failure handling |
| Agent Event Routing | 🆕 TODO | Redis Pub/Sub flow |
| DoR → Status Transition | 🆕 TODO | Ready state enforcement |
| DoD → Status Transition | 🆕 TODO | Done state enforcement |

### Frontend Unit Tests (Flutter) — 9 Files

| # | Test File | Status | Notes |
|---|----------|--------|-------|
| 1 | `api_service_test.dart` | ✅ PASSING | API service layer |
| 2 | `auth_bloc_test.dart` | ✅ PASSING | Auth BLoC (login/logout) |
| 3 | `ba_chat_bloc_test.dart` | ✅ PASSING | BA chat BLoC |
| 4 | `ba_chat_model_test.dart` | ✅ PASSING | BA message model |
| 5 | `dashboard_screen_test.dart` | ✅ PASSING | Dashboard UI rendering |
| 6 | `login_screen_test.dart` | ✅ PASSING | Login screen UI |
| 7 | `models_test.dart` | ✅ PASSING | Data models |
| 8 | `project_bloc_test.dart` | ✅ PASSING | Project BLoC |
| 9 | `subscription_bloc_test.dart` | ✅ PASSING | Subscription BLoC |

### Manual QA Checklist

| Scenario | Status | Linked Acceptance Criteria |
|---------|--------|---------------------------|
| PO sends message → BA responds | ✅ PASSING | BA clarifies ambiguities |
| BA reaches max rounds → escalation | ✅ PASSING | Escalation triggered |
| New user registers → JWT issued | ✅ PASSING | Auth flow complete |
| Create sprint → backlog updated | 🆕 TODO | Sprint lifecycle |
| Add story with persona → DoR check | 🆕 TODO | DoR enforcement |
| Complete story → DoD check | 🆕 TODO | DoD enforcement |
| Agent message needs_human=true → BA routes | 🆕 TODO | Human-in-the-loop |
| Compliance finds risk → options to PO | 🆕 TODO | Risk loop |

## Test Execution Commands

### Using Ops Scripts (Recommended)

```bash
# Run all tests (backend + frontend) with coverage
ops/run_all_tests.bat          # Windows
ops/run_all_tests.sh           # Linux/macOS

# Run backend tests only with JaCoCo coverage
ops/run_backend_tests.bat      # Windows
ops/run_backend_tests.sh       # Linux/macOS

# Run frontend tests only with coverage
ops/run_frontend_tests.bat     # Windows
ops/run_frontend_tests.sh      # Linux/macOS
```

### Using Makefile

```bash
make backend-test    # Backend tests with JaCoCo coverage
make frontend-test   # Frontend tests with Flutter coverage
```

### Manual Commands

#### Backend
```bash
cd backend
./mvnw test                                              # All tests
./mvnw test -Dtest=BAChatControllerTest                  # Single test class
./mvnw clean test jacoco:report                          # With coverage
```

#### Frontend
```bash
cd frontend
flutter test                                             # All tests
flutter test test/ba_chat_bloc_test.dart                 # Single test file
flutter test --coverage                                   # With coverage
```

## Coverage Configuration

### Backend (JaCoCo)
- **Plugin**: `jacoco-maven-plugin` v0.8.12 in `backend/pom.xml`
- **Agent**: `prepare-agent` goal binds to Maven's `initialize` phase
- **Report**: `report` goal binds to Maven's `test` phase
- **Output**: `backend/target/site/jacoco/index.html`
- **Usage**: `./mvnw clean test jacoco:report` or `make backend-test`

### Frontend (Flutter)
- **Tool**: Built-in `flutter test --coverage`
- **Output**: `frontend/coverage/lcov.info`
- **HTML Report**: `genhtml frontend/coverage/lcov.info -o frontend/coverage/html`

## Known Issues

1. **Phase 2+ Tests**: Tests for PM, Documentation, and Orchestrator services (`PmControllerTest`, `DocControllerTest`, `OrchestratorControllerTest`, `GuardrailServiceTest`, `DoRValidatorTest`, `DoDValidatorTest`) are still TODO. These should be implemented as part of the agile methodology rollout.
2. **Agent Event Integration**: Redis Pub/Sub integration tests require a running Redis instance.
3. **DoR/DoD Enforcement**: The enforcement logic in `ProjectManagerService.updateStatus()` uses approved defaults for code review and test results — this should be replaced with actual QA test data.

## Test Traceability Matrix

| Requirement | Test Class(es) | Acceptance Criteria | Status |
|------------|---------------|---------------------|--------|
| BA Chat | `BAChatControllerTest`, `BAChatServiceTest` | BA is single PO interface, max 3 rounds | ✅ |
| Auth / JWT | `AuthControllerTest`, `JwtServiceTest`, `SecurityEdgeCaseTest` | JWT auth, RBAC, security hardening | ✅ |
| Requirements CRUD | `RequirementControllerTest` | CRUD + clarification rounds | ✅ |
| Usage Metering | `UsageMeterServiceTest` | Usage counter & tier enforcement | ✅ |
| Project Mgmt | `ProjectControllerTest`, `ProjectServiceTest` | Project CRUD + member mgmt | ✅ |
| Screen Approval | `ScreenControllerTest` | Screen lifecycle (3 rounds → escalation) | ✅ |
| Audit Logs | `AuditLogServiceTest` | Audit record creation | ✅ |
| Bootstrap | `BootstrapServiceTest` | Prompt-to-app skeleton | ✅ |
| LLM Integration | `DeepSeekAgentClientTest`, `StubLLMAgentClientTest` | Circuit breaker, fallback client | ✅ |
| Quality Gates | `QualityGateServiceTest` | Coverage threshold enforcement | ✅ |
| Sprint Simulation | `SimulatedSprintServiceTest`, `SimulatedSprintServiceDbFailureTest` | 5-agent sprint with DB | ✅ |
| Agent Orchestration | `AgentOrchestrationServiceTest` | Message routing | ✅ |
| Health Check | `HealthControllerTest` | Health endpoint returns 200 | ✅ |
| Error Handling | `GlobalExceptionHandlerTest` | Consistent error responses | ✅ |
| Subscription | `SubscriptionServiceTest` | Subscription management | ✅ |
| Prompt Catalog | `PromptCatalogTest` | Prompt catalog operations | ✅ |
| Repo Workspace | `RepoWorkspaceServiceTest` | Repo workspace operations | ✅ |
| Backlog Mgmt | `PmControllerTest` (TODO) | CRUD backlog items | 🆕 |
| Sprint Mgmt | `PmControllerTest` (TODO) | Sprint lifecycle | 🆕 |
| DoR | `DoRValidatorTest` (TODO) | 10 DoR criteria checked | 🆕 |
| DoD | `DoDValidatorTest` (TODO) | 7 DoD criteria checked | 🆕 |
| Guardrails | `GuardrailServiceTest` (TODO) | 5 anti-hallucination rules | 🆕 |
| Docs | `DocControllerTest` (TODO) | Living doc lifecycle | 🆕 |
| Orchestrator | `OrchestratorControllerTest` (TODO) | Loop evaluation matrix | 🆕 |
| Backlog Item Model | `models_test.dart`, `backlog_item.dart` | Backlog item model | ✅ |
| Sprint Model | `sprint_model_test.dart` (TODO) | Sprint model | 🆕 |

## Test Execution Summary

- **Total Backend Tests**: 24 test classes (all passing)
- **Total Frontend Tests**: 9 test files (all passing)
- **Coverage Tooling**:
  - Backend: JaCoCo (v0.8.12) in `backend/pom.xml`
  - Frontend: Flutter `--coverage` flag
- **Ops Scripts**: See `ops/` folder for run/test scripts

## Quick Test Commands

```bash
# Backend tests with coverage
ops/run_backend_tests.bat          # Windows
ops/run_backend_tests.sh           # Linux/macOS

# Frontend tests with coverage
ops/run_frontend_tests.bat         # Windows
ops/run_frontend_tests.sh          # Linux/macOS

# All tests (backend + frontend)
ops/run_all_tests.bat              # Windows
ops/run_all_tests.sh               # Linux/macOS

# Via Makefile
make backend-test
make frontend-test
```

[← Back to README](README.md) | Related: [requirement_traceability_matrix.md](requirement_traceability_matrix.md), [agent_guidelines.md](agent_guidelines.md), [ucto_playbook.md](ucto_playbook.md), [test_cases.md](test_cases.md)

Last updated: 2026-05-14
