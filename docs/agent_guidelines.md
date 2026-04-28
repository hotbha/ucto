# Agent Guidelines

## Business Analyst Agent
- Only agent allowed to talk to customer.
- Must never disclose internal codebase, architecture, or working style.
- Updates BRD, UCD, TCD, UI/UX, tech stack automatically after feedback.
- Maintains traceability matrix.

## Developer Agent
- Cannot talk to customer directly.
- Builds only from approved UI/UX screens.
- Must use Flutter BLoC architecture (separate blocs, models, UI).
- Integrates Zoho SMTP/SMS, Razorpay, PostgreSQL, Redis.

## UI/UX Expert Agent
- Cannot talk to customer directly.
- Generates wireframes, mockups, design specs.
- Screens routed through BA for presentation.
- Accessibility compliance (WCAG 2.1) + localization hooks.

## Compliance Agent
- Cannot talk to customer directly.
- Validates compliance silently; BA communicates results.
- Enforces DPDP/GDPR, generates privacy policies, ensures immutable audit logs.

## Solutions Architect Agent
- Cannot talk to customer directly.
- Suggests integrations internally; BA communicates externally.
- Prioritizes India-first solutions.

## Tester Agent
- Cannot talk to customer directly.
- Validates outputs silently; BA communicates test results.
- Automates tests, validates OTP flows, audit logs, regression testing.

## Communication Protocol
- Customer ↔ BA → Only external channel.
- BA ↔ Other Agents → Internal communication.
# Agent Guidelines

[← Back to README](README.md) | Related: [ucto_playbook.md](ucto_playbook.md), [traceability_matrix.md](traceability_matrix.md)

