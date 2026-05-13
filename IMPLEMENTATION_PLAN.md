# Implementation Plan: Agile Methodology Alignment

## Status: ✅ COMPLETE

This plan outlines the three-phase implementation to align the Unicornator (UCTO) system with the international agile best practices defined in the methodology document.

## Phase 1: Create New Documents ✅

Create 6 new methodology documents:

- [x] `docs/agile_principles.md` — 4 agile values with enforceable interpretations
- [x] `docs/closed_loop_workflows.md` — 4 concurrent closed loops (Discovery, Build, Risk, UX/Doc)
- [x] `docs/message_structure.md` — Standardized agent communication protocol
- [x] `docs/definition_of_ready_done.md` — DoR (10 criteria) + DoD (7 criteria)
- [x] `docs/anti_hallucination_guardrails.md` — 5 global rules
- [x] `docs/orchestrator_prompt_template.md` — Centralized orchestrator prompt

## Phase 2: Update Existing Files ✅

Update 4 existing documents:

- [x] `docs/agent_guidelines.md` — 9 agent roles with guardrails
- [x] `docs/ucto_playbook.md` — Restructured to closed-loop model
- [x] `docs/system_architecture.md` — Added PM, Documentation agents, backlog_items
- [x] `docs/README.md` — Updated docs index

## Phase 3: Backend Implementation ✅

### Entities (4 new)
- [x] `AgentMessage` — Structured message for agent-to-agent communication
- [x] `BacklogItem` — Epic/story/task with DoR/DoD tracking
- [x] `Sprint` — Iteration lifecycle with active loop tracking
- [x] `DocumentationRecord` — Living documentation records

### Repositories (4 new)
- [x] `AgentMessageRepository` — Agent message persistence
- [x] `BacklogItemRepository` — Backlog CRUD and queries
- [x] `SprintRepository` — Sprint lifecycle queries
- [x] `DocumentationRecordRepository` — Doc CRUD

### DTOs (6 new)
- [x] `PmRequest` / `PmResponse` — PM/Scrum Master API
- [x] `DocRequest` / `DocResponse` — Documentation Agent API
- [x] `OrchestratorRequest` / `OrchestratorResponse` — Orchestrator API

### Services (7 new)
- [x] `DoRValidator` — Enforces 10 DoR criteria
- [x] `DoDValidator` — Enforces 7 DoD criteria
- [x] `GuardrailService` — 5 anti-hallucination rules
- [x] `SourceReferenceValidator` — Traceability validation
- [x] `ProjectManagerService` — Backlog + sprint + loop orchestration
- [x] `DocumentationService` — Living doc lifecycle
- [x] `OrchestratorService` — Loop evaluation + message routing

### Controllers (3 new)
- [x] `PmController` — PM REST endpoints
- [x] `DocController` — Documentation REST endpoints
- [x] `OrchestratorController` — Orchestrator REST endpoints

### Updates (2 existing)
- [x] `AgentEventListener` — Added PM + Documentation topics
- [x] `AgentOrchestrationService` — Added PM + DOCUMENTATION agent types

## Phase 4: Frontend Implementation ✅

### Models (3 new)
- [x] `backlog_item.dart` — Backlog item with DoR/DoD
- [x] `sprint.dart` — Sprint with active loop tracking
- [x] `agent_message.dart` — Agent message with needs_human

### Services (3 new)
- [x] `pm_service.dart` — PM/Scrum Master API client
- [x] `doc_service.dart` — Documentation Agent API client
- [x] `orchestrator_service.dart` — Orchestrator API client

## Phase 5: Testing ✅

- [x] `docs/testing_plan_and_status.md` — Updated with new test coverage
- [x] Traceability matrix updated
- [x] New test stubs created for all new backend services

## Architecture Overview (After Implementation)

```
PO (Human) → BA Agent → PM/Scrum Master → Developer → Tester → Compliance
                                              ↓
                                         Orchestrator
                                              ↓
                                    (Discovery | Build | Risk | UX/Doc Loops)
                                              ↓
                                    Documentation Agent
```

### API Endpoints Added
- `POST /api/pm/action` — PM actions (CREATE_SPRINT, ADD_BACKLOG_ITEM, UPDATE_STATUS, RUN_LOOP, CHECK_DOR, CHECK_DOD)
- `GET /api/pm/backlog/{projectId}` — Get backlog
- `GET /api/pm/sprints/{projectId}` — Get sprints
- `POST /api/docs/action` — Doc actions (GENERATE, UPDATE, PUBLISH, ARCHIVE)
- `GET /api/docs/project/{projectId}` — Get docs
- `GET /api/docs/project/{projectId}/type/{docType}` — Get docs by type
- `POST /api/orchestrator/action` — Orchestrator actions (EVALUATE_NEXT_LOOP, ROUTE_MESSAGE, GET_LOOP_STATUS)
- `GET /api/orchestrator/status/{projectId}` — Loop status
- `GET /api/orchestrator/evaluate/{projectId}` — Evaluate next loop
