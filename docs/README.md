# UCTO - Agentic AI Platform for Automated Agile App Development

## Overview
UCTO is an agentic AI‑powered platform for automated agile app development. This documentation suite enforces enterprise standards, traceability, and confidentiality guardrails.

All documents are in .md format for developer‑friendly workflows, CI/CD integration, and reproducibility.

## Technology Stack
- **Frontend**: Flutter + BLoC Architecture (Dart)
- **Backend**: Spring Boot (Java)
- **Database**: PostgreSQL (primary), Redis (cache)
- **Object Storage**: Backblaze / Wasabi / S3
- **Infrastructure**: Docker Compose (MVP), Kubernetes (scaling)
- **CI/CD**: GitHub Actions

## Core Architecture
- **Flutter BLoC pattern**: Separate `blocs/`, `models/`, `ui/` layers
- **Spring Boot REST API**: Authentication, requirements, audit logs, compliance
- **6 AI Agents**: Business Analyst, Developer, Tester, Compliance, UI/UX Expert, Solutions Architect

## Documentation Index

### Core Documentation
- **[Business Requirements](business_requirements.md)** - BRD with user personas, modules, subscription tiers
- **[Use Cases](use_cases.md)** - All 7 use cases (UC01-UC07)
- **[System Architecture](system_architecture.md)** - Core services, databases, integrations, deployment
- **[Solutions Architecture](solutions_architect.md)** - Integration choices, auth strategies, India-first stack

### Process & Workflow
- **[Agile Playbook](ucto_playbook.md)** - Sprint ceremonies, communication protocol, guardrails
- **[Screen Workflow](screen_review.md)** - Screen generation, presentation, feedback, approval gates
- **[Agent Guidelines](agent_guidelines.md)** - Per-agent responsibilities and communication rules

### Testing & Quality
- **[Test Cases](test_cases.md)** - Authentication, roles, audit, compliance, CLI tests
- **[Requirement Traceability Matrix](requirement_traceability_matrix.md)** - Links requirements → agents → outputs → status

### Portal & CLI
- **[Portal & CLI Design](portal_cli_design.md)** - Web portal dashboard + VS Code extension commands
- **Traceability Panel**: Side panel showing audit logs + outputs
- **CI/CD Hooks**: Auto-generate GitHub Actions YAML

### Planning
- **[Cost Breakdown](costs.md)** - Infrastructure, AI, backend, frontend, monitoring estimates
- **[Next Refinements](next_refinements.md)** - Design system, localization, performance, analytics

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

## Agent Guardrails
- **BA** is the only customer‑facing agent
- **BA** must never disclose internal codebase, architecture, or working style
- **All other agents** communicate internally only
- **Developer** enforces Flutter BLoC architecture
- **Compliance, Tester, UI/UX, Architect** agents operate silently with BA as the communication bridge

## Key Principles
- **Screen‑first workflow** → customer alignment before development
- **BLoC architecture** → enterprise‑grade separation of concerns
- **Strict guardrails** → BA is the single voice to customer; confidentiality enforced
- **Traceability matrix** → every requirement linked to outputs and audit logs
- **India‑first integrations** → Zoho, Razorpay, MSG91, DigiLocker
- **Scalable infra** → start lean with VPS + API inference, scale to Kubernetes + GPU

## Communication Protocol
- Customer ↔ BA → Only external channel
- BA ↔ Other Agents → Internal communication (event-driven via message broker)
- Other Agents ↔ Other Agents → Allowed internally, never with customer
- Audit logs record all agent interactions and updates

## 📂 File Structure
```
ucto/
├── backend/          # Spring Boot (Java)
├── frontend/         # Flutter + BLoC (Dart)
├── src/              # VS Code Extension (TypeScript)
└── docs/             # Documentation suite
```

## ✅ Status
- Backend: Spring Boot scaffolded with auth foundation
- Frontend: Flutter scaffold ready for implementation
- VS Code Extension: Commands registered (ucto init, sprint, agent, deploy)
- Documentation: Complete suite with BRD, UCD, SA, TCD, RTM, Playbook, Agent Guidelines
