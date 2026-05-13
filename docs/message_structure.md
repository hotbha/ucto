# Message Structure — Agent Communication Protocol

> **Purpose:** Define the standardized structured message format for all agent-to-agent communication in the UCTO system.
> **Status:** Adopted (replaces legacy format) | **Last Updated:** 2026-05-09

---

## Standard Message Envelope

Every internal message between agents MUST follow this structure:

```json
{
  "fromAgent": "<agent_id>",
  "toAgent": "<agent_id>",
  "type": "<MESSAGE_TYPE>",
  "storyId": "<story_or_epic_id>",
  "projectId": "<project_id>",
  "correlationId": "<traceable_identifier>",
  "timestamp": "2026-05-09T10:00:00Z",
  "needsHuman": false,
  "humanQuestions": [],
  "payload": { }
}
```

### Field Descriptions

| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `fromAgent` | ✅ | String | Sender agent identifier (e.g., `ba`, `pm`, `developer`, `tester`, `architect`, `ux`, `compliance`, `docs`) |
| `toAgent` | ✅ | String | Recipient agent identifier |
| `type` | ✅ | String | Message type (see catalog below) |
| `storyId` | ✅ | String | ID of the user story, epic, or backlog item this message pertains to |
| `projectId` | ✅ | String | Project identifier for multi-project isolation |
| `correlationId` | ✅ | String | Traceable ID linking this message to the original PO request or trigger event |
| `timestamp` | ✅ | ISO 8601 | When the message was created |
| `needsHuman` | ✅ | Boolean | `true` if this message requires human (PO) input before proceeding |
| `humanQuestions` | ⚠️ (if `needsHuman=true`) | Array of String | Concise list of questions for the PO; MUST be populated if `needsHuman=true` |
| `payload` | ✅ | Object (JSON) | Message-type-specific data (see payload schemas below) |

---

## Message Type Catalog

| Type | Description | Sender → Receiver | Purpose |
|------|-------------|-------------------|---------|
| `REQUIREMENTS_PACKAGE` | Structured requirements/AC | BA → PM | Ready story package for backlog |
| `ARCHITECTURE_SPEC` | Architecture options/feasibility | Architect → BA | Design review output |
| `UI_SPEC` | UI flows, screen mockups, states | UX → BA | Design review output |
| `IMPLEMENTATION_UPDATE` | Code changes with story links | Dev → PM | Progress update on build |
| `TEST_REPORT` | Test results against AC | QA → PM | Quality gate output |
| `COMPLIANCE_REPORT` | Risk/security/privacy findings | Compliance → BA | Risk assessment |
| `DOC_UPDATE` | Documentation changes/requests | Docs → BA | Living doc sync |
| `CLARIFICATION_REQUEST` | Ambiguity flagged by any agent | Any → BA | Needs PO input |
| `CLARIFICATION_RESPONSE` | Answer from PO via BA | BA → Any | PO's decision |
| `DOR_CHECK` | DoR validation request | PM → BA/Architect/UX | Pre-build gate |
| `DOD_CHECK` | DoD validation request | QA → PM/BA | Post-build gate |
| `LOOP_TRIGGER` | Request to activate a loop | PM → Orchestrator | Loop orchestration |
| `ERROR_REPORT` | Agent failure notification | Any → PM | Error handling |

---

## Payload Schemas by Type

### REQUIRMENTS_PACKAGE (BA → PM)
```json
{
  "epicId": "EPIC-001",
  "storyId": "STORY-001",
  "title": "As a founder, I want to invite team members so that we can collaborate",
  "description": "Full description...",
  "acceptanceCriteria": [
    "Founder can invite by email",
    "Invited user receives notification",
    "Invited user can accept and join project"
  ],
  "persona": "Founder",
  "constraints": ["Free tier: max 1 project"]
}
```

### ARCHITECTURE_SPEC (Architect → BA)
```json
{
  "feasible": true,
  "options": [
    {
      "name": "Direct DB query",
      "pros": ["Simple", "Fast to implement"],
      "cons": ["Tight coupling", "Hard to test"],
      "recommended": false
    }
  ],
  "assumptions": ["PostgreSQL 16 available"],
  "tradeOffs": ["Performance vs flexibility"],
  "risks": ["Indexing needed for large datasets"],
  "needsHuman": false
}
```

### IMPLEMENTATION_UPDATE (Dev → PM)
```json
{
  "storyId": "STORY-001",
  "acAddressed": ["AC-1", "AC-2"],
  "changesSummary": "Added invite endpoint POST /api/projects/{id}/invite with email validation",
  "filesChanged": [
    "backend/src/main/java/.../ProjectController.java",
    "backend/src/main/java/.../InviteService.java"
  ],
  "needsHuman": false,
  "humanQuestions": []
}
```

### COMPLIANCE_REPORT (Compliance → BA)
```json
{
  "riskLevel": "low",
  "findings": [
    {
      "issue": "Email addresses stored without encryption at rest",
      "impact": "low",
      "likelihood": "unlikely",
      "mitigation": "Enable TDE or column-level encryption",
      "status": "open"
    }
  ],
  "overallStatus": "pass_with_warnings",
  "needsHuman": true,
  "humanQuestions": [
    "Should we enable column-level encryption for email fields? This adds ~2 days dev time."
  ]
}
```

### TEST_REPORT (QA → PM)
```json
{
  "storyId": "STORY-001",
  "testsRun": 12,
  "passed": 11,
  "failed": 1,
  "skipped": 0,
  "failures": [
    {
      "testCase": "invite_nonExistentEmail_ShouldReturn404",
      "expected": "404 Not Found",
      "actual": "500 Internal Server Error",
      "assignedTo": "developer"
    }
  ],
  "overallStatus": "needs_fix",
  "doDMet": false
}
```

---

## needs_human = true Routing Protocol

When any agent sets `needsHuman = true`, the message must follow this strict routing path:

```
Origin Agent → PM Agent → BA Agent → PO (Human)
                ↓                          ↓
         BA bundles & simplifies    PO answers
         questions into plain
         language for PO
                ↓                          ↓
         BA receives answer ← ← ← ← ← ← ←
                ↓
         BA sends CLARIFICATION_RESPONSE
         back to origin agent (optionally
         via PM for traceability)
```

### Rules
- **Only BA interacts with PO.** No other agent may directly communicate with the human.
- **BA must aggregate** multiple `needs_human` questions from different agents into a single, concise message for the PO.
- **BA must simplify**: translate technical questions into business-impact language.
- **PM must log** all `needs_human` messages for audit traceability.
- **Maximum 3 clarification rounds** per requirement batch (existing rule, remains in effect).

---

## Legacy Format Deprecation

This structured format **fully replaces** the previous ad-hoc message format. All agent implementations must:
1. Use `fromAgent`/`toAgent` fields instead of implicit routing
2. Always set `needsHuman` (default `false`) with empty `humanQuestions` array
3. Always include `storyId` for traceability
4. Use the standardized `type` values from the catalog above

[← Back to README](README.md) | Related: [closed_loop_workflows.md](closed_loop_workflows.md), [agent_guidelines.md](agent_guidelines.md)
