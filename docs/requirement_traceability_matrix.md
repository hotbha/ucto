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

| Test Case ID | Description | Linked RTM Row |
|-------------|-------------|---------------|
| TC-AUTH-01 | Validate JWT issuance and refresh | Unified Signup/Signin |
| TC-AUTH-02 | Google OAuth login flow | Unified Signup/Signin |
| TC-AUTH-03 | Email + Password registration and login | Unified Signup/Signin |
| TC-AUTH-04 | OTP validation for password reset | Unified Signup/Signin |
| TC-RBAC-01 | Verify UCTO Admin vs Customer (Founder/Developer/Viewer) roles | Role-based Access |
| TC-AUD-01 | Success case audit record created | Audit Logs |
| TC-AUD-02 | Failed attempt audit record created | Audit Logs |
| TC-COMP-01 | DPDP/GDPR automated checks pass | Compliance Enforcement |
| TC-UI-01 | Screen generation and approval workflow | UI/UX Design |
| TC-ARCH-01 | Solutions Architect integration suggestions | Architectural Guidance |
| TC-CLI-01 | CLI commands (init, sprint, agent, deploy) execute and return | CLI Extension |
| TC-METER-01 | Usage counter increments and tier enforcement at limit | Usage Metering |
| TC-PROJ-01 | Project CRUD with member management | Project Management |
| TC-SCREEN-01 | Screen approval lifecycle (approve/reject/changes) | Screen Approval Workflow |
| TC-SCREEN-02 | Screen revision limit (3 rounds → escalation) | Screen Approval Workflow |

## Notes
- BA ensures all updates propagate automatically
- Audit logs capture every change for compliance
- "Agent Runs Used" column tracks cumulative agent runs per requirement (requires implementation)
- "Last Validated By" tracks which agent last processed this requirement

[← Back to README](README.md) | Related: [agent_guidelines.md](agent_guidelines.md), [ucto_playbook.md](ucto_playbook.md), [test_cases.md](test_cases.md)
