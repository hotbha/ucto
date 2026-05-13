# Closed-Loop Workflows

> **Purpose:** Define the four repeating closed loops that replace the traditional linear sprint waterfall. Each loop centers on the Product Owner (PO) via the BA Agent, and repeats until its exit criteria are met.
> **Status:** Adopted | **Last Updated:** 2026-05-09

---

## Overview

Instead of a single sequential pipeline (Plan → Design → Develop → Test → Deploy), the system uses **four concurrent, repeating loops**. Each loop has:
- **Participants** — which agents are involved
- **Trigger** — what starts the loop
- **Steps** — the sequence of actions
- **Output** — what is produced
- **Exit criteria** — when the loop is complete
- **Repetition** — conditions that cause the loop to run again

```
                    ┌─────────────────────────────────────────────────────┐
                    │                   PRODUCT OWNER                     │
                    │                 (Human / Founder)                   │
                    └──────────┬──────────┬──────────┬────────────────────┘
                               │          │          │
                    ┌──────────▼──────────▼──────────▼────────────────────┐
                    │               BA AGENT (Sole Interface)             │
                    └──┬──────────┬──────────┬──────────┬─────────────────┘
                       │          │          │          │
              ┌────────▼──┐ ┌────▼────┐ ┌───▼────┐ ┌──▼──────────┐
              │ Discovery  │ │  Build  │ │  Risk  │ │  UX/Doc     │
              │   Loop     │ │  Loop   │ │  Loop  │ │  Loop       │
              └────────────┘ └─────────┘ └────────┘ └─────────────┘
```

---

## Loop A: Discovery Loop

**Purpose:** Turn PO ideas/feature requests into structured, ready-for-development user stories.

### Participants
- **PO** (via BA) — provides vision, answers questions, makes trade-off decisions
- **BA Agent** — structures ideas into epics/stories/acceptance criteria; sole communicator with PO
- **Solution Architect Agent** — reviews feasibility, proposes architecture options
- **UI/UX Agent** — proposes flows and screen outlines
- **Compliance / Governance Agent** — reviews if data/risk is involved (optional trigger)

### Trigger
- PO expresses a new idea, feature request, or change
- PM identifies a gap in the backlog

### Steps
```
1. PO explains idea/feature in natural language → BA
2. BA structures it into epics, user stories, and acceptance criteria
3. BA shares structured requirements with Architect → feasibility check
4. Architect proposes architecture options (2-3 if ambiguous) with pros/cons
5. BA shares requirements with UI/UX → flow proposals and screen outlines
6. Compliance reviews if personal data, security, or regulatory risk is involved
7. BA collects all open questions from all agents
8. BA bundles questions → presents them to PO in a single, concise message
9. PO answers → BA updates requirements
10. Repeat steps 3-9 until Definition of Ready (DoR) is met
```

### Exit Criteria (Definition of Ready)
- [ ] Story has clear "As a [persona], I want [goal] so that [benefit]" format
- [ ] Acceptance criteria are defined and unambiguous
- [ ] Constraints and dependencies are documented
- [ ] Open questions have been resolved with PO
- [ ] Architect has confirmed feasibility (or documented trade-offs)
- [ ] UI/UX has proposed initial flows (or documented why not needed)
- [ ] Compliance has reviewed (or documented "not applicable")

### Output
- One or more **Ready** user stories added to the product backlog
- Updated requirements artifacts (BRD, acceptance criteria)

---

## Loop B: Build Loop

**Purpose:** Implement a single **Ready** user story as small, tested, working increments.

### Participants
- **PO** (via BA) — accepts or rejects completed work
- **BA Agent** — communicates results to PO in plain language
- **PM / Scrum Master Agent** — assigns tasks, tracks progress, enforces DoR/DoD
- **Developer Agent** — implements the story in small code increments
- **Tester / QA Agent** — derives and runs tests from acceptance criteria

### Trigger
- A user story has met **Definition of Ready (DoR)**
- PM assigns the story to Dev for implementation

### Steps
```
1. BA passes a Ready story to PM
2. PM creates implementation tasks and assigns to Dev
3. Dev implements small increments
   - Each commit linked to the story and acceptance criteria
   - Dev summarizes changes: what, why, which criteria addressed
4. Dev flags any missing knowledge as needs_human = true (never hallucinate)
5. QA designs tests from acceptance criteria (can run in parallel with Dev)
6. Dev completes implementation → QA executes tests
7. If tests fail: QA reports → Dev fixes → repeat step 6
8. If acceptance criteria are unclear: QA → BA → PO for clarification
9. When code passes all tests and matches acceptance criteria:
   QA marks story as "Ready for PO Review"
10. BA summarizes completed work in plain language → PO reviews
11. PO accepts → story marked "Done"
    OR PO requests changes → feedback becomes new backlog items → loop repeats
```

### Exit Criteria (Definition of Done)
- [ ] All acceptance criteria met
- [ ] All tests pass (automated + manual)
- [ ] Code is committed and linked to the story
- [ ] No known P0/P1 defects
- [ ] PO has accepted the work
- [ ] Documentation Agent has been notified for doc updates (UX/Doc Loop)

### Output
- Working, tested code for the user story
- Passed test results
- PO acceptance

---

## Loop C: Risk Loop

**Purpose:** Identify, assess, and mitigate security, privacy, and compliance risks continuously.

### Participants
- **PO** (via BA) — makes risk acceptance or mitigation decisions
- **BA Agent** — communicates risk findings and options to PO
- **Compliance / Governance Agent** — identifies risks, suggests mitigations
- **Solution Architect Agent** — proposes technical mitigations
- **Developer Agent** — implements mitigations (when needed)

### Trigger
- Compliance identifies a new risk during Discovery or Build loops
- A security/privacy issue is discovered during testing
- PO asks for a risk assessment on a specific feature
- Scheduled periodic risk review

### Steps
```
1. Compliance identifies issues: security gaps, privacy violations, regulatory concerns
2. Compliance produces a risk report with:
   - Issue description
   - Impact and likelihood
   - Suggested mitigations and controls
3. For major trade-offs (cost vs security, speed vs compliance):
   - Architect proposes 2-3 options with pros/cons
   - BA presents options to PO
4. PO chooses a path (accept risk, implement mitigation, or change scope)
5. Decision is recorded and constraints are updated in the requirements
6. If mitigation requires code changes → mitigation enters Build Loop
7. If PO accepts risk → risk is documented and monitored
```

### Exit Criteria
- All identified risks are either mitigated, accepted, or documented as deferred
- Risk report is updated with PO decisions
- No critical (P0) risks remain unaddressed

### Output
- Risk report with findings and decisions
- Updated compliance constraints
- Optional: mitigation tasks in the backlog

---

## Loop D: UX/Doc Loop

**Purpose:** Evolve UX mockups and living documentation continuously alongside implementation.

### Participants
- **PO** (via BA) — validates UX and documentation
- **BA Agent** — presents summaries to PO, collects feedback
- **UI/UX Agent** — creates/updates flows, screens, states
- **Documentation Agent** — maintains living docs (specs, API docs, guides, release notes)

### Trigger
- A user story is marked "Done" → documentation needs updating
- UI/UX proposes new flows for an upcoming story
- PO requests to see current UX or documentation state
- Scheduled periodic review

### Steps
```
1. UI/UX creates or updates user flows, screen mockups, state diagrams
   - Includes: empty, error, loading, success states
   - Follows: accessibility guidelines (labels, contrast, keyboard behavior)
2. Documentation Agent updates:
   - Product specs matching implemented features
   - API docs for new/changed endpoints
   - Setup/ops guides if infrastructure changed
   - Release notes for the current increment
3. BA reviews UX and doc updates for consistency
4. BA presents summaries to PO in natural language
5. PO validates and provides feedback
6. If changes needed: BA routes feedback to UI/UX or Documentation Agent
7. Updated artifacts are finalized and stored
```

### Exit Criteria
- UX artifacts reflect the current state of implemented features
- Documentation is in sync with code and architecture decisions
- PO has reviewed and accepted (or changes are documented for next iteration)

### Output
- Updated UX mockups/flows
- Updated living documentation
- Release notes for the increment

---

## Loop Orchestration

The **PM / Scrum Master Agent** decides which loop to run at any time:

| Condition | Loop to Activate |
|-----------|-----------------|
| New idea/feature from PO | Discovery Loop |
| A story meets DoR | Build Loop |
| Compliance issue identified | Risk Loop |
| Story marked "Done" | UX/Doc Loop |
| Multiple conditions true | Prioritize: Risk > Discovery > Build > UX/Doc |

**All four loops can run concurrently.** For example:
- Discovery Loop refines a new feature while Build Loop implements another
- Risk Loop runs in background for compliance review
- UX/Doc Loop updates docs for recently completed stories

[← Back to README](README.md) | Related: [agile_principles.md](agile_principles.md), [definition_of_ready_done.md](definition_of_ready_done.md), [message_structure.md](message_structure.md)
