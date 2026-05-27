# Requirements Traceability Matrix (RTM)

| Requirement | Linked Document | Agent Responsible | Test Case ID | Last Validated By | Agent Runs Used | Status |
|------------|----------------|-------------------|-------------|-------------------|----------------|--------|
| Unified Signup/Signin | BRD, UCD | Developer | TC-AUTH-01, TC-AUTH-02, TC-AUTH-03 | — | — | Approved |
| Role-based Access | BRD, UCD | Business Analyst | TC-RBAC-01 | — | — | Approved |
| Audit Logs | BRD, TCD | Tester | TC-AUD-01, TC-AUD-02 | — | — | In Review |
| Compliance Enforcement (DPDP/GDPR) | BRD, TCD | Compliance Agent | TC-COMP-01 | — | — | Approved |
| UI/UX Design | UCD | UI/UX Expert | TC-UI-01 | — | — | Approved |
| Architectural Guidance | UCD | Solutions Architect | TC-ARCH-01 | — | — | Pending |
| CLI Extension | UCD | Developer + Architect | TC-CLI-01 | — | — | Pending |
| Usage Metering & Tier Enforcement | BRD, deployment_readiness_plan.md | Developer | TC-METER-01 | — | — | Pending |
| Project Member Management | BRD | Developer | TC-PROJ-01 | — | — | Approved |
| Screen Approval Workflow | BRD, screen_review.md, state_machines.md | UI/UX Expert + Compliance + Tester | TC-SCREEN-01, TC-SCREEN-02 | — | — | Pending |

## Test Case ID Reference

| Test Case ID | Description | Linked RTM Row | Test Class |
|-------------|-------------|---------------|------------|
| TC-AUTH-01 | Validate JWT issuance and refresh | Unified Signup/Signin | AuthControllerTest |
| TC-AUTH-02 | Google OAuth login flow | Unified Signup/Signin | AuthControllerTest |
| TC-AUTH-03 | Email + Password registration and login | Unified Signup/Signin | AuthControllerTest |
| TC-AUTH-04 | OTP validation for password reset | Unified Signup/Signin | AuthControllerTest |
| TC-RBAC-01 | Verify UCTO Admin vs Customer (Founder/Developer/Viewer) roles | Role-based Access | SecurityEdgeCaseTest |
| TC-AUD-01 | Success case audit record created | Audit Logs | AuditLogServiceTest |
| TC-AUD-02 | Failed attempt audit record created | Audit Logs | AuditLogServiceTest |
| TC-COMP-01 | DPDP/GDPR automated checks pass | Compliance Enforcement | GlobalExceptionHandlerTest |
| TC-UI-01 | Screen generation and approval workflow | UI/UX Design | ScreenControllerTest |
| TC-ARCH-01 | Solutions Architect integration suggestions | Architectural Guidance | — |
| TC-CLI-01 | CLI commands (init, sprint, agent, deploy) execute and return | CLI Extension | — |
| TC-METER-01 | Usage counter increments and tier enforcement at limit | Usage Metering | UsageMeterServiceTest, SubscriptionServiceTest |
| TC-PROJ-01 | Project CRUD with member management | Project Management | ProjectControllerTest, ProjectServiceTest |
| TC-SCREEN-01 | Screen approval lifecycle (approve/reject/changes) | Screen Approval Workflow | ScreenControllerTest |
| TC-SCREEN-02 | Screen revision limit (3 rounds → escalation) | Screen Approval Workflow | ScreenControllerTest |
| TC-BA-01 | BA receives PO message and responds | BA Chat | BAChatControllerTest |
| TC-BA-02 | Max 3 clarification rounds enforced | BA Chat | BAChatServiceTest |
| TC-BA-03 | Escalation when max rounds reached | BA Chat | BAChatServiceTest |
| TC-BOOT-01 | Prompt-to-app skeleton created | Bootstrap | BootstrapServiceTest |
| TC-BOOT-02 | Unsupported stack returns error | Bootstrap | BootstrapServiceTest |
| TC-LLM-01 | DeepSeek circuit breaker (CLOSED/OPEN/HALF_OPEN) | LLM Integration | DeepSeekAgentClientTest |
| TC-LLM-02 | Stub LLM client fallback when no API key | LLM Integration | StubLLMAgentClientTest |
| TC-QG-01 | Coverage threshold enforced | Quality Gates | QualityGateServiceTest |
| TC-SPRINT-01 | 5-agent sprint simulation with DB | Simulated Sprint | SimulatedSprintServiceTest |
| TC-ORCH-01 | Agent message routing via Redis | Agent Orchestration | AgentOrchestrationServiceTest |
| TC-JWT-01 | JWT token generation and validation | JWT Authentication | JwtServiceTest |
| TC-HLTH-01 | Health endpoint returns 200 | Health Check | HealthControllerTest |
| TC-REQ-01 | Requirement CRUD with clarification rounds | Requirements CRUD | RequirementControllerTest |

## Notes
- BA ensures all updates propagate automatically
- Audit logs capture every change for compliance
- "Agent Runs Used" column tracks cumulative agent runs per requirement (requires implementation)
- "Last Validated By" tracks which agent last processed this requirement
- "Actual Test Class" column links to real test classes in `backend/src/test/java/com/ucto/backend/`

## Test Execution Summary
- **Total Backend Tests**: 211 (all passing)
- **Total Frontend Tests**: 9 test files (all passing)
- **Coverage Tooling**:
  - Backend: JaCoCo (v0.8.12) configured in `pom.xml`
  - Frontend: Flutter `--coverage` flag
- **Ops Scripts**: See `ops/` folder for run/test scripts

## Quick Test Commands
```bash
# Backend tests with coverage
ops/run_backend_tests.bat          # Windows
# Frontend tests with coverage
ops/run_frontend_tests.bat         # Windows
# All tests
ops/run_all_tests.bat              # Windows
```

[← Back to README](README.md) | Related: [agent_guidelines.md](agent_guidelines.md), [ucto_playbook.md](ucto_playbook.md), [test_cases.md](test_cases.md), [testing_plan_and_status.md](testing_plan_and_status.md)
