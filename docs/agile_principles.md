# Agile Principles — UCTO Interpretation

> **Purpose:** Codify the four agile manifesto values with concrete, enforceable interpretations for the UCTO multi-agent system.
> **Status:** Adopted | **Last Updated:** 2026-05-09

---

## Principle 1: Customer Collaboration over Contract Negotiation

### UCTO Interpretation
The **Product Owner (PO) / Founder** is the single source of truth for all product decisions.

### Enforcement Rules
- The **BA Agent** is the **only** agent that communicates with the PO directly.
- All agents must prioritize PO goals and constraints over internal assumptions.
- PO decisions are final — they supersede any inferred or assumed requirements.
- If an agent identifies a conflict between PO direction and existing documentation, the PO direction wins.

### Guardrails
- BA must always frame questions in terms of business impact, priority, or trade-offs — never implementation details.
- BA must never tell the PO "the system expects X" — instead, ask "should the system do X or Y?"

---

## Principle 2: Working Software over Comprehensive Documentation

### UCTO Interpretation
Each iteration must produce **potentially shippable increments** — not just documents, designs, or plans.

### Enforcement Rules
- Every user story must map to working code that can be demonstrated.
- The Build Loop terminates only when **executable, tested code** exists for the story.
- Do not accept "documentation complete" as a milestone for a story — code must be demonstrable.
- Small, end-to-end vertical slices are preferred over horizontal layers (e.g., implement one full feature rather than "all database schemas" then "all APIs").

### Guardrails
- Developer Agent must produce compilable, runnable code — not stubs, TODOs, or skeleton files.
- Tester/QA Agent must verify the code executes against acceptance criteria, not just that files exist.
- Documentation Agent must reference **existing** code and features — never document something that hasn't been implemented.

---

## Principle 3: Responding to Change over Following a Plan

### UCTO Interpretation
The backlog can change at any time based on PO feedback. Agents must treat requirements as **evolving**, not fixed.

### Enforcement Rules
- The PO can re-prioritize, add, modify, or remove backlog items at any time (not just at sprint boundaries).
- When requirements change, all affected artifacts (requirements, architecture, UX, code, tests, docs) must be updated — but only in the **next relevant loop iteration**, not all at once.
- No agent should ever say "but that's what we planned" — the plan is a living document.

### Guardrails
- BA must handle requirement changes gracefully: acknowledge, document the change, update affected stories, and communicate impacts to the PO.
- PM Agent must re-evaluate DoR/DoD for changed stories before they re-enter the Build Loop.
- Dev Agent must not assume requirements are frozen after implementation begins — small changes can be accepted mid-sprint.

---

## Principle 4: Individuals and Interactions over Processes and Tools

### UCTO Interpretation
The PO is always in the loop through the **BA Agent**. BA must ensure mutual understanding, not just push tickets.

### Enforcement Rules
- The BA Agent must verify that the PO understands what will be built before marking a story as "Ready."
- BA must summarize technical implications in plain language for the PO.
- When an agent flags `needs_human = true`, the message must flow to the PO through the BA — never be silently resolved by an agent.

### Guardrails
- BA must use natural language summaries, not raw JSON or code, when communicating with the PO.
- BA must ask "does this match your expectation?" rather than assuming the PO understands technical details.
- If an agent proposes multiple options (e.g., architectural choices), BA must present them with pros/cons in business terms.

---

## Short Feedback Loops

All workflows are organized as **small closed loops** (Discovery, Build, Risk, UX/Doc) rather than a one-time waterfall. Each loop:
- Has a clear entry and exit criteria
- Involves the PO (via BA) at decision points
- Can repeat as many times as needed based on new information
- Produces a concrete output (requirements, code, risk mitigation, docs)

See [closed_loop_workflows.md](closed_loop_workflows.md) for the full loop definitions.

---

## Definition of Ready (DoR) and Definition of Done (DoD)

Every user story must satisfy **DoR** before development begins.
Every story must meet **DoD** before it is marked "Done."

See [definition_of_ready_done.md](definition_of_ready_done.md) for checklists and enforcement rules.

[← Back to README](README.md) | Related: [closed_loop_workflows.md](closed_loop_workflows.md), [definition_of_ready_done.md](definition_of_ready_done.md)
