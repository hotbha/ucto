# Definition of Ready (DoR) and Definition of Done (DoD)

> **Purpose:** Establish the mandatory quality gates that every user story must pass before entering the Build Loop (DoR) and before being marked complete (DoD).
> **Status:** Adopted | **Last Updated:** 2026-05-09

---

## Definition of Ready (DoR)

A user story is **Ready** for development ONLY when ALL of the following criteria are met.

### DoR Checklist

| # | Criterion | Validated By | Evidence |
|---|-----------|-------------|----------|
| 1 | **User story format** — Story uses "As a [persona], I want [goal] so that [benefit]" | BA Agent | Story artefact |
| 2 | **Clear persona** — The user persona is identified (Founder, Developer, Viewer) | BA Agent | Story artefact |
| 3 | **Acceptance criteria defined** — At least one testable acceptance criterion exists | BA Agent | Story artefact |
| 4 | **Acceptance criteria unambiguous** — Each AC is specific, measurable, and testable | BA + QA Agents | QA review sign-off |
| 5 | **Constraints documented** — Known constraints (technical, business, regulatory) are listed | BA Agent | Story artefact |
| 6 | **Dependencies identified** — Blocking dependencies on other stories, services, or external systems are documented | BA Agent | Story artefact |
| 7 | **Open questions resolved** — All questions from agents have been answered by the PO (via BA) | BA Agent | CLARIFICATION_RESPONSE history |
| 8 | **Feasibility confirmed** — Solution Architect has reviewed and confirmed feasibility (or documented trade-offs) | Solution Architect Agent | ARCHITECTURE_SPEC with `feasible: true` or documented trade-offs |
| 9 | **UX flows proposed** — UI/UX Agent has proposed initial flows or documented why not needed | UI/UX Agent | UI_SPEC or `not_applicable` note |
| 10 | **Compliance reviewed** — Compliance Agent has reviewed or documented "not applicable" | Compliance Agent | COMPLIANCE_REPORT with `overallStatus: pass` or `not_applicable` |

### Enforcement
- **PM Agent** MUST verify ALL DoR criteria before assigning a story to the Developer Agent.
- If any criterion is not met, the PM returns the story to the Discovery Loop with a DOR_CHECK message listing missing criteria.
- Stories that bypass DoR MUST be rejected by the PM.

---

## Definition of Done (DoD)

A user story is **Done** ONLY when ALL of the following criteria are met.

### DoD Checklist

| # | Criterion | Validated By | Evidence |
|---|-----------|-------------|----------|
| 1 | **All acceptance criteria met** — Every AC has been verified as implemented and working | Tester / QA Agent | TEST_REPORT with `passed >= total` |
| 2 | **All tests pass** — Unit, integration, and acceptance tests all pass with 0 failures | Tester / QA Agent | TEST_REPORT with `failed: 0` |
| 3 | **Code committed** — All code changes are committed to version control with references to the story ID | Developer Agent | IMPLEMENTATION_UPDATE with `storyId` |
| 4 | **No P0/P1 defects** — No critical or high-severity defects are open against this story | Tester / QA Agent | TEST_REPORT with no P0/P1 failures |
| 5 | **PO acceptance** — The Product Owner (via BA) has reviewed and accepted the work | BA Agent + PO | PO acceptance recorded in audit log |
| 6 | **Documentation notified** — Documentation Agent has been informed to update living docs | Developer Agent | DOC_UPDATE triggered or notification sent |
| 7 | **Code review completed** — Code follows architecture constraints and project conventions | Solution Architect Agent | Architecture compliance sign-off |

### Enforcement
- **QA Agent** MUST verify ALL DoD criteria before marking a story as "Ready for PO Review."
- **PM Agent** MUST verify DoD criteria 5-7 before marking the story as "Done" in the backlog.
- If any criterion is not met, the PM returns the story to the appropriate loop (Build Loop for code/test fixes, Discovery Loop for requirement changes, UX/Doc Loop for documentation).
- Stories NOT meeting DoD must NOT be counted toward sprint velocity.

---

## Relationship to Loops

```
Discovery Loop (DoR)
  │
  ▼
  [DoR check by PM]
  │
  ├── All criteria met ──→ Build Loop
  │
  └── Criteria missing ──→ Back to Discovery Loop

Build Loop (Impl + Test)
  │
  ▼
  [DoD check by QA + PM]
  │
  ├── All criteria met ──→ Story marked "Done"
  │                        → UX/Doc Loop triggered
  │
  └── Criteria missing ──→ Back to Build Loop (or appropriate loop)
```

---

## Escalation

If a story cannot meet DoR or DoD after 3 attempts:
1. PM flags with `needsHuman = true` and routes to BA
2. BA presents the blocking issue to PO
3. PO decides: adjust criteria, descope, or abandon the story

[← Back to README](README.md) | Related: [closed_loop_workflows.md](closed_loop_workflows.md), [message_structure.md](message_structure.md), [agile_principles.md](agile_principles.md)
