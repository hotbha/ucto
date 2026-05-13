# Hero Flow MVP — Simulated Sprint

> **Purpose:** Define a single "Simulated Sprint" flow that orchestrates all agents in simulation mode, producing gate evaluations visible in the frontend "Quality & Agents" tab.
> **Status:** Draft | **Last Updated:** 2026-05-13
> 
> **Related:** [system_architecture.md](system_architecture.md) — see "Resilience Patterns" section for circuit breaker and git clone retry details.



---

## 1. Overview

The Simulated Sprint is the UCTO hero flow — a single API call that runs the full agent pipeline (BA → Architect → Developer → Tester → Compliance) in **simulation mode**, evaluates quality gates, and returns a summary of results.

All agents use `StubLLMAgentClient` (no real LLM calls). Real Git operations are skipped (simulation flag). The output is visible immediately in the "Quality & Agents" tab.

---

## 2. Input

```
POST /api/projects/{id}/simulated-sprint
Content-Type: application/json

{
  "branch": "main",
  "changeDescription": "Add user profile page with edit functionality"
}
```

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `branch` | String | No | `"main"` | Target branch for the sprint |
| `changeDescription` | String | Yes | — | Natural-language description of the change |

---

## 3. Output

```json
{
  "projectId": 1,
  "branch": "main",
  "changeDescription": "Add user profile page",
  "correlationId": "sim_sprint_20260513_abc123",
  "steps": [
    { "agent": "BA", "status": "COMPLETED", "simulation": true, "eventId": "evt_..." },
    { "agent": "ARCHITECT", "status": "COMPLETED", "simulation": true, "eventId": "evt_..." },
    { "agent": "DEVELOPER", "status": "COMPLETED", "simulation": true, "eventId": "evt_..." },
    { "agent": "TESTER", "status": "COMPLETED", "simulation": true, "eventId": "evt_..." },
    { "agent": "COMPLIANCE", "status": "COMPLETED", "simulation": true, "eventId": "evt_..." }
  ],
  "gateStatus": {
    "overallPass": false,
    "testGate": { "gateType": "TEST", "passed": true, "details": "...", "simulation": true },
    "complianceGate": { "gateType": "COMPLIANCE", "passed": true, "details": "...", "simulation": true },
    "coordinatedGate": { "gateType": "COORDINATED", "passed": true, "details": "...", "simulation": true }
  },
  "testResult": {
    "testsRun": 6, "testsPassed": 5, "testsFailed": 1, "coveragePercent": 75.0
  },
  "complianceResult": {
    "overallStatus": "pass", "severity": "LOW"
  }
}
```

---

## 4. Step Sequence

The `SimulatedSprintService` orchestrates the flow **synchronously** (no async polling for MVP):

```
Step 1: BA Agent
  - Call LLMAgentClient.execute("BA_REQUIREMENTS", { projectTitle, changeDescription })
  - Parse JSON response into requirements list
  - Audit log: AGENT_TRIGGER_BA_SIMULATED

Step 2: Architect Agent
  - Call LLMAgentClient.execute("ARCHITECT_DESIGN", { projectTitle, requirements })
  - Parse JSON response into architecture spec
  - Audit log: AGENT_TRIGGER_ARCHITECT_SIMULATED

Step 3: Developer Agent
  - Call repoWorkspaceService.prepareWorkspace(project, simulation=true)
  - Call LLMAgentClient.execute("DEV_IMPLEMENT", { projectTitle, requirements, screens, files })
  - Audit log: AGENT_TRIGGER_DEVELOPER_SIMULATED

Step 4: Tester Agent
  - Call LLMAgentClient.execute("TEST_GENERATE", { projectTitle, acceptanceCriteria })
  - Parse response into TestResult entity
  - Save TestResult to test_results table
  - Audit log: AGENT_TRIGGER_TESTER_SIMULATED

Step 5: Compliance Agent
  - Call LLMAgentClient.execute("COMPLIANCE_CHECK", { projectTitle, requirements })
  - Parse response into ComplianceResult entity
  - Save ComplianceResult to compliance_results table
  - Audit log: AGENT_TRIGGER_COMPLIANCE_SIMULATED

Step 6: Gate Evaluation
  - QualityGateService.evaluateTestGate(projectId, correlationId, testResult, simulation=true, branch)
  - QualityGateService.evaluateComplianceGate(projectId, correlationId, complianceResult, simulation=true, branch)
  - QualityGateService.evaluateCoordinatedGate(projectId, correlationId, testGate, complianceGate, simulation=true, branch)
  - All audit logs use _SIMULATED suffix

Return summary with gateStatus, testResult, complianceResult
```

---

## 5. Failure Behavior

When the system is configured to use the real `DeepSeekAgentClient` (non-stub mode), the following resilience patterns apply:

### Circuit Breaker Fast-Fail
- If the DeepSeek API is unreachable or returning errors, `DeepSeekAgentClient` may open its circuit breaker after 3 consecutive failures
- When the circuit breaker is **OPEN**, all LLM agent calls fast-fail with error JSON: `{"circuitBreakerOpen": true}`
- `SimulatedSprintService.parseOrFallback()` catches this error JSON gracefully and returns empty DTOs (empty requirement lists, zero test runs, etc.)
- This means the sprint flow **does not crash** — it completes with empty/safe results rather than throwing an exception
- After a 30-second cooldown, a single probe call is allowed; a successful probe resets the breaker

### Git Clone Retries
- When `simulation=false` and the repo needs cloning, `RepoWorkspaceService` retries up to 3 times with a 120-second per-attempt timeout
- On timeout or non-zero exit code, partial clone artifacts are cleaned up before the next attempt
- A 5-second fixed backoff separates retry attempts
- If all 3 attempts fail, `RepoWorkspaceException` is thrown with the attempt count in the message (e.g., `"Clone failed for project 1 after 3 attempt(s)"`)
- In **simulation mode** (`simulation=true`), all git operations are skipped — no retries, no timeouts

---

## 7. Services & Topics Used

| Service | Topics | Simulation |
|---------|--------|------------|
| `SimulatedSprintService` | _none (direct calls)_ | All steps |
| `LLMAgentClient` (stub) | — | Stub responses |
| `RepoWorkspaceService.prepareWorkspace()` | — | `simulation=true` skips git |
| `TestResultRepository.save()` | — | `simulation=true` on entity |
| `ComplianceResultRepository.save()` | — | `simulation=true` on entity |
| `QualityGateService` | — | `_SIMULATED` audit suffixes |
| `AuditLogService` | — | `simulation=true` on audit log |

---

## 8. Frontend Integration


The "Quality & Agents" tab (in `project_detail_screen.dart`) is updated to:

1. **Add "Run Simulated Sprint" button** at the top of the tab
   - Calls `POST /api/projects/{id}/simulated-sprint` with `{ branch, changeDescription }`
   - Shows loading state during execution
   - On completion, refreshes gate status automatically

2. **Show latest TestResult and ComplianceResult** under the gate cards
   - Fetch `GET /api/projects/{id}/branches/{branch}/test-results` (latest)
   - Fetch `GET /api/projects/{id}/branches/{branch}/compliance-results` (latest)
   - Display as compact info cards with pass/fail indicators

---

## 9. Backend REST Endpoints


| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/projects/{id}/simulated-sprint` | Run the full simulated sprint |
| `GET` | `/api/projects/{id}/branches/{branch}/test-results/latest` | Latest test result |
| `GET` | `/api/projects/{id}/branches/{branch}/compliance-results/latest` | Latest compliance result |

---

## 10. Implementation Checklist


1. Create `docs/hero_flow_mvp.md`
2. Add `TestResultRepository.findTopByProjectIdAndBranch` (exists) and `ComplianceResultRepository.findTopByProjectIdAndBranch` (exists)
3. Create `SimulatedSprintService` with orchestration logic
4. Create `SimulatedSprintRequest/Response` DTOs
5. Add `POST /api/projects/{id}/simulated-sprint` endpoint
6. Add `GET /api/projects/{id}/branches/{branch}/test-results/latest` and compliance equivalent
7. Add "Run Simulated Sprint" button to frontend
8. Add TestResult/ComplianceResult display to frontend
9. Create unit tests for `SimulatedSprintService`

[← Back to README](README.md) | Related: [agent_orchestration_design.md](agent_orchestration_design.md), [quality_gates_and_simulation_design.md](quality_gates_and_simulation_design.md), [repo_aware_dev_agent_design.md](repo_aware_dev_agent_design.md)
