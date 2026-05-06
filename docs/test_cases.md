# Test Case Document (TCD)

## Authentication
| ID | Test Case | Expected Result |
|----|-----------|-----------------|
| TC-AUTH-01 | Validate JWT issuance and refresh | Valid JWT returned on login; refresh token extends session |
| TC-AUTH-02 | Google OAuth login flow | User redirected to Google; callback creates/links account; JWT issued |
| TC-AUTH-03 | Email + Password registration and login | Account created; login returns JWT |
| TC-AUTH-04 | OTP validation for password reset | OTP sent; correct OTP allows password reset |

## Role-based Access
| ID | Test Case | Expected Result |
|----|-----------|-----------------|
| TC-RBAC-01 | Verify UCTO Admin vs Customer (Founder/Developer/Viewer) roles | Admin has full access; Founder can approve screens; Developer can view code; Viewer is read-only |

## Audit Logs
| ID | Test Case | Expected Result |
|----|-----------|-----------------|
| TC-AUD-01 | Success case audit record created | Successful login/screen approval/agent trigger creates audit log entry |
| TC-AUD-02 | Failed attempt audit record created | Failed login/screen rejection creates audit log entry with reason |

## Agent Orchestration
| ID | Test Case | Expected Result |
|----|-----------|-----------------|
| TC-AGENT-01 | Trigger + response validation | Agent trigger event published to Redis Pub/Sub; response event received and logged |

## Compliance
| ID | Test Case | Expected Result |
|----|-----------|-----------------|
| TC-COMP-01 | DPDP/GDPR checks | Automated checks pass; compliance report generated; failures block sprint |

## UI/UX Screen Workflow
| ID | Test Case | Expected Result |
|----|-----------|-----------------|
| TC-SCREEN-01 | Screen approval lifecycle (approve/reject/changes) | Screen status transitions correctly through all states |
| TC-SCREEN-02 | Screen revision limit (3 rounds → escalation) | After 3rd changes_requested, screen auto-escalates |

## Usage Metering
| ID | Test Case | Expected Result |
|----|-----------|-----------------|
| TC-METER-01 | Usage counter increments and tier enforcement at limit | Counter increments on agent trigger; at limit, triggers return 402 |

## Project Management
| ID | Test Case | Expected Result |
|----|-----------|-----------------|
| TC-PROJ-01 | Project CRUD with member management | Create, read, update, delete project; invite/remove members |

## VS Code Extension
| ID | Test Case | Expected Result |
|----|-----------|-----------------|
| TC-CLI-01 | Validate CLI commands (`ucto init`, `ucto sprint`, `ucto agent`, `ucto deploy`) | Each command executes and returns expected output |

## Linked RTM
See [requirement_traceability_matrix.md](requirement_traceability_matrix.md) for requirement-to-test-case mapping.
