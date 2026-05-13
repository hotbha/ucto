# Anti-Hallucination Guardrails — Global Rules for All Agents

> **Purpose:** Define the enforceable rules that prevent AI agents from fabricating information, making silent assumptions, or producing untraceable outputs.
> **Status:** Adopted | **Last Updated:** 2026-05-09

---

## Rule 1: No Silent Assumptions

**If something is not clearly specified or reasonably inferable from documented sources, create a CLARIFICATION_REQUEST instead of guessing.**

### Enforcement
- Agents MUST explicitly distinguish between three states in all outputs:
  - **Fact** — directly from PO (via BA) or from a confirmed artefact (BRD, ADR, AC)
  - **Inference** — BA's interpretation from facts; MUST be labeled as such
  - **Assumption** — unknown; MUST be marked and turned into a question for PO
- If an agent is unsure about a requirement, API spec, business rule, or constraint, they MUST send a CLARIFICATION_REQUEST with `needsHuman = true`.
- **Never** use placeholder values, "TODO" comments, or stubs in place of unresolved questions.

### Examples

| ❌ Incorrect (Hallucination) | ✅ Correct (Transparent) |
|-----------------------------|-------------------------|
| "The system will use Razorpay for payments." (when PO hasn't confirmed) | "Assumption: Razorpay for payments. Question for PO: Should we use Razorpay or another provider?" |
| "API returns 200 OK with user data." (when spec isn't finalized) | "Inference from BRD §3.2: API returns user data on successful auth. Needs PO confirmation on response shape." |
| "Database has an `invitations` table." (when not in schema) | "Fact: Schema does not include `invitations`. To implement invite feature, we need to add this table. Confirm with Architect." |

---

## Rule 2: Always Reference Source

**Every claim, decision, and artefact must reference its originating source.**

### Enforcement
- **Requirements** — Must cite PO statements, BRD sections, or previously agreed artefacts.
- **Architecture decisions** — Must reference the specific requirement or story that drove the decision.
- **Code** — Must reference the user story and acceptance criteria being addressed.
- **Tests** — Must reference the acceptance criteria they validate.
- **Documentation** — Must reference the implemented features and architecture decisions that justify the content.

### Format
```
[Source: <document/section/statement>] — <claim>
```

### Examples
- `[Source: BRD §3.1] — User authentication supports Google OAuth as primary flow.`
- `[Source: PO statement on 2026-05-08 via BA] — Free tier limited to 1 project.`
- `[Source: ADR-002, Story STORY-001] — Agent broker uses Redis Pub/Sub for MVP.`
- `[Inference from BRD §2.3 + BRD §4.1] — Project creation limit is per-tier because Free tier has 1 project max.`

---

## Rule 3: Use Options Instead of Inventions

**When multiple reasonable paths exist, propose 2-3 options with pros/cons and ask PO/BA to choose. Never unilaterally decide.**

### Enforcement
- If a decision involves trade-offs (cost, time, complexity, security), present options.
- Each option must include:
  - **Description** — what the option involves
  - **Pros** — benefits (2-3 bullet points)
  - **Cons** — drawbacks (2-3 bullet points)
  - **Estimated impact** — effort, cost, or timeline delta
  - **Recommendation** — which option the agent recommends (if any)
- If the agent has a strong recommendation, state it clearly but still present alternatives.

### Template
```json
{
  "options": [
    {
      "name": "Option A: ...",
      "pros": ["Pro 1", "Pro 2"],
      "cons": ["Con 1", "Con 2"],
      "effort": "2 days",
      "recommended": true
    },
    {
      "name": "Option B: ...",
      "pros": ["Pro 1", "Pro 2"],
      "cons": ["Con 1", "Con 2"],
      "effort": "5 days",
      "recommended": false
    }
  ],
  "needsHuman": true,
  "humanQuestions": [
    "Which approach should we take? Option A is faster but less flexible. Option B is slower but more scalable."
  ]
}
```

---

## Rule 4: Traceability

**Every decision and artefact must be traceable back through: PO input → BA interpretation → architecture/UX/code/test/docs.**

### Enforcement
- All artefacts must carry a `traceId` or reference chain.
- The chain must be reconstructable: from a line of code back to the acceptance criteria → story → PO request.
- Audit logs MUST capture the trace chain for every agent action.

### Trace Chain Example
```
PO statement (2026-05-08): "I need to invite team members"
  → BA interprets: EPIC-001 "Team Collaboration"
    → Story STORY-001: "As a Founder, I want to invite members..."
      → AC-1: "Founder can invite by email"
        → Code: ProjectController.inviteMember()
        → Test: invite_byEmail_ShouldReturn200
        → Doc: API docs → POST /api/projects/{id}/invite
```

---

## Rule 5: Keep Increments Small

**Implement and change small slices at a time. Do not refactor entire systems without explicit request.**

### Enforcement
- Each code change should address ONE user story (or one acceptance criterion if the story is large).
- Commits must be small, cohesive, and linked to a specific story/AC.
- Refactoring unrelated code is prohibited unless explicitly requested by the PO or PM.
- If an agent discovers a need for larger refactoring, they must flag it as `needsHuman = true` and propose the scope.

### Good Commit Example
```
feat: add project invite endpoint (STORY-001, AC-1)

- Added POST /api/projects/{id}/invite
- Email validation on invite request
- Permission check: only Founder can invite
```

### Bad Commit Example
```
Refactored entire auth system, fixed project controller, updated tests
```

---

## Summary: What to Do When Uncertain

| Situation | Action |
|-----------|--------|
| Requirement not specified | Send CLARIFICATION_REQUEST with `needsHuman = true` |
| Multiple implementation paths | Propose 2-3 options with pros/cons |
| Unknown API/data source | Flag `needsHuman = true` or `needsResearch = true` |
| Large refactoring needed | Flag `needsHuman = true` with scope proposal |
| PO says something unexpected | Accept it — PO is the single source of truth. Update artefacts accordingly. |
| Agent disagrees with another agent | Route through PM; PM escalates to BA/PO if unresolved |

[← Back to README](README.md) | Related: [message_structure.md](message_structure.md), [agent_guidelines.md](agent_guidelines.md)
