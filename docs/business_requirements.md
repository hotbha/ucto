# Business Requirements Document (BRD)

## Purpose
Automate agile app development using agentic AI, enforcing strict requirements traceability and compliance.

## Scope
India-first platform with global scalability. Supports founders, startups, growth-stage, and enterprise customers.

## User Personas and Journeys
- **Founder Persona**: Tech-savvy entrepreneur, needs quick requirements to code, focuses on MVP delivery.
  - Journey: Signup → Project setup → Requirements input → BA clarification → Approve screens → Deploy.
- **Developer Persona**: Technical user, wants detailed specs and traceability.
  - Journey: Access project → Review requirements → View agent outputs → Implement code → Run tests.
- **Viewer Persona**: Non-technical stakeholder, needs high-level overviews.
  - Journey: Login → View dashboard → Read summaries → Provide feedback.

## Core Modules

### Unified Signup/Signin
**For MVP:**
- Google OAuth (primary flow)
- Email + Password (fallback for users without social accounts)
- OTP via SMS for password reset or additional verification
- Account collision handling: If social login matches existing email, prompt to link accounts

**Phase 2:**
- Facebook OAuth

### Role-based Access (UCTO Admin + Customer App Roles)
- **UCTO Admin**: Full platform access, manages users, agents, audit logs
- **Customer App roles**:
  - **Founder**: Creates projects, approves screens, views outputs. Maximum authority.
  - **Developer**: Accesses generated code, runs tests, deploys. Cannot approve screens.
  - **Viewer**: Read-only access to requirements, screens, and outputs.
- **Enterprise extension (Phase 2)**: Add Product Owner, Finance, Compliance roles with scoped permissions.

### Subscription Management (Chargebee)
- Manage tiers, billing, upgrades/downgrades
- See [usage_metering_design.md](usage_metering_design.md) for tier limits and enforcement

### Audit Logging
- Append-only record of all user and agent actions
- Tracks: token/API usage, failed login attempts, cost per sprint
- Required for DPDP/GDPR compliance

### Compliance Enforcement
- DPDP (India) and GDPR (EU) automated checks
- See [compliance_checklist.md](compliance_checklist.md) for full checklist

## Definition of "Agent Run"
An **agent run** is defined as one API `trigger` event to any single agent. For example:
- BA clarification: 1 run
- UI/UX screen generation: 1 run
- Developer code generation: 1 run
- Tester validation: 1 run
- Compliance check: 1 run
- Architect review: 1 run

A typical sprint with 6 agent triggers = 6 agent runs. See [usage_metering_design.md](usage_metering_design.md) for complete rules.

## Clarification Process
BA communicates with customer via portal chat for requirements clarification. 
- Maximum 3 clarification rounds per requirement batch
- After round 3, escalation to UCTO Admin
- See [state_machines.md](state_machines.md) for formal state machine

## Subscription Tiers and Limits

| Tier | Projects | Agent Runs/Month | Features | Enforcement at Limit |
|------|----------|-----------------|----------|-------------------|
| **Free** | 1 | 5 | BA agent, screen generation | Graceful degradation (read-only); new triggers blocked |
| **Startup** | 5 | 50 | All agents, basic audit | Same as above; upgrade CTA shown |
| **Growth** | 50 | 200 | Compliance checks, advanced audit | Same as above |
| **Enterprise** | Unlimited | Unlimited | Custom integrations, priority support | Never blocked |

- Billing: Monthly/annual via Chargebee, auto-upgrade/downgrade
- Cost tracking: Per agent run, with sprint-based reporting
- **At limit exhaustion**: User can still log in and view data. Agent triggers return 402 Payment Required. Upgrade CTA displayed prominently.

## Enhancements (Phase 2)
- Password fallback enforced
- Audit logs include failed attempts
- DigiLocker optional plugin for verified identity
- Facebook OAuth
