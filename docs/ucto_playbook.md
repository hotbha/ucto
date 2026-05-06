# UCTO Agile Playbook

## Agile Ceremonies

### Sprint State Machine
```
NOT_STARTED → SPRINT_PLANNING → DESIGN_SPRINT → ARCHITECTURE_REVIEW → COMPLIANCE_CHECK → DEVELOPMENT_SPRINT → TESTING_SPRINT → SPRINT_REVIEW → RETROSPECTIVE → SPRINT_CLOSED
                                                                                                                 ↓ (any state) → SPRINT_BLOCKED
```

### Ceremony Details
1. **Sprint Planning** → BA interrogates requirements, defines backlog.
2. **Design Sprint** → UI/UX Expert generates screens, BA shows them to customer.
3. **Architecture Review** → Solutions Architect proposes integrations.
4. **Compliance Check** → Compliance Agent validates proposals.
5. **Development Sprint** → Developer builds code from approved screens (BLoC enforced).
6. **Testing Sprint** → Tester validates outputs.
7. **Sprint Review** → BA presents demo to customer.
8. **Retrospective** → Agents exchange feedback internally, BA updates docs.

### State Transitions
| From | To | Trigger |
|------|----|---------|
| NOT_STARTED | SPRINT_PLANNING | Sprint scheduled |
| SPRINT_PLANNING | DESIGN_SPRINT | Backlog defined by BA |
| SPRINT_PLANNING | SPRINT_BLOCKED | Requirements unclear after 3 BA clarification rounds |
| DESIGN_SPRINT | ARCHITECTURE_REVIEW | Screens finalized (FINAL_APPROVED) |
| DESIGN_SPRINT | SPRINT_BLOCKED | Screens rejected ×2 without resolution |
| ARCHITECTURE_REVIEW | COMPLIANCE_CHECK | Architect recommendations delivered |
| COMPLIANCE_CHECK | DEVELOPMENT_SPRINT | Compliance passed |
| COMPLIANCE_CHECK | SPRINT_BLOCKED | Compliance critical failure |
| DEVELOPMENT_SPRINT | TESTING_SPRINT | Code generation complete, tests compile |
| DEVELOPMENT_SPRINT | SPRINT_BLOCKED | Code fails to compile after 3 attempts |
| TESTING_SPRINT | SPRINT_REVIEW | All tests pass |
| TESTING_SPRINT | SPRINT_BLOCKED | Critical test failures |
| SPRINT_REVIEW | RETROSPECTIVE | Customer demo completed |
| SPRINT_REVIEW | SPRINT_BLOCKED | Customer rejects entire sprint |
| RETROSPECTIVE | SPRINT_CLOSED | Retro completed, action items logged |
| SPRINT_BLOCKED | (back to appropriate state) | Blocking issue resolved (admin action) |
| SPRINT_BLOCKED | SPRINT_CLOSED | Sprint abandoned (admin action) |

See [state_machines.md](state_machines.md) for complete state machine with escalation paths and time limits.

## Communication Protocol
- Customer ↔ BA → Only external channel
- BA ↔ Other Agents → Internal communication via **Redis Pub/Sub** (MVP)
- Other Agents ↔ Other Agents → Allowed internally, never with customer
- Audit logs record all agent interactions and updates
- BA clarification: **Maximum 3 rounds** per requirement batch. After round 3, escalation to UCTO Admin.

## Guardrails
- BA is the single voice to customer
- BA must never disclose internal codebase, architecture, or working style
- All other agents communicate internally only
- Max 3 BA clarification rounds before escalation
- Sprint auto-abandons after 7 days in SPRINT_BLOCKED state

## Escalation Path
1. BA (first escalation) — notified on any block
2. UCTO Admin (if unresolved after 24h)
3. Product Owner (if unresolved after 48h)

[← Back to README](README.md) | Related: [agent_guidelines.md](agent_guidelines.md), [screen_review.md](screen_review.md), [requirement_traceability_matrix.md](requirement_traceability_matrix.md), [state_machines.md](state_machines.md)
