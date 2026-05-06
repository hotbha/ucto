# Formal State Machines

## 1. Signup/Signin Flow

### States
```
INITIAL
SIGNUP_METHOD_SELECTED    (Google OAuth | Email+Password | OTP)
EMAIL_ENTERED
EMAIL_VERIFIED
PHONE_ENTERED
PHONE_VERIFIED
PASSWORD_CREATED
ACCOUNT_CREATED
CHARGEBEE_CUSTOMER_CREATED
JWT_ISSUED
AUTHENTICATED
AUTH_FAILED
SESSION_EXPIRED
```

### Transition Table
| From | To | Trigger | Actor |
|------|----|---------|-------|
| INITIAL | SIGNUP_METHOD_SELECTED | User selects method | User |
| SIGNUP_METHOD_SELECTED | EMAIL_ENTERED | User submits email | User |
| EMAIL_ENTERED | EMAIL_VERIFIED | User clicks verify link / enters OTP | System + User |
| SIGNUP_METHOD_SELECTED | EMAIL_ENTERED | User also needs email (phone-first path) | User |
| SIGNUP_METHOD_SELECTED | PHONE_ENTERED | User submits phone | User |
| PHONE_ENTERED | PHONE_VERIFIED | User enters OTP correctly | User |
| EMAIL_VERIFIED & PHONE_VERIFIED | PASSWORD_CREATED | User creates password | User |
| PASSWORD_CREATED | CHARGEBEE_CUSTOMER_CREATED | System creates Chargebee customer | System |
| CHARGEBEE_CUSTOMER_CREATED | JWT_ISSUED | System issues JWT | System |
| JWT_ISSUED | AUTHENTICATED | Client stores JWT | System |
| AUTHENTICATED | SESSION_EXPIRED | JWT expires (15 min) | System |
| SESSION_EXPIRED | JWT_ISSUED | Refresh token valid | System |
| Any | AUTH_FAILED | Validation error / OAuth error | System |

### Invalid Transitions
- INITIAL → AUTHENTICATED (skipping verification)
- EMAIL_ENTERED → PASSWORD_CREATED (email not verified)
- PHONE_ENTERED → PASSWORD_CREATED (phone not verified)

### Escalation Path
- AUTH_FAILED × 5 within 15 min → ACCOUNT_LOCKED (rate limit)
- ACCOUNT_LOCKED → requires password reset or customer support

---

## 2. Auth/Session Flow

### States
```
LOGGED_OUT
JWT_OBTAINED
SESSION_ACTIVE
TOKEN_REFRESHING
SESSION_EXPIRED
ACCOUNT_LOCKED
```

### Transition Table
| From | To | Trigger | Actor |
|------|----|---------|-------|
| LOGGED_OUT | JWT_OBTAINED | Login success | System |
| JWT_OBTAINED | SESSION_ACTIVE | Client stores JWT | Client |
| SESSION_ACTIVE | TOKEN_REFRESHING | Access token expired, refresh available | System |
| TOKEN_REFRESHING | SESSION_ACTIVE | New access token issued | System |
| TOKEN_REFRESHING | SESSION_EXPIRED | Refresh token expired | System |
| SESSION_ACTIVE | SESSION_EXPIRED | Both tokens expired | System |
| SESSION_EXPIRED | LOGGED_OUT | User clicks logout | User |
| Any | ACCOUNT_LOCKED | Rate limit exceeded / too many failures | System |
| ACCOUNT_LOCKED | LOGGED_OUT | Customer support intervention | Admin |

### Invalid Transitions
- LOGGED_OUT → SESSION_ACTIVE (must obtain JWT first)
- SESSION_ACTIVE → LOGGED_OUT (must expire first for security)

---

## 3. BA Clarification Loop

### States
```
REQUIREMENTS_DRAFT
AWAITING_CLARIFICATION
CLARIFICATION_IN_PROGRESS
CLARIFICATION_COMPLETE
ESCALATED
REQUIREMENTS_FINALIZED
```

### Transition Table
| From | To | Trigger | Actor | Max Repetitions |
|------|----|---------|-------|-----------------|
| REQUIREMENTS_DRAFT | AWAITING_CLARIFICATION | BA has questions | BA | — |
| AWAITING_CLARIFICATION | CLARIFICATION_IN_PROGRESS | Customer responds | Customer | — |
| CLARIFICATION_IN_PROGRESS | AWAITING_CLARIFICATION | BA needs more info | BA | Max 3 total rounds |
| CLARIFICATION_IN_PROGRESS | CLARIFICATION_COMPLETE | BA satisfied | BA | — |
| CLARIFICATION_IN_PROGRESS | ESCALATED | Max 3 rounds reached | System | — |
| CLARIFICATION_COMPLETE | REQUIREMENTS_FINALIZED | BA finalizes | BA | — |
| AWAITING_CLARIFICATION | REQUIREMENTS_FINALIZED | Customer says "no more questions" | Customer | — |
| ESCALATED | REQUIREMENTS_DRAFT | Product owner intervenes | Admin | — |
| ESCALATED | REQUIREMENTS_FINALIZED | Customer accepts as-is | Customer + Admin | — |

### Invalid Transitions
- REQUIREMENTS_DRAFT → REQUIREMENTS_FINALIZED (BA must review)
- CLARIFICATION_IN_PROGRESS → CLARIFICATION_COMPLETE without CLEARANCE

### Clarification Round Counting
- Round counter increments each time BA publishes to `agent.ba.clarify`
- Counter resets when requirement batch moves to REQUIREMENTS_FINALIZED
- Escalation sends notification to UCTO Admin / product owner

---

## 4. Screen Approval Lifecycle

### States
```
DRAFT
SCREENS_GENERATED
SUBMITTED_FOR_REVIEW
IN_REVIEW
APPROVED
REJECTED
CHANGES_REQUESTED
FINAL_APPROVED
```

### Transition Table
| From | To | Trigger | Actor |
|------|----|---------|-------|
| DRAFT | SCREENS_GENERATED | UI/UX completes generation | System |
| SCREENS_GENERATED | SUBMITTED_FOR_REVIEW | BA submits screens to customer | BA |
| SUBMITTED_FOR_REVIEW | IN_REVIEW | Customer opens screen review | Customer |
| IN_REVIEW | APPROVED | Customer approves screen | Customer (per screen) |
| IN_REVIEW | REJECTED | Customer rejects screen | Customer (per screen) |
| IN_REVIEW | CHANGES_REQUESTED | Customer requests changes | Customer (per screen) |
| CHANGES_REQUESTED | SCREENS_GENERATED | UI/UX regenerates | System |
| APPROVED (all screens) | FINAL_APPROVED | All screens in batch approved + Compliance + Tester passed | System |
| FINAL_APPROVED | (terminal) | Development can begin | — |

### Approval Rules
- **Per-screen granularity**: Customer can approve, reject, or request changes on individual screens
- **Batch-level gate**: Development starts only when ALL screens in a batch reach FINAL_APPROVED
- **Voting authority**: Customer (Founder role) only; BA facilitates but does not approve
- **Max revision rounds**: 3 rounds of CHANGES_REQUESTED → SCREENS_GENERATED per screen
  - After 3rd round, screen auto-escalates to UCTO Admin
- **Time limit**: Customer has 7 calendar days to review. After 7 days, BA may auto-approve or escalate.

### Percentage-Based Gate
- If ≥80% of screens in a batch are FINAL_APPROVED and <20% are REJECTED, the batch can proceed
- Rejected screens are re-generated and submitted as a follow-up mini-batch

### Invalid Transitions
- DRAFT → APPROVED (screens must be generated and submitted first)
- SCREENS_GENERATED → IN_REVIEW (BA must submit)
- Any → FINAL_APPROVED without Compliance and Tester passing

---

## 5. Sprint Lifecycle

### States
```
NOT_STARTED
SPRINT_PLANNING
DESIGN_SPRINT
ARCHITECTURE_REVIEW
COMPLIANCE_CHECK
DEVELOPMENT_SPRINT
TESTING_SPRINT
SPRINT_REVIEW
RETROSPECTIVE
SPRINT_CLOSED
SPRINT_BLOCKED
```

### Transition Table
| From | To | Trigger | Actor |
|------|----|---------|-------|
| NOT_STARTED | SPRINT_PLANNING | Sprint scheduled | System |
| SPRINT_PLANNING | DESIGN_SPRINT | Backlog defined | BA |
| SPRINT_PLANNING | SPRINT_BLOCKED | Requirements unclear after 3 BA rounds | System |
| DESIGN_SPRINT | ARCHITECTURE_REVIEW | Screens finalized (FINAL_APPROVED) | System |
| DESIGN_SPRINT | SPRINT_BLOCKED | Screens rejected ×2 without resolution | System |
| ARCHITECTURE_REVIEW | COMPLIANCE_CHECK | Architect recommendations delivered | Architect |
| COMPLIANCE_CHECK | DEVELOPMENT_SPRINT | Compliance passed | System |
| COMPLIANCE_CHECK | SPRINT_BLOCKED | Compliance critical failure | System |
| DEVELOPMENT_SPRINT | TESTING_SPRINT | Code generation complete, tests compile | Developer |
| DEVELOPMENT_SPRINT | SPRINT_BLOCKED | Code fails to compile after 3 attempts | System |
| TESTING_SPRINT | SPRINT_REVIEW | All tests pass | Tester |
| TESTING_SPRINT | SPRINT_BLOCKED | Critical test failures | System |
| SPRINT_REVIEW | RETROSPECTIVE | Customer demo completed | BA |
| SPRINT_REVIEW | SPRINT_BLOCKED | Customer rejects entire sprint | Customer |
| RETROSPECTIVE | SPRINT_CLOSED | Retro completed, action items logged | BA |
| SPRINT_BLOCKED | SPRINT_PLANNING (or appropriate state) | Blocking issue resolved | Admin |
| SPRINT_BLOCKED | SPRINT_CLOSED | Sprint abandoned | Admin |

### Invalid Transitions
- NOT_STARTED → DEVELOPMENT_SPRINT (skipping design, review, compliance)
- DESIGN_SPRINT → COMPLIANCE_CHECK (without architecture review)
- DEVELOPMENT_SPRINT → SPRINT_REVIEW (without testing)

### Escalation Path
- Any SPRINT_BLOCKED state sends notification to:
  1. BA (first escalation)
  2. UCTO Admin (if unresolved after 24h)
  3. Product Owner (if unresolved after 48h)
- Sprint auto-abandons after 7 days in SPRINT_BLOCKED state

---

## Legend

| Symbol | Meaning |
|--------|---------|
| (terminal) | End state; no outgoing transitions |
| System | Automated transition by the orchestration service |
| BA | Business Analyst agent |
| Customer | Product owner / founder persona |
| Admin | UCTO platform administrator |
| Developer | Developer agent |
| Tester | Tester agent |
