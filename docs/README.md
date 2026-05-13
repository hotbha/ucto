# UCTO - Agentic AI Platform for Automated Agile App Development

## Overview
UCTO is an agentic AI‑powered platform for automated agile app development. This documentation suite enforces enterprise standards, traceability, and confidentiality guardrails.

All documents are in .md format for developer‑friendly workflows, CI/CD integration, and reproducibility.

## Technology Stack
- **Frontend**: Flutter + BLoC Architecture (Dart) — **Flutter Web for MVP; mobile (iOS/Android) Phase 2**
- **Backend**: Spring Boot 4.0.6 (Java 25)
- **Database**: PostgreSQL (primary), Redis (cache + Pub/Sub)
- **Object Storage**: S3-compatible (Backblaze / Wasabi / AWS S3)
- **Infrastructure**: Docker Compose (MVP), Kubernetes (scaling)
- **CI/CD**: GitHub Actions

> ⚠️ **Deprecation note**: The files `frontend/package.json`, `vite.config.ts`, `tsconfig*.json`, and `frontend/web/index.html` are orphan/experimental React artifacts from an earlier exploration phase. They are **not** the MVP source of truth. The canonical frontend is Flutter + BLoC (`frontend/lib/`).

## Core Architecture
- **Flutter BLoC pattern**: Separate `blocs/`, `models/`, `ui/` layers
- **Spring Boot REST API**: Authentication, requirements, audit logs, compliance, orchestrator, PM, docs
- **8 AI Agents**: Business Analyst (BA), Project Manager/Scrum Master, Developer, Tester/QA, Compliance/Governance, UI/UX, Solution Architect, Documentation Agent
- **Agent Communication**: Standardized message structure with `needs_human` routing (see [message_structure.md](message_structure.md))
- **Message Broker**: Redis Pub/Sub for MVP; RabbitMQ planned for Phase 2
- **Closed-Loop Workflows**: 4 concurrent loops (Discovery, Build, Risk, UX/Doc) instead of linear sprint pipeline

## Documentation Index

### Core Documentation
- **[Business Requirements](business_requirements.md)** — BRD with user personas, modules, subscription tiers
- **[Use Cases](use_cases.md)** — All 10 use cases (UC01-UC10)
- **[System Architecture](system_architecture.md)** — Core services, databases, integrations, deployment
- **[Solutions Architecture](solutions_architect.md)** — Integration choices, auth strategies, India-first stack
- **[Glossary](glossary.md)** — Single source of truth for UCTO terminology

### Process & Workflow
- **[Agile Playbook](ucto_playbook.md)** — Sprint ceremonies, communication protocol, guardrails, state machines
- **[Screen Workflow](screen_review.md)** — Screen generation, presentation, feedback, approval gates
- **[Agent Guidelines](agent_guidelines.md)** — Per-agent responsibilities and communication rules
- **[Formal State Machines](state_machines.md)** — Signup, auth, BA clarification, screen approval, sprint lifecycle
- **[Agent Orchestration Design](agent_orchestration_design.md)** — Event flows, topic naming, retry/ timeout policies

### Testing & Quality
- **[Test Cases](test_cases.md)** — Authentication, roles, audit, compliance, CLI tests
- **[Requirement Traceability Matrix](requirement_traceability_matrix.md)** — Links requirements → agents → outputs → status
- **[Compliance Checklist](compliance_checklist.md)** — DPDP (India) and GDPR (EU) automated and manual checks

### Portal & CLI
- **[Portal & CLI Design](portal_cli_design.md)** — Web portal dashboard + VS Code extension commands
- **Traceability Panel**: Side panel showing audit logs + outputs
- **CI/CD Hooks**: Auto-generate GitHub Actions YAML

### Planning & Operations
- **[Cost Breakdown](costs.md)** — Infrastructure, AI, backend, frontend, monitoring estimates
- **[Deployment Readiness Plan](deployment_readiness_plan.md)** — First 10 customers onboarding and conversion
- **[Usage Metering Design](usage_metering_design.md)** — Agent run counting, tier enforcement, graceful degradation
- **[Next Refinements](next_refinements.md)** — Design system, localization, performance, analytics
- **[Decision Log](decision_log.md)** — Architecture Decision Record (ADR) tracking
- **[Analysis & Plan (Superseded)](analysis_and_plan.md)** — Gap analysis superseded by `documentation_remediation_plan.md`

## Core Traceability
Links requirements → agents → outputs → status. Ensures strict traceability across sprints. See [RTM](requirement_traceability_matrix.md) for full matrix.

## Agile Workflow
1. **Sprint Planning** → BA interrogates requirements, defines backlog
2. **Design Sprint** → UI/UX Expert generates screens, BA shows to customer
3. **Architecture Review** → Solutions Architect proposes integrations
4. **Compliance Check** → Compliance Agent validates proposals
5. **Development Sprint** → Developer builds code from approved screens (BLoC enforced)
6. **Testing Sprint** → Tester validates outputs
7. **Sprint Review** → BA presents demo to customer
8. **Retrospective** → Agents exchange feedback internally, BA updates docs

See [state_machines.md](state_machines.md) for formal state transitions and escalation paths.

## Agent Guardrails
- **BA** is the only customer‑facing agent
- **BA** must never disclose internal codebase, architecture, or working style
- **All other agents** communicate internally only
- **Developer** enforces Flutter BLoC architecture
- **Compliance, Tester, UI/UX, Solutions Architect** agents operate silently with BA as the communication bridge
- **BA clarification**: Maximum 3 rounds per requirement batch before escalation

## Key Principles
- **Screen‑first workflow** → customer alignment before development
- **BLoC architecture** → enterprise‑grade separation of concerns
- **Strict guardrails** → BA is the single voice to customer; confidentiality enforced
- **Traceability matrix** → every requirement linked to outputs and audit logs
- **India‑first integrations** → Zoho, Razorpay, MSG91, DigiLocker (Phase 2)
- **Scalable infra** → start lean with VPS + API inference, scale to Kubernetes + GPU

## Communication Protocol
- Customer ↔ BA → Only external channel
- BA ↔ Other Agents → Internal communication (Redis Pub/Sub for MVP)
- Other Agents ↔ Other Agents → Allowed internally, never with customer
- Audit logs record all agent interactions and updates

## 📂 File Structure
```
ucto/
├── backend/          # Spring Boot (Java 25)
├── frontend/         # Flutter + BLoC (Dart) — Flutter Web (MVP), mobile (Phase 2)
├── frontend/
│   ├── lib/          # Canonical Flutter BLoC code
│   ├── package.json  # ⚠️ Deprecated React artifact (do not use)
│   └── vite.config.ts # ⚠️ Deprecated React artifact (do not use)
├── src/              # VS Code Extension (TypeScript)
└── docs/             # Documentation suite (20+ files)
```

## ✅ Status
- Backend: Spring Boot scaffolded with auth, project, audit, subscription, screen, requirement, agent entities
- Frontend: Flutter BLoC scaffold with 12+ screens, 4 BLoC modules
- VS Code Extension: Commands registered (`ucto init`, `ucto sprint`, `ucto agent`, `ucto deploy`)
- Documentation: Complete suite with BRD, UCD, SA, TCD, RTM, Playbook, Agent Guidelines, State Machines, Orchestration Design
