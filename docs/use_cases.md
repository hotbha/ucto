# Use Case Document (UCD)

## UC01: Business Analyst Agent
Interrogates requirements, captures feedback via BA-customer communication, updates BRD/UCD/TCD/UI/UX/tech stack automatically. Max 3 clarification rounds per requirement batch before escalation. See [state_machines.md](state_machines.md).

## UC02: Developer Agent
Generates Flutter + Spring Boot scaffolds using BLoC architecture. **MVP target: Flutter Web**. Code generation only from FINAL_APPROVED screens.

## UC03: Tester Agent
Validates compliance and traceability against auto-generated test cases. Runs after Developer agent completes. Results logged and communicated via BA.

## UC04: Compliance Agent
Enforces DPDP/GDPR, generates privacy policies and compliance checklists. Runs automated checks from [compliance_checklist.md](compliance_checklist.md). Failures block sprint progression.

## UC05: UI/UX Expert Agent
Designs flows, generates wireframes/mockups, ensures accessibility (WCAG 2.1). **Localization: Phase 2.** For MVP, generate for English only.

## UC06: Solutions Architect Agent
Suggests integrations and architecture, prioritizing India-first solutions. Runs during Architecture Review ceremony.

## UC07: CLI Extension
Triggers agents directly from VS Code (`ucto init`, `ucto sprint`, `ucto agent`, `ucto deploy`).

## UC08: Usage Metering & Tier Enforcement
**Increments**: Each `trigger` event to any agent topic counts as one agent run. See [usage_metering_design.md](usage_metering_design.md).
**Enforcement**: At limit exhaustion, graceful degradation (read-only, new triggers blocked, upgrade CTA shown).

### UC08 Flow
```
User triggers agent
  → UsageCounter checks remaining runs
  → If runs remaining > 0: publish trigger event, increment counter, log audit event
  → If runs remaining = 0: return 402 Payment Required, show upgrade CTA
  → On 1st of month: all counters reset to 0
```

## UC09: Project Member Management
**Actors**: Founder (owner), Developer, Viewer
**Flow**:
```
Founder creates project (Free: 1 project, Startup: 5 projects, Growth: 50 projects)
  → Founder invites members by email
  → Member accepts invitation
  → Member assigned role (Founder, Developer, or Viewer)
  → Role-scoped permissions enforced on all project operations
```

## UC10: Screen Approval Workflow
**Actors**: Customer (Founder), BA, UI/UX Agent, Compliance Agent, Tester Agent
**Flow**:
```
UI/UX generates screens (DRAFT → SCREENS_GENERATED)
  → BA submits screens for review (SUBMITTED_FOR_REVIEW)
  → Customer opens review (IN_REVIEW)
  → Per screen: Customer can APPROVE, REJECT, or CHANGES_REQUESTED
  → If changes requested: max 3 rounds per screen; then auto-escalation
  → If ≥80% of batch approved: batch can proceed (rejected screens re-generated later)
  → Compliance + Tester validate approved screens
  → All screens FINAL_APPROVED → Development begins
```
See [screen_review.md](screen_review.md) and [state_machines.md](state_machines.md) for full state machine.
