# Quality Gates & Simulation Design

> **Purpose:** Define how tester and compliance agents act as quality gates before merges or deployments, introduce a simulation mode flag, and store test/compliance results in the data model.
> **Status:** Draft | **Last Updated:** 2026-05-13

---

## 1. Quality Gate Model

### 1.1 Gate Evaluation Pipeline

Before any code change can be merged or deployed, it must pass through quality gates:

```
Developer agent creates branch + commits
         ↓
  agent.developer.complete
         ↓
  agent.tester.trigger     ← Quality Gate 1: Tests
         ↓
  agent.compliance.trigger ← Quality Gate 2: Compliance
         ↓
Both gates pass?
  YES → agent.pm.trigger (PM reviews; can approve merge)
  NO  → agent.developer.trigger (fix issues; loop back)
```

### 1.2 Gate Evaluation Rules

A gate is evaluated based on:

- **Test Gate**: All tests pass with coverage ≥ configured threshold.
- **Compliance Gate**: All compliance checks pass with no open high‑severity findings.
- **Coordinated Gate**: Both gates must pass. If either fails, the pipeline publishes an error event back to the developer agent.

| Gate | Condition | Fail Behavior |
|------|-----------|---------------|
| Test | `testsPassed == testsRun` AND `coverage >= threshold` | Publish `agent.developer.trigger` with `action: "fix_tests"` |
| Compliance | `overallStatus == "pass"` | Publish `agent.compliance.error` with findings details |
| Combined | Both gates pass | Publish `agent.pm.trigger` with `action: "review_merge"` |

---

## 2. Data Model for Test Results

### 2.1 New Entity: `TestResult`

```java
@Entity
@Table(name = "test_results")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long agentRunId; // Links to the AgentRun that produced these tests

    private String storyId; // Backlog item tested

    @Column(nullable = false)
    private int testsRun;

    @Column(nullable = false)
    private int testsPassed;

    @Column(nullable = false)
    private int testsFailed;

    private int testsSkipped;

    @Column(nullable = false)
    private double coveragePercent; // 0.0 – 100.0

    @Column(columnDefinition = "TEXT")
    private String failuresJson; // JSON array of failed test details

    @Column(nullable = false)
    private String status; // PASSED, FAILED, INCOMPLETE, ERROR

    @Column(nullable = false)
    private String correlationId;

    private LocalDateTime createdAt;
    private LocalDateTime evaluatedAt;
}
```

### 2.2 New Entity: `ComplianceResult`

```java
@Entity
@Table(name = "compliance_results")
public class ComplianceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long agentRunId;

    @Column(columnDefinition = "TEXT")
    private String checksPassedJson; // JSON array of passed check names

    @Column(columnDefinition = "TEXT")
    private String checksFailedJson; // JSON array of failed check details

    @Column(nullable = false)
    private String overallStatus; // PASS, PASS_WITH_WARNINGS, FAIL

    @Column(nullable = false)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    private String reportUrl;

    @Column(nullable = false)
    private String correlationId;

    private LocalDateTime createdAt;
    private LocalDateTime evaluatedAt;
}
```

### 2.3 New Entity: `GateEvaluation`

Records each gate evaluation for audit and traceability.

```java
@Entity
@Table(name = "gate_evaluations")
public class GateEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String gateType; // TEST, COMPLIANCE, COORDINATED

    @Column(nullable = false)
    private boolean passed;

    private Long testResultId; // FK to test_results (nullable for compliance-only gates)
    private Long complianceResultId; // FK to compliance_results

    @Column(nullable = false)
    private String correlationId;

    @Column(columnDefinition = "TEXT")
    private String details; // Human-readable summary of the evaluation

    private boolean simulation; // Was this a simulated run?

    private LocalDateTime evaluatedAt;
}
```

---

## 3. Simulation Mode

### 3.1 What Simulation Mode Means

A **boolean `simulation` flag** is added to agent events. When `simulation = true`:

| Allowed Actions | Forbidden Actions |
|----------------|-------------------|
| Analysis and planning | Real Git pushes / commits |
| Code generation (files written to temp workspace) | PR creation / merge |
| Test execution (in isolated sandbox) | External API calls (Chargebee, SMTP) |
| Compliance checks (read‑only analysis) | Side effects on production data |
| Audit logging (with `simulation = true` marker) | Repository writes (clone is read‑only) |
| Publish simulation‑complete events | Deployment triggers |

### 3.2 Event Payload with Simulation Flag

The `simulation` flag is part of the top-level event envelope:

```json
{
  "eventId": "evt_sim_001",
  "eventType": "agent.developer.trigger",
  "projectId": "1",
  "simulation": true,
  "data": {
    "action": "implement_changes",
    "repoConfig": { ... },
    "requirementIds": ["req_1"]
  }
}
```

### 3.3 Propagation Through Pipeline

Once a trigger event has `simulation = true`, all downstream events in that pipeline **must also carry** `simulation = true`. The `AgentEventListener.createNextEvent()` method propagates the flag:

```java
private Map<String, Object> createNextEvent(Map<String, Object> currentPayload,
                                              String nextAgentType,
                                              String projectId,
                                              Map<String, Object> data) {
    Map<String, Object> nextEvent = new HashMap<>();
    // ... existing fields ...
    nextEvent.put("simulation", currentPayload.getOrDefault("simulation", false));
    return nextEvent;
}
```

### 3.4 Enforcement in Services

Each service that performs side-effect operations checks the simulation flag:

```java
// In RepoWorkspaceService
if (Boolean.TRUE.equals(simulation)) {
    log.info("SIMULATION MODE: Skipping real git push for project {}", projectId);
    auditLogService.log(userId, projectId, "REPO_PUSH_SIMULATED",
        "Simulated push skipped", ipAddress, true);
    return;
}
```

### 3.5 Use Cases for Simulation Mode

| Use Case | Description |
|----------|-------------|
| Dry‑run a feature | Test planning, code generation, and evaluation without side effects |
| Compliance pre‑check | Run compliance analysis before committing real code changes |
| Training / demo | Show the agent pipeline workflow without creating real artifacts |
| Debugging | Isolate a failing gate without redundant real operations |

---

## 4. Audit Logging for Simulated vs Real Runs

The `AuditLog` entity is extended with an optional `simulation` boolean column:

```java
@Column(nullable = false)
private boolean simulation;
```

All simulated audit entries are logged with action suffixes:

| Operation | Real Action | Simulated Action |
|-----------|-------------|------------------|
| Git push | `REPO_PUSH` | `REPO_PUSH_SIMULATED` |
| PR create | `REPO_PR_CREATE` | `REPO_PR_CREATE_SIMULATED` |
| Test evaluation | `GATE_TEST_EVALUATE` | `GATE_TEST_EVALUATE_SIMULATED` |
| Compliance evaluation | `GATE_COMPLIANCE_EVALUATE` | `GATE_COMPLIANCE_EVALUATE_SIMULATED` |
| Agent trigger | `AGENT_TRIGGER_DEVELOPER` | `AGENT_TRIGGER_DEVELOPER_SIMULATED` |

---

## 5. Gate Service

A new `QualityGateService` coordinates gate evaluation:

```java
@Service
public class QualityGateService {

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private ComplianceResultRepository complianceResultRepository;

    @Autowired
    private GateEvaluationRepository gateEvaluationRepository;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Coverage threshold configuration.
     * Default: 80%. Configurable via ucto.gates.coverage-threshold.
     */
    private double coverageThreshold = 80.0;

    /**
     * Evaluate test gate. Returns true if tests pass with sufficient coverage.
     */
    public GateEvaluation evaluateTestGate(Long projectId, String correlationId,
                                            TestResult result, boolean simulation) {
        boolean passed = result.getTestsFailed() == 0
                      && result.getCoveragePercent() >= coverageThreshold;

        GateEvaluation eval = new GateEvaluation();
        eval.setProjectId(projectId);
        eval.setGateType("TEST");
        eval.setPassed(passed);
        eval.setTestResultId(result.getId());
        eval.setCorrelationId(correlationId);
        eval.setSimulation(simulation);
        eval.setDetails(String.format(
            "Tests: %d/%d passed, coverage %.1f%% (threshold %.1f%%)",
            result.getTestsPassed(), result.getTestsRun(),
            result.getCoveragePercent(), coverageThreshold));

        gateEvaluationRepository.save(eval);

        String action = simulation ? "GATE_TEST_EVALUATE_SIMULATED" : "GATE_TEST_EVALUATE";
        auditLogService.logAuthAction(null, action, eval.getDetails(), "", passed);

        return eval;
    }

    /**
     * Evaluate compliance gate. Returns true if overall status is PASS.
     */
    public GateEvaluation evaluateComplianceGate(Long projectId, String correlationId,
                                                  ComplianceResult result, boolean simulation) {
        boolean passed = "PASS".equals(result.getOverallStatus());

        GateEvaluation eval = new GateEvaluation();
        eval.setProjectId(projectId);
        eval.setGateType("COMPLIANCE");
        eval.setPassed(passed);
        eval.setComplianceResultId(result.getId());
        eval.setCorrelationId(correlationId);
        eval.setSimulation(simulation);
        eval.setDetails(String.format(
            "Compliance: %s, severity %s",
            result.getOverallStatus(), result.getSeverity()));

        gateEvaluationRepository.save(eval);

        String action = simulation ? "GATE_COMPLIANCE_EVALUATE_SIMULATED" : "GATE_COMPLIANCE_EVALUATE";
        auditLogService.logAuthAction(null, action, eval.getDetails(), "", passed);

        return eval;
    }

    /**
     * Evaluate coordinated gate (both test + compliance must pass).
     */
    public boolean evaluateCoordinatedGate(Long projectId, String correlationId,
                                            GateEvaluation testEval,
                                            GateEvaluation complianceEval,
                                            boolean simulation) {
        boolean bothPassed = testEval.isPassed() && complianceEval.isPassed();

        GateEvaluation coord = new GateEvaluation();
        coord.setProjectId(projectId);
        coord.setGateType("COORDINATED");
        coord.setPassed(bothPassed);
        coord.setCorrelationId(correlationId);
        coord.setSimulation(simulation);
        coord.setDetails(String.format(
            "Coordinated gate: tests=%s, compliance=%s → overall=%s",
            testEval.isPassed() ? "PASS" : "FAIL",
            complianceEval.isPassed() ? "PASS" : "FAIL",
            bothPassed ? "PASS" : "FAIL"));

        gateEvaluationRepository.save(coord);

        String action = simulation ? "GATE_COORDINATED_EVALUATE_SIMULATED" : "GATE_COORDINATED_EVALUATE";
        auditLogService.logAuthAction(null, action, coord.getDetails(), "", bothPassed);

        return bothPassed;
    }
}
```

---

## 6. Integration with AgentEventListener

The `AgentEventListener` is extended to invoke `QualityGateService`:

```java
// In handleAgentComplete(), after receiving tester.complete:
if (topic.equals("agent.tester.complete")) {
    TestResult testResult = parseTestResult(payload);
    GateEvaluation testGate = qualityGateService.evaluateTestGate(
        projectId, correlationId, testResult, isSimulation(payload));

    if (!testGate.isPassed()) {
        // Route back to developer for fixes
        publishEvent("agent.developer.trigger", createFixEvent(payload, "fix_tests"));
        return;
    }
    // Continue to compliance
    publishEvent("agent.compliance.trigger", createNextEvent(payload, "compliance", projectId, getData(payload)));
}

// After receiving compliance.complete:
if (topic.equals("agent.compliance.complete")) {
    ComplianceResult complianceResult = parseComplianceResult(payload);
    GateEvaluation complianceGate = qualityGateService.evaluateComplianceGate(
        projectId, correlationId, complianceResult, isSimulation(payload));

    if (!complianceGate.isPassed()) {
        publishEvent("agent.developer.trigger", createFixEvent(payload, "fix_compliance"));
        return;
    }
    // Both gates passed
    publishEvent("agent.pm.trigger", createNextEvent(payload, "pm", projectId, getData(payload)));
}
```

---

## 7. Configuration Flags

```properties
# Coverage threshold for test gate (percentage)
ucto.gates.coverage-threshold=80

# Enable/disable automatic gate evaluation
ucto.gates.enabled=true

# Enable/disable simulation mode
ucto.gates.simulation-mode=false

# If true, PM is notified even if gates pass (for manual approval)
ucto.gates.require-pm-approval=true

# Gate evaluation actions to skip in simulation
ucto.gates.simulation-skip-actions=git_push,pr_create,pr_merge,deploy
```

---

## 8. Implementation Checklist

1. Create `TestResult`, `ComplianceResult`, and `GateEvaluation` JPA entities with repositories.
2. Create `QualityGateService` with evaluate methods and audit logging.
3. Extend `AgentEventListener` to invoke `QualityGateService` after `tester.complete` and `compliance.complete`.
4. Add `simulation` boolean column to `AuditLog` entity.
5. Propagate `simulation` flag in `createNextEvent()`.
6. Add simulation guard checks in `RepoWorkspaceService` and any other side-effect services.
7. Add `simulation-skip-actions` configuration.
8. Add database migration scripts for the three new tables.
9. Add unit tests for `QualityGateService` and integration tests for gate routing.

[← Back to README](README.md) | Related: [agent_orchestration_design.md](agent_orchestration_design.md), [repo_aware_dev_agent_design.md](repo_aware_dev_agent_design.md)
