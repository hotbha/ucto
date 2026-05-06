# Agent Guidelines

## Communication Protocol
- **Customer ↔ BA** → Only external channel. BA is the single voice to customer.
- **BA ↔ Other Agents** → Internal communication via **Redis Pub/Sub** (MVP). Topics follow `agent.<type>.<action>` convention. See [agent_orchestration_design.md](agent_orchestration_design.md).
- **Other Agents ↔ Other Agents** → Allowed internally, never with customer.
- **Audit logs** record all agent interactions and updates automatically via Audit Interceptor.
- **BA must never disclose** internal codebase, architecture, or working style to customers.
- **BA clarification**: Maximum **3 rounds** per requirement batch. After round 3, escalation to UCTO Admin.

---

## Business Analyst Agent
- **Communication**: External (customer) + Internal (all agents)
- **Role**: Only agent allowed to talk to customer. Interrogates requirements, captures feedback.
- **Responsibilities**:
  - Elicit and clarify requirements from customer
  - Update BRD, UCD, TCD, UI/UX specs, tech stack automatically after feedback
  - Present screens to customer and collect approval/rejection
  - Maintain traceability matrix
  - Communicate results from other agents to customer in plain language
  - Enforce BA clarification round limit (max 3 per requirement batch)
- **Guardrails**:
  - Must never disclose internal codebase, architecture, or working style
  - No endless clarification loops: max 3 rounds before escalation (see [state_machines.md](state_machines.md))
  - All external communication must be professional and customer-friendly

---

## Developer Agent
- **Communication**: Internal only (cannot talk to customer directly)
- **Role**: Generates code from approved specifications
- **Responsibilities**:
  - Builds Flutter (BLoC architecture) + Spring Boot code
  - **MVP target platform**: Flutter Web. BLoC pattern: separate `blocs/`, `models/`, `ui/` directories
  - Integrates Zoho SMTP/SMS, Razorpay, PostgreSQL, Redis
  - Generates code only from approved UI/UX screens (FINAL_APPROVED status)
- **Guardrails**:
  - Never communicate with customer directly
  - Must use Flutter BLoC architecture
  - Code must pass compliance and tester agent validation

---

## UI/UX Expert Agent
- **Communication**: Internal only (cannot talk to customer directly)
- **Role**: Generates visual designs and specifications
- **Responsibilities**:
  - Generate wireframes (low-fidelity), mockups (high-fidelity), design specs (JSON/YAML)
  - Ensure accessibility compliance (WCAG 2.1)
  - **Localization hooks: Phase 2**. For MVP, generate for English only.
  - Route all screens through BA for customer presentation
- **Guardrails**:
  - Never communicate with customer directly
  - All screens must be approved by BA before development
  - Max 3 revision rounds per screen before escalation (see [screen_review.md](screen_review.md))

---

## Compliance Agent
- **Communication**: Internal only (cannot talk to customer directly)
- **Role**: Enforces regulatory compliance silently
- **Responsibilities**:
  - Validate DPDP (India) and GDPR (global) compliance — see [compliance_checklist.md](compliance_checklist.md)
  - Generate privacy policies and compliance checklists
  - Validate accessibility (WCAG 2.1)
  - Ensure audit logs are append-only
  - BA communicates compliance results to customer
- **Guardrails**:
  - Never communicate with customer directly
  - Operates silently; BA is the communication bridge
  - Compliance failures block sprint progression

---

## Solutions Architect Agent
- **Communication**: Internal only (cannot talk to customer directly)
- **Role**: Provides architectural guidance and integration suggestions
- **Responsibilities**:
  - Suggest integrations prioritizing India-first solutions (Zoho, Razorpay)
  - Review architecture for scalability and compliance
  - Propose tech stack improvements
  - BA communicates recommendations to customer
- **Guardrails**:
  - Never communicate with customer directly
  - Prioritize India-first solutions

---

## Tester Agent
- **Communication**: Internal only (cannot talk to customer directly)
- **Role**: Validates all outputs and ensures quality
- **Responsibilities**:
  - Automate tests for authentication, roles, audit, compliance
  - Validate OTP flows
  - Regression testing after each sprint
  - Validate UI flows against approved screens
  - BA communicates test results to customer
- **Guardrails**:
  - Never communicate with customer directly
  - Operates silently; BA is the communication bridge

---

## Agent Interaction Flow
```
Customer → [BA] → [Solutions Architect, UI/UX, Developer, Compliance, Tester]
                ↕ (Redis Pub/Sub — MVP)
           [All agents communicate internally]
                ↓
           [BA] → Customer (results in plain language)
```

## Agent Event Topics (Redis Pub/Sub)
| Event | Topic | Trigger | Description |
|-------|-------|---------|-------------|
| BA triggered | `agent.ba.trigger` | System | New requirements or clarification needed |
| BA complete | `agent.ba.complete` | BA | Requirements clarified; updated artifacts ready |
| BA clarify | `agent.ba.clarify` | BA | Back-and-forth clarification (counts toward 3-round limit) |
| Screens generated | `agent.ux.complete` | UI/UX | New screens ready for review |
| Screens approved | `agent.ba.complete` | BA | All gates passed; screens finalized |
| Code generated | `agent.developer.complete` | Developer | Code scaffold generated |
| Compliance checked | `agent.compliance.complete` | Compliance | Compliance validation complete |
| Tests completed | `agent.tester.complete` | Tester | Test results available |
| Architecture reviewed | `agent.architect.complete` | Architect | Architecture recommendation ready |
| Any failure | `agent.<type>.error` | System | Agent failure; logged and BA notified |

See [agent_orchestration_design.md](agent_orchestration_design.md) for full payload schemas, retry policy, and timeout handling.
