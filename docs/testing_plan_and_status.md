# Testing Plan & Status

## Overview
This document tracks the testing strategy for the Unicornator (UCTO) system. Tests are organized by layer: unit, integration, and end-to-end. Each test is linked to agile user stories, acceptance criteria, and the relevant agent.

## Agile Testing Framework
Derived from docs/definition_of_ready_done.md:
- **DoD Criterion 1**: All acceptance criteria must pass.
- **DoD Criterion 3**: All tests pass (unit, integration, e2e).
- **DoD Criterion 4**: No P0/P1 defects open.
- Agent: **Tester / QA Agent** owns test derivation and traceability.

## Test Coverage by Layer

### Backend Unit Tests (JUnit 5 + Mockito)

| Test Class | Status | Coverage | Notes |
|---|---|---|---|
| `BAChatControllerTest` | ✅ PASSING | Controller layer | BA chat endpoint validation |
| `BAChatServiceTest` | ✅ PASSING | Service layer | BA processing logic |
| `RequirementControllerTest` | ✅ PASSING | Controller layer | CRUD + clarification rounds |
| `RequirementServiceTest` | 🆕 IN PROGRESS | Service layer | DoR/DoD validation, backlog mgmt |
| `AuthControllerTest` | ✅ PASSING | Security layer | JWT auth, registration, login |
| `SecurityEdgeCaseTest` | ✅ PASSING | Security layer | XSS, CSRF, path traversal |
| `GlobalExceptionHandlerTest` | ✅ PASSING | Error handling | Consistent error responses |
| `UsageMeterServiceTest` | ✅ PASSING | Service layer | Usage tracking |
| `PmControllerTest` | 🆕 TODO | Controller layer | PM/Scrum Master endpoints |
| `DocControllerTest` | 🆕 TODO | Controller layer | Documentation agent endpoints |
| `OrchestratorControllerTest` | 🆕 TODO | Controller layer | Orchestrator loop evaluation |
| `GuardrailServiceTest` | 🆕 TODO | Service layer | Anti-hallucination guardrails |
| `DoRValidatorTest` | 🆕 TODO | Service layer | Definition of Ready validation |
| `DoDValidatorTest` | 🆕 TODO | Service layer | Definition of Done validation |

### Backend Integration Tests

| Test | Status | Notes |
|---|---|---|
| BA Chat → DB Integration | ✅ PASSING | Message persistence |
| Requirement CRUD Integration | ✅ PASSING | Full lifecycle |
| Agent Event Routing | 🆕 TODO | Redis Pub/Sub flow |
| DoR → Status Transition | 🆕 TODO | Ready state enforcement |
| DoD → Status Transition | 🆕 TODO | Done state enforcement |

### Frontend Unit Tests (Flutter)

| Test File | Status | Notes |
|---|---|---|
| `ba_chat_model_test.dart` | ✅ PASSING | BA message model |
| `ba_chat_bloc_test.dart` | ✅ PASSING | BA chat BLoC |
| `dashboard_screen_test.dart` | ✅ PASSING | Dashboard UI |
| `backlog_item_model_test.dart` | 🆕 TODO | Backlog item model |
| `sprint_model_test.dart` | 🆕 TODO | Sprint model |
| `pm_service_test.dart` | 🆕 TODO | PM API service |
| `doc_service_test.dart` | 🆕 TODO | Doc API service |

### Manual QA Checklist

| Scenario | Status | Linked Acceptance Criteria |
|---|---|---|
| PO sends message → BA responds | ✅ PASSING | BA clarifies ambiguities |
| BA reaches max rounds → escalation | ✅ PASSING | Escalation triggered |
| New user registers → JWT issued | ✅ PASSING | Auth flow complete |
| Create sprint → backlog updated | 🆕 TODO | Sprint lifecycle |
| Add story with persona → DoR check | 🆕 TODO | DoR enforcement |
| Complete story → DoD check | 🆕 TODO | DoD enforcement |
| Agent message needs_human=true → BA routes | 🆕 TODO | Human-in-the-loop |
| Compliance finds risk → options to PO | 🆕 TODO | Risk loop |

## Test Execution Commands

### Backend
```bash
cd backend
mvn test
mvn test -Dtest=BAChatControllerTest  # Single test class
mvn verify  # Includes integration tests
```

### Frontend
```bash
cd frontend
flutter test
flutter test test/ba_chat_bloc_test.dart  # Single test file
```

## Known Issues

1. **Phase 2+ Tests**: Many tests for the new PM, Documentation, and Orchestrator services are TODO items. These should be implemented as part of the agile methodology rollout.
2. **Agent Event Integration**: Redis Pub/Sub integration tests require a running Redis instance.
3. **DoR/DoD Enforcement**: The enforcement logic in `ProjectManagerService.updateStatus()` uses approved defaults for code review and test results — this should be replaced with actual QA test data.

## Test Traceability Matrix

| Requirement | Test Class | Acceptance Criteria | Status |
|---|---|---|---|
| BA Chat | BAChatControllerTest | BA is single PO interface | ✅ |
| BA Chat | BAChatServiceTest | Max 3 clarification rounds | ✅ |
| Auth | AuthControllerTest | JWT-based authentication | ✅ |
| Backlog Mgmt | PmControllerTest (TODO) | CRUD backlog items | 🆕 |
| Sprint Mgmt | PmControllerTest (TODO) | Sprint lifecycle | 🆕 |
| DoR | DoRValidatorTest (TODO) | 10 DoR criteria checked | 🆕 |
| DoD | DoDValidatorTest (TODO) | 7 DoD criteria checked | 🆕 |
| Guardrails | GuardrailServiceTest (TODO) | 5 anti-hallucination rules | 🆕 |
| Docs | DocControllerTest (TODO) | Living doc lifecycle | 🆕 |
| Orchestrator | OrchestratorControllerTest (TODO) | Loop evaluation matrix | 🆕 |

Last updated: Phase 2 Agile Methodology Implementation
