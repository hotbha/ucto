# Business Requirements Document (BRD)

## Purpose
Automate agile app development using agentic AI, enforcing strict requirements traceability and compliance.

## Scope
India-first platform with global scalability. Supports founders, startups, growth-stage, and enterprise customers.

## User Personas and Journeys
- Founder Persona: Tech-savvy entrepreneur, needs quick requirements to code, focuses on MVP delivery.
  - Journey: Signup → Project setup → Requirements input → BA clarification → Approve screens → Deploy.
- Developer Persona: Technical user, wants detailed specs and traceability.
  - Journey: Access project → Review requirements → View agent outputs → Implement code → Run tests.
- Viewer Persona: Non-technical stakeholder, needs high-level overviews.
  - Journey: Login → View dashboard → Read summaries → Provide feedback.

## Core Modules
- Unified Signup/Signin (Google, Facebook, OTP, Email + Password fallback)
  - Primary flow: Google/Facebook OAuth for quick access.
  - Fallback: Email + Password for users without social accounts.
  - OTP: SMS-based for password reset or additional verification.
  - Account collision handling: If social login matches existing email, prompt to link accounts.
- Role-based Access (UCTO Admin + Customer App roles)
  - UCTO Admin: Full platform access, manages users, agents, audit logs.
  - Customer App roles: Founder (creates projects, views outputs), Developer (accesses code, deploys), Viewer (read-only access to requirements and outputs).
  - Enterprise extension: Add Product Owner, Finance, Compliance roles with scoped permissions.
- Subscription Management (Chargebee)
- Audit Logging (token/API usage, failed attempts, cost per sprint)
- Compliance Enforcement (DPDP/GDPR)

Clarification Process: BA communicates with customer via portal chat or email for requirements clarification, not automated loops. Founders provide feedback on screens via approval/reject with reasons.

## Subscription Tiers and Limits
- Free: 1 project, 5 agent runs/month, basic BA + screen generation.
- Startup: 5 projects, 50 runs/month, all agents, basic audit.
- Growth: Unlimited projects, 200 runs/month, compliance checks, advanced audit.
- Enterprise: Custom limits, priority support, custom integrations.
- Billing: Monthly/annual via Chargebee, auto-upgrade/downgrade.
- Cost tracking: Per agent run, with sprint-based reporting.

## Enhancements
- Password fallback enforced
- Audit logs include failed attempts
- DigiLocker optional plugin for verified identity
