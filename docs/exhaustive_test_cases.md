# UCTO MVP — Exhaustive Test Cases

## Document Purpose
Organize all automated and manual test cases for the UCTO MVP by module.
Test IDs are prefixed by module code: AUTH, RBAC, PROJ, REQ, AGNT, REDIS, USAGE, SUB, SCRN, AUDIT, ERR, FLUT, PERF, SEC, DEPLOY.

---

## 1. Authentication

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| AUTH-01 | Auth | Register with email/password | No user exists | POST /api/auth/register with email, password, name | 201, user created, token returned | Duplicate email → 400 | Yes | P0 |
| AUTH-02 | Auth | Login with email/password | User exists with password | POST /api/auth/login with email, password | 200, tokens returned | Wrong password → 401 | Yes | P0 |
| AUTH-03 | Auth | Login with Google OAuth | Valid Google ID token | POST /api/auth/oauth with google token | 200, tokens returned, user created if new | Invalid token → 401 | Yes | P0 |
| AUTH-04 | Auth | Refresh token | Valid refresh token | POST /api/auth/refresh with refresh token | 200, new access token | Expired refresh → 401 | Yes | P0 |
| AUTH-05 | Auth | Request password reset | User exists with email | POST /api/auth/forgot-password with email | 200, reset email sent | Unknown email → 200 (no leak) | Yes | P1 |
| AUTH-06 | Auth | Reset password | Valid reset token | POST /api/auth/reset-password with token, new password | 200, password updated | Invalid/expired token → 400 | Yes | P1 |
| AUTH-07 | Auth | Request OTP | User exists with phone | POST /api/auth/otp/request with phone | 200, OTP sent | Unknown phone → 200 | No | P2 |
| AUTH-08 | Auth | Verify OTP | Valid OTP exists | POST /api/auth/otp/verify with phone, otp | 200, tokens returned | Wrong OTP → 400 | No | P2 |

## 2. Authorization / RBAC

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| RBAC-01 | Authz | Access protected route without token | No authentication | GET /api/projects with no Authorization header | 401 | — | Yes | P0 |
| RBAC-02 | Authz | Access protected route with expired token | Expired JWT | GET /api/projects with expired Bearer token | 401 | — | Yes | P0 |
| RBAC-03 | Authz | Access health without auth | — | GET /api/health | 200 | — | Yes | P0 |
| RBAC-04 | Authz | Founder can create project | Logged in as FOUNDER | POST /api/projects | 201 | — | Yes | P0 |
| RBAC-05 | Authz | Viewer cannot create project | Logged in as VIEWER | POST /api/projects | 403 | — | Yes | P1 |
| RBAC-06 | Authz | Founder can delete own project | Project owned by Founder | DELETE /api/projects/{id} | 200 | Not owner → 403 | Yes | P0 |
| RBAC-07 | Authz | Non-member cannot access project | Not a member | GET /api/projects/{id}/screens | 403 | — | Yes | P1 |

## 3. Project Management

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| PROJ-01 | Proj | Create project | Authenticated user | POST /api/projects with title, description | 201, project returned, owner added as FOUNDER | Missing title → 400 | Yes | P0 |
| PROJ-02 | Proj | List own projects | User has projects | GET /api/projects | 200, project list | — | Yes | P0 |
| PROJ-03 | Proj | Get project by ID | Project exists | GET /api/projects/{id} | 200, project | Not found → 404 | Yes | P0 |
| PROJ-04 | Proj | Update project | Owner | PUT /api/projects/{id} with title/status | 200, updated project | Non-owner → 403 | Yes | P0 |
| PROJ-05 | Proj | Delete project | Owner exists | DELETE /api/projects/{id} | 200, members also deleted | Non-owner → 403 | Yes | P0 |
| PROJ-06 | Proj | Add project member | Owner | POST /api/projects/{id}/members with userId, role | 201, member added | Duplicate member → 400 | Yes | P0 |
| PROJ-07 | Proj | List project members | Project exists | GET /api/projects/{id}/members | 200, member list | — | Yes | P0 |

## 4. Requirements Workflow

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| REQ-01 | Req | Create requirement | Project exists | POST /api/requirements with projectId, title, description | 201, status=DRAFT | Missing project → 400 | Yes | P0 |
| REQ-02 | Req | List project requirements | Requirements exist | GET /api/requirements/project/{projectId} | 200, requirement list | — | Yes | P0 |
| REQ-03 | Req | Update requirement | Owner/Founder | PUT /api/requirements/{id} with new title/description | 200, updated | Not owner → 403 | Yes | P0 |
| REQ-04 | Req | BA clarification round increments | 1st clarification request | PUT /api/requirements/{id} with clarificationRound+1 | clarificationRound increments | Max 3 rounds → 400 | Yes | P0 |
| REQ-05 | Req | Clarification cap at 3 rounds | clarificationRound = 3 | Post response to requirement with changes requested | 400, "Maximum clarification rounds reached" | — | Yes | P0 |
| REQ-06 | Req | Status transition DRAFT→CLARIFIED | User submits requirement | PUT status to CLARIFIED | 200, status changed | Invalid transition → 400 | Yes | P1 |

## 5. Agent Orchestration

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| AGNT-01 | Agent | Trigger BA agent | User has available runs | POST /api/agents/trigger with type=ba, projectId | 200, eventId returned | — | Yes | P0 |
| AGNT-02 | Agent | Trigger developer agent | User has available runs | POST /api/agents/trigger with type=developer | 200, eventId returned | — | Yes | P0 |
| AGNT-03 | Agent | Trigger when usage exhausted | User has 0 remaining runs | POST /api/agents/trigger | 402, AGENT_RUN_LIMIT_EXCEEDED | — | Yes | P0 |
| AGNT-04 | Agent | Agent run persisted on trigger | Valid trigger | Check AgentRun table after trigger | 1 agent run recorded | — | Yes | P1 |
| AGNT-05 | Agent | Redis event published on trigger | Valid trigger | Verify Redis convertAndSend called | Event published to agent.{type}.trigger | — | Yes | P1 |

## 6. Redis Messaging

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| REDIS-01 | Redis | Redis message listener registered | App startup | Check container has listener | AgentEventListener is registered | — | Yes | P1 |
| REDIS-02 | Redis | Agent event JSON schema valid | Trigger event | Capture published JSON | Contains eventId, eventType, timestamp, agentId | Malformed → N/A | Yes | P2 |

## 7. Usage Metering

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| USAGE-01 | Usage | Monthly run counter increments | User triggers agent | Check getMonthlyRuns before/after | +1 after trigger | — | Yes | P0 |
| USAGE-02 | Usage | Monthly counter resets next month | Runs recorded in previous month | getMonthlyRuns in new month | 0 runs | — | Yes | P1 |
| USAGE-03 | Usage | Project count increments | User creates project | Check getProjectCount before/after | +1 after create | — | Yes | P0 |
| USAGE-04 | Usage | Project deletion decrements count | Project exists | Create, delete, check count | 0 after delete | — | Yes | P1 |

## 8. Subscription Enforcement

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| SUB-01 | Sub | Free plan limits: 1 project, 5 runs | New user | Check getUsageStatus | tier=FREE, maxProjects=1, maxAgentRuns=5 | — | Yes | P0 |
| SUB-02 | Sub | Create subscription | No active sub | POST /api/subscriptions/upgrade with tier=STARTUP | 200, sub created, old cancelled | Invalid tier → 400 | Yes | P0 |
| SUB-03 | Sub | Activate 14-day startup trial | No trial used | POST /api/subscriptions/start-trial | 200, STARTUP_TRIAL created | Already used → ? | Yes | P0 |
| SUB-04 | Sub | Trial expiry auto-downgrades | Trial expired | Call getEffectivePlan after end date | Returns FREE plan | — | Yes | P1 |
| SUB-05 | Sub | Get subscription plans | — | GET /api/subscriptions/plans | 200, 4 plans returned | — | Yes | P0 |
| SUB-06 | Sub | Get my subscription status | User exists | GET /api/subscriptions/my | 200, usage status with runsRemaining | Not authenticated → 401 | Yes | P0 |
| SUB-07 | Sub | canRunAgent returns false when exhausted | 5/5 runs used | GET /api/subscriptions/can-run-agent | canRun=false, needsUpgrade=true | — | Yes | P0 |
| SUB-08 | Sub | Project creation blocked when limit hit | 1/1 project used | POST /api/projects | 402 or 400 with project limit message | — | Yes | P1 |

## 9. Screen Review State Machine

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| SCRN-01 | Screen | Create screen | Project exists | POST /api/screens with projectId, type | 201, status=PENDING | Missing type → 400 | Yes | P0 |
| SCRN-02 | Screen | List project screens | Screens exist | GET /api/screens/project/{projectId} | 200, screen list | — | Yes | P0 |
| SCRN-03 | Screen | Approve screen | status=PENDING | PUT /api/screens/{id}/status with status=APPROVED | 200, approvedBy set, approvedAt set | Non-PENDING → 400 | Yes | P0 |
| SCRN-04 | Screen | Reject screen | status=PENDING | PUT /api/screens/{id}/status with status=REJECTED | 200, revisionCount incremented | Non-PENDING → 400 | Yes | P0 |
| SCRN-05 | Screen | Request changes | status=PENDING | PUT /api/screens/{id}/status with status=CHANGES_REQUESTED | 200, revisionCount incremented | Non-PENDING → 400 | Yes | P0 |
| SCRN-06 | Screen | Revision count max 3 | PENDING, revisionCount=2 | PUT to CHANGES_REQUESTED | 400, max revision limit | — | Yes | P0 |
| SCRN-07 | Screen | Cannot approve after max revisions | REJECTED, revisionCount=3 | PUT to APPROVED | 400, max revision limit | — | Yes | P0 |
| SCRN-08 | Screen | Feedback stored on change request | PENDING | PUT with status=CHANGES_REQUESTED, feedback="needs work" | 200, feedback field set | — | Yes | P1 |
| SCRN-09 | Screen | Invalid state transition rejected | APPROVED | PUT to APPROVED again | 400, "Only PENDING screens can be approved" | — | Yes | P0 |
| SCRN-10 | Screen | Creator becomes approvedBy | APPROVED | PUT to APPROVED with auth | approvedBy = current userId | — | Yes | P1 |

## 10. Audit Logging

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| AUDIT-01 | Audit | Screen created logged | Screen has projectId | Create screen | Audit entry with SCREEN_CREATED | — | Yes | P1 |
| AUDIT-02 | Audit | Screen approved logged | Screen exists | Approve screen | Audit entry with SCREEN_APPROVED | — | Yes | P1 |
| AUDIT-03 | Audit | Agent trigger logged | User triggers agent | Trigger agent | Audit entry with AGENT_TRIGGER_BA | — | Yes | P1 |
| AUDIT-04 | Audit | Project created logged | User creates project | Create project | Audit entry with PROJECT_CREATED | — | Yes | P2 |

## 11. Error Handling

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| ERR-01 | Error | Malformed JSON body | — | POST /api/auth/login with bad JSON | 400, consistent error shape | — | Yes | P0 |
| ERR-02 | Error | Missing required field | — | POST /api/projects with empty body | 400, field error message | — | Yes | P0 |
| ERR-03 | Error | Resource not found | No resource | GET /api/projects/99999 | 404 or 400 | — | Yes | P0 |
| ERR-04 | Error | Internal server error | Unhandled exception | Trigger unhandled error path | 500, "Internal server error" | — | Yes | P1 |
| ERR-05 | Error | Error response shape consistent | Any error happens | Check all error responses | Shape: {"error": string} or {"error": string, "message": string, "code": string} | — | Yes | P1 |

## 12. Flutter UI and Navigation

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| FLUT-01 | Flutter | Login screen renders | App loaded | Navigate to / | Login form with email, password, Google button | — | Yes | P0 |
| FLUT-02 | Flutter | Login form validation | Empty form | Tap Login without filling fields | Validation errors shown | — | Yes | P0 |
| FLUT-03 | Flutter | Dashboard loads projects | Auth token present | Navigate to /dashboard | Project list or empty state | — | Yes | P0 |
| FLUT-04 | Flutter | Create project dialog | Dashboard loaded | Tap create, fill title, submit | Project appears in list | Empty title → error | Yes | P0 |
| FLUT-05 | Flutter | Unauthorized redirects to login | Expired token | Make API call that fails 401 | Redirect to /login | — | Yes | P0 |
| FLUT-06 | Flutter | Usage limit banner shown | Runs exhausted | Navigate to dashboard | "Usage limit reached" banner | — | Yes | P1 |
| FLUT-07 | Flutter | Screen review shows status badge | Screen exists | View screen detail | Status badge (PENDING/APPROVED/REJECTED) | — | Yes | P1 |
| FLUT-08 | Flutter | Loading indicator on API call | Slow API | Navigate to project detail | CircularProgressIndicator shown | — | Yes | P0 |

## 13. Performance / Resilience Edge Cases

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| PERF-01 | Perf | Concurrent agent triggers from same user | Usage near limit | Send 2 agent triggers simultaneously | One succeeds, one fails with limit | — | No | P2 |
| PERF-02 | Perf | Large project member list | 1000 members | GET /api/projects/{id}/members | 200, complete list | — | No | P2 |

## 14. Security Edge Cases

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| SEC-01 | Sec | SQL injection attempt | — | POST /api/auth/login with email=' OR 1=1-- | 401, not 200 | — | Yes | P0 |
| SEC-02 | Sec | XSS in project title | — | POST /api/projects with title=<script>alert(1)</script> | Stored safely, HTML escaped on render | — | No | P1 |
| SEC-03 | Sec | JWT tampering | Valid token | Modify JWT payload, resend | 401 | — | Yes | P0 |
| SEC-04 | Sec | IDOR: access another user's project | User A, Project B owned by User B | GET /api/projects/{B's project} | 403 | — | Yes | P1 |

## 15. Deployment Smoke Tests

| Test ID | Module | Scenario | Preconditions | Steps | Expected | Negative | Automated | Priority |
|---------|--------|----------|---------------|-------|----------|----------|-----------|----------|
| DEPLOY-01 | Deploy | Health endpoint returns 200 | App running | GET /api/health | 200, {"status":"UP"} | — | No | P0 |
| DEPLOY-02 | Deploy | Database migration runs | Fresh DB | Start app | Tables created, no errors | — | No | P0 |
| DEPLOY-03 | Deploy | Redis connection established | Redis running | Start app | Redis connection OK, no errors | — | No | P0 |
| DEPLOY-04 | Deploy | Flutter Web builds | Node/Flutter installed | flutter build web --release | Build succeeds | — | No | P0 |
| DEPLOY-05 | Deploy | Docker Compose starts all services | Docker installed | docker compose up -d | All containers healthy | — | No | P0 |
