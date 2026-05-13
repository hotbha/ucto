# UCTO Agile Playbook

> **Last Updated:** 2026-05-09  
> **Purpose:** Define sprint ceremonies, closed-loop workflows, communication protocol, guardrails, and state machines for the UCTO multi-agent system.

---

## Overview — Closed Loops (Not a Linear Sprint)

The traditional linear sprint pipeline has been replaced by **four concurrent, repeating closed loops** centered on the Product Owner (PO). See [closed_loop_workflows.md](closed_loop_workflows.md) for the complete definition.

### Loop Summary
| Loop | Participants | Purpose |
|------|-------------|---------|
| **Discovery** | PO ↔ BA ↔ Architect ↔ UI/UX ↔ Compliance | Turn ideas into ready stories |
| **Build** | PO ↔ BA ↔ PM ↔ Dev ↔ QA | Implement ready stories incrementally |
| **Risk** | PO ↔ BA ↔ Compliance ↔ Architect/Dev | Identify and mitigate risks |
| **UX/Doc** | PO ↔ BA ↔ UI/UX ↔ Documentation | Evolve UX and living docs |

### Ceremony Details (Mapped to Loops)
1. **Sprint Planning** → PM checks backlog for Ready stories → **Build Loop** activates
2. **Design Sprint** → UI/UX generates screens during **Discovery Loop**
3. **Architecture Review** → Architect provides feasibility during **Discovery Loop**
4. **Compliance Check** → Compliance validates during **Risk Loop** (ongoing)
5. **Development Sprint** → Dev implements in **Build Loop** (small increments)
6. **Testing Sprint** → QA tests and validates DoD during **Build Loop**
7. **Sprint Review** → BA presents demo to PO (after Build Loop completes)
8. **Retrospective** → Agents exchange feedback internally, BA updates docs

---

## Story State Machine

```
New → In Discovery → Ready → In Progress → In Review → Done
   ↓ (DoR not met)     ↑          ↓              ↓
   └─── Back to Discovery Loop   Build Loop    QA validates DoD
                                              ↓
                                        Ready for PO Review
                                              ↓
                                      PO accepts → Done
                                      PO rejects → New
```

### State Transitions
| From | To | Trigger | Loop |
|------|----|---------|------|
| New | In Discovery | PO expresses new idea/feature | Discovery |
| In Discovery | Ready | DoR criteria all met (BA + Architect + UI/UX + Compliance sign-off) | Discovery |
| Ready | In Progress | PM assigns story to Dev (DoR verified) | Build |
| In Progress | In Review | Dev completes implementation, QA begins testing | Build |
| In Review | Ready for PO Review | QA validates DoD, all tests pass | Build |
| Ready for PO Review | Done | PO accepts work via BA | Build |
| Ready for PO Review | New | PO requests changes → new backlog item | Build → Discovery |
| Any | Blocked | Compliance issue, critical failure, or needs_human escalation | Risk |
| Blocked | (appropriate state) | Issue resolved by BA/PO | Risk |

---

## Definition of Ready (DoR)

A story is **Ready** only when ALL criteria are met. See [definition_of_ready_done.md](definition_of_ready_done.md) for the full checklist.

Key criteria:
- "As a [persona], I want [goal] so that [benefit]" format
- Acceptance criteria defined and unambiguous
- Feasibility confirmed by Architect
- UX flows proposed by UI/UX
- Compliance reviewed (or N/A)
- All open questions resolved with PO

**Enforcement**: PM verifies DoR before assigning to Dev. If missing, returns to Discovery Loop.

---

## Definition of Done (DoD)

A story is **Done** only when ALL criteria are met. See [definition_of_ready_done.md](definition_of_ready_done.md) for the full checklist.

Key criteria:
- All acceptance criteria met
- All tests pass (0 failures)
- Code committed with story reference
- No P0/P1 defects
- PO acceptance received
- Documentation notified

**Enforcement**: QA verifies DoD before "Ready for PO Review." PM verifies PO acceptance before "Done."

---

## Communication Protocol
- **PO ↔ BA** → Only external channel. BA is the single voice to PO.
- **BA ↔ Other Agents** → Internal communication via standardized message structure (see [message_structure.md](message_structure.md)).
- **Other Agents ↔ Other Agents** → Allowed internally, never with PO.
- **`needs_human = true` messages** must follow: Origin Agent → PM → BA → PO.
- **Audit logs** record all agent interactions and updates.
- **BA clarification**: Maximum **3 rounds** per requirement batch. After round 3, escalation to UCTO Admin.

---

## Guardrails
- BA is the single voice to PO
- BA must never disclose internal codebase, architecture, or working style
- Only BA communicates with PO — all other agents work internally
- Max 3 BA clarification rounds before escalation
- Story auto-abandons after 7 days in BLOCKED state
- Stories must satisfy DoR before Build Loop
- Stories must satisfy DoD before marking Done

## Escalation Path
1. **PM** (first escalation) — notified on any block between agents
2. **BA** (second escalation) — aggregates issues and presents to PO
3. **PO** (final decision) — resolves trade-offs, accepts/rejects
4. **UCTO Admin** — if unresolved after 48h (system-level escalation)

[← Back to README](README.md) | Related: [closed_loop_workflows.md](closed_loop_workflows.md), [definition_of_ready_done.md](definition_of_ready_done.md), [message_structure.md](message_structure.md), [agent_guidelines.md](agent_guidelines.md)
