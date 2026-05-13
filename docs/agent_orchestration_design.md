# Agent Orchestration Design

## Purpose
Define the implementation mechanism, event flow, and operational policies for UCTO's AI agent communication system.

## MVP Orchestration Model
For MVP, agent orchestration uses **Redis Pub/Sub** as the message broker. This choice leverages the existing Redis dependency (already used for cache/sessions) and avoids adding RabbitMQ or a separate message queue service.

**Why Redis Pub/Sub for MVP:**
- Redis already in the stack — zero additional infrastructure
- Pub/Sub semantics map cleanly to agent event patterns
- Sufficient throughput for MVP scale (dozens of runs/day)
- Simple topic-based routing matches agent naming conventions

**When to migrate to RabbitMQ (Phase 2+):**
- Persistent message delivery (Redis Pub/Sub does not guarantee delivery)
- Consumer groups for horizontal scaling
- Dead-letter queues for failed messages
- Message ordering guarantees

## Topic Naming Convention

```
agent.<agent_type>.<action>
```

| Agent Type | Prefix |
|------------|--------|
| Business Analyst | `ba` |
| Developer | `developer` |
| Tester | `tester` |
| Compliance | `compliance` |
| UI/UX Expert | `ux` |
| Solutions Architect | `architect` |

| Action | Suffix |
|--------|--------|
| Triggered | `trigger` |
| Completed | `complete` |
| Failed | `error` |
| Clarification needed | `clarify` |

**Examples:**
- `agent.ba.trigger` — BA agent triggered
- `agent.ba.complete` — BA task completed
- `agent.developer.trigger` — Developer agent triggered
- `agent.tester.complete` — Tester agent finished
- `agent.compliance.error` — Compliance check failed
- `agent.ux.clarify` — UI/UX needs clarification

## Event Flow Per Agent

### Business Analyst
```
agent.ba.trigger
  → BA processes requirement clarification
  → agent.ba.complete (with updated requirements)
  → agent.developer.trigger (if screens approved)
  → agent.ux.trigger (if requirements clarified)
```

### Developer
```
agent.developer.trigger
  → Developer generates code from approved screens
  → agent.developer.complete (with code artifacts)
  → agent.tester.trigger
```

### Tester
```
agent.tester.trigger
  → Tester runs validation suite
  → agent.tester.complete (with test results)
  → agent.compliance.trigger (if tests pass)
```

### Compliance
```
agent.compliance.trigger
  → Compliance checks DPDP/GDPR rules
  → agent.compliance.complete (with compliance report)
```

### UI/UX Expert
```
agent.ux.trigger
  → UI/UX generates wireframes/mockups
  → agent.ux.complete (with screen artifacts)
  → agent.ba.complete (BA presents to customer)
```

### Solutions Architect
```
agent.architect.trigger
  → Architect reviews integration/architecture
  → agent.architect.complete (with recommendations)
```

## Event Payload Schema

### Base Event Envelope
```json
{
  "eventId": "evt_abc123",
  "eventType": "agent.ba.complete",
  "projectId": "proj_xyz",
  "agentId": "agent_ba_01",
  "timestamp": "2026-05-05T10:00:00Z",
  "correlationId": "sprint_17_req_3",
  "data": { }
}
```

### Agent-Specific Payloads

**BA Complete:**
```json
{
  "eventId": "evt_ba_001",
  "eventType": "agent.ba.complete",
  "projectId": "proj_xyz",
  "data": {
    "requirementIds": ["req_1", "req_2"],
    "status": "clarified",
    "summary": "Requirements clarified after 2 rounds",
    "nextAction": "screen_generation"
  }
}
```

**Developer Complete:**
```json
{
  "eventId": "evt_dev_001",
  "eventType": "agent.developer.complete",
  "projectId": "proj_xyz",
  "data": {
    "screenIds": ["scr_1", "scr_2"],
    "codeArtifacts": ["lib/screens/dashboard.dart"],
    "status": "generated",
    "testCoverage": 85
  }
}
```

**Compliance Complete:**
```json
{
  "eventId": "evt_comp_001",
  "eventType": "agent.compliance.complete",
  "projectId": "proj_xyz",
  "data": {
    "checksPassed": ["dpdp_consent", "gdpr_right_to_delete"],
    "checksFailed": [],
    "overallStatus": "pass",
    "reportUrl": "/reports/compliance/proj_xyz/001"
  }
}
```

## Retry Policy

| Scenario | Retries | Backoff | Notes |
|----------|---------|---------|-------|
| Agent timeout | 3 | Exponential (1s, 4s, 16s) | Each retry increments AgentRun counter |
| Network error | 3 | Immediate × 1, then exponential (2s, 8s) | Only if Redis connection is intermittent |
| Compliance failure | 0 (fail fast) | N/A | Must be resolved before proceeding |
| Agent returns error | 0 (fail fast) | N/A | Logged; BA notified for manual intervention |

## Timeout Handling

| Agent | Timeout | Behavior |
|-------|---------|----------|
| BA | 30s | Log timeout; prompt BA to retry |
| Developer | 120s | Log timeout; code escrows for manual review |
| Tester | 60s | Log timeout; mark tests as incomplete |
| Compliance | 30s | Log timeout; mark compliance as unchecked |
| UI/UX | 120s | Log timeout; use fallback templates |
| Solutions Architect | 30s | Log timeout; skip architect recommendations |

## Idempotency Notes
- Each `eventId` should be processed exactly once
- Redis stores processed event IDs for 24 hours
- Duplicate events with same `eventId` are silently dropped
- AgentRun counter increments only on first processing

## Audit Interception Pattern
Every event published to any agent topic is **automatically logged** by an AuditInterceptor service:
1. Subscribe to `agent.*` (wildcard)
2. Copy event payload to `audit_logs` table
3. Add metadata: `userId`, `projectId`, `ipAddress` (from original trigger)

This ensures **every agent interaction is audited without agents writing audit code.**

## Failure Handling

| Failure Mode | Detection | Recovery |
|-------------|-----------|----------|
| Agent not responding | Timeout after max retries | Mark sprint step as blocked; notify BA |
| Invalid event payload | Schema validation fails | Log malformed event; do not publish to downstream agents |
| Redis down | Connection error | Queue events in memory (degraded mode); retry on reconnect |
| Agent returns error data | Status field = "error" | Log error; BA notified via dashboard |

## Future Migration Note
When scaling beyond MVP, replace Redis Pub/Sub with **RabbitMQ** for:
- Persistent delivery guarantees (survive broker restart)
- Consumer groups for parallel agent instances
- Dead-letter exchanges for failed event analysis
- Delivery acknowledgments and retry queues

The topic naming convention (`agent.<type>.<action>`) maps directly to RabbitMQ routing keys, making migration a configuration change rather than a code rewrite.

## Next‑Gen Features

Three companion documents define the next evolution of the agent orchestration system. Each is designed to be implemented directly from its specification.

### Repo‑Aware Developer Agent
**Document:** [repo_aware_dev_agent_design.md](repo_aware_dev_agent_design.md)

Extends the developer agent to: (a) link projects to Git repositories via new `repoUrl`, `repoProvider`, `repoBranch`, and `repoTokenRef` fields on the `Project` entity; (b) prepare local workspaces via clone/pull before processing events; (c) generate branches, commits, and PRs as concrete outputs of agent runs. Introduces `RepoWorkspaceService` and the `agent.developer.workspace_ready` / `agent.developer.workspace_error` topics. All repo operations are audited. Implements retry policy and timeouts for Git operations.

### Prompt‑to‑App Bootstrap
**Document:** [prompt_to_app_bootstrap_design.md](prompt_to_app_bootstrap_design.md)

Defines the end‑to‑end flow from a single user prompt → BA agent requirements → Architect agent design → Developer agent skeleton generation → project registration with linked repository. MVP supports the `spring-boot-react-postgres` stack. Reuses existing agent topics (`agent.ba.*`, `agent.architect.*`, `agent.developer.*`). Adds `SkeletonGeneratorService` and a `POST /api/projects/bootstrap` endpoint. Non‑goals for later phases include Next.js, Python backends, and mobile Flutter bootstrap.

### Quality Gates & Simulation Mode
**Document:** [quality_gates_and_simulation_design.md](quality_gates_and_simulation_design.md)

Introduces structured quality gates (test and compliance) that block merges unless all conditions are met. Defines new `TestResult`, `ComplianceResult`, and `GateEvaluation` JPA entities. Adds a boolean `simulation` flag to agent events that propagates through the pipeline — simulating analysis, planning, and evaluation while forbidding real Git pushes, PR merges, and external side effects. Simulated vs real runs are recorded separately in the audit log. New `QualityGateService` orchestrates gate evaluation with configurable coverage thresholds.

### Combined Impact on Topic Set
With all three features, the full agent topic set becomes:

```
agent.ba.{trigger,complete,error,clarify}
agent.developer.{trigger,complete,error,clarify,workspace_ready,workspace_error}
agent.tester.{trigger,complete,error}
agent.compliance.{trigger,complete,error}
agent.ux.{trigger,complete,error,clarify}
agent.architect.{trigger,complete,error}
agent.pm.{trigger,complete,error}
agent.documentation.{trigger,complete,error}
```

