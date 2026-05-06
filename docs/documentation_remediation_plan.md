# Documentation Remediation Plan

> Generated: 2026-05-05
> Status: DRAFT — For review prior to implementation

---

## 1. Executive Summary

The UCTO documentation suite (17 Markdown files in `/docs`) contains foundational architectural vision but suffers from **10+ categories of contradictions, ambiguities, and gaps** that block reliable implementation by both human developers and AI coding agents.

**Root causes:**
- Frontend technology identity crisis (Flutter vs React) infects almost every doc
- MVP scope vs future vision is not systematically separated
- Agent orchestration is described at the communication-protocol level but never at the implementation-mechanism level
- Multiple documents contain duplicate/conflicting sections
- Existing `analysis_and_plan.md` identified many issues but was never reconciled into the source docs
- The actual repo state has diverged significantly from the docs

**This plan identifies:**
- 26+ specific contradictions/ambiguities
- 10 repo-inferred decisions that should be codified
- 5 open decisions requiring a product owner choice
- 11 files to edit
- 6 new files to create
- A 20-item consistency checklist

---

## 2. Current Problems — Complete Inventory

### 2.1 CRITICAL: Frontend/Backend Stack Inconsistencies

| # | Problem | Docs Say | Repo Has | Impact |
|---|---------|----------|----------|--------|
| 1 | **Frontend identity crisis** | "Flutter + BLoC" in README, system_architecture.md, agent_guidelines.md, ucto_playbook.md | `frontend/lib/` has legit Flutter BLoC code; `frontend/package.json` has React 19 + Vite + TS | **Blocking.** Agents cannot know which framework to build for |
| 2 | **Java version mismatch** | Not explicitly stated in most docs; system_architecture.md implies Java 17 | `pom.xml` uses Spring Boot 4.0.6 / Java 25 | Runtime compatibility risk |
| 3 | **React orphan artifacts** | Nowhere in docs | `package.json`, `vite.config.ts`, `tsconfig*.json`, `index.html` in `/web` | New devs may think React is the target |
| 4 | **Flutter platform target unclear** | README says "Mobile + Web" | `pubspec.yaml` has `web/` dir and `flutter_facebook_auth` (mobile plugin) | Screen architecture ambiguous |
| 5 | **Missing Spring Boot version pin** | Implicit in system_architecture.md | `pom.xml` uses 4.0.6 (via parent) but docs reference "3.x" architecture | Version-sensitive features may break |

### 2.2 MVP vs Future Scope Ambiguity

| # | Problem | Evidence |
|---|---------|----------|
| 6 | **Agent orchestration = MVP or future?** | deployment_readiness_plan.md says Agent Orchestration is P1 (Day 6-8) yet it's the core product value prop |
| 7 | **Mobile app vs web portal** | README says "Flutter (Mobile + Web)" but deployment_readiness_plan.md says "Build portal in React; Flutter for mobile app later." Docs contradict each other. |
| 8 | **Chargebee: MVP or Phase 2?** | BRD says Required; pom.xml includes chargebee-java; but deployment_readiness_plan.md says "Mock Chargebee for MVP" |
| 9 | **Facebook OAuth: MVP or future?** | BRD says "Google, Facebook, OTP, Email+Password fallback" but deployment_readiness_plan.md only mentions Google OAuth |
| 10 | **Analytics tools unspecified** | Prometheus, Grafana, Mixpanel, Amplitude, MoEngage, Hevo — which is MVP? costs.md lists ALL |
| 11 | **Localization: MVP or future?** | next_refinements.md lists it as future; screen_review.md says "Include localization hooks" as agent responsibility |

### 2.3 Role & Tenancy Confusion

| # | Problem | Evidence |
|---|---------|----------|
| 12 | **Customer sub-roles not implemented** | BRD specifies Founder, Developer, Viewer; User entity has `role` field but only UCTO_ADMIN and CUSTOMER values |
| 13 | **Enterprise roles not modeled** | Product Owner, Finance, Compliance mentioned in BRD but no entity, no DB schema |
| 14 | **Project membership unclear** | ProjectMember entity exists but relationship to tier limits (Free=1 project, etc.) not enforced |

### 2.4 Agent Orchestration Ambiguity

| # | Problem | Evidence |
|---|---------|----------|
| 15 | **No message broker implementation** | Docs say "event-driven message broker (Redis Pub/Sub or RabbitMQ)" — still "or" with no decision |
| 16 | **AgentRun entity exists but no orchestration service** | `AgentRun.java` entity exists; `AgentOrchestrationService.java` does not exist |
| 17 | **"BA is the bridge" — no actual implementation pattern** | Agent communication protocol described at abstraction level; no concrete API/event flow |
| 18 | **Screen generation AI service unspecified** | "Generative AI to create wireframes/mockups" — no service, no API, no prompt template |

### 2.5 Missing Workflow State Machines

| # | Problem | Evidence |
|---|---------|----------|
| 19 | **No signup/signin state machine** | BRD describes flows verbally; no AS-IS flow diagram or FSM |
| 20 | **No screen-approval state machine** | Gate 1, Gate 2, Gate 3 defined but no transition rules, rollback, or rejection paths |
| 21 | **No sprint lifecycle state machine** | Playbook lists 8 ceremonies but does not define valid state transitions per sprint |
| 22 | **No BA clarification loop termination** | "No endless loops" stated but no hard limit or escalation path defined |

### 2.6 Screen Approval Ambiguity

| # | Problem | Evidence |
|---|---------|----------|
| 23 | **Approval voting rules undefined** | Who approves? Customer only? BA + Customer? Quorum rules? |
| 24 | **Rejection granularity undefined** | Can customer reject 1 screen out of 10? Or whole batch? |
| 25 | **Edit/revision cycle limit undefined** | How many revision rounds before escalation? |

### 2.7 Traceability Model Gaps

| # | Problem | Evidence |
|---|---------|----------|
| 26 | **RTM status values inconsistent** | `requirement_traceability_matrix.md` shows Approved, In Review, Pending — no definitions for these values |
| 27 | **RTM no link to test cases** | No TestCase ID column in RTM; tests in test_cases.md not linked to requirements |
| 28 | **RTM missing AgentRun tracking** | No "last validated by agent" or "last run timestamp" |
| 29 | **Duplicate RTM header** | File starts with `# Requirements Traceability Matrix (RTM)` repeated twice |

### 2.8 Billing / "Agent Run" Definition Gaps

| # | Problem | Evidence |
|---|---------|----------|
| 30 | **"Agent run" not formally defined** | Is it one API call? One sprint? One requirement processed by all 6 agents? |
| 31 | **Cost per agent run not calculable** | costs.md gives monthly aggregates; cannot derive per-unit cost |
| 32 | **No usage metering service** | deployment_readiness_plan.md lists it as P0 missing; no UsageMeterService or TierEnforcementFilter exists |
| 33 | **Tier limit enforcement undefined** | Free=5 runs/month — what happens at run #6? Graceful degradation? Hard block? |

### 2.9 Compliance Wording Risk

| # | Problem | Evidence |
|---|---------|----------|
| 34 | **"Immutable audit logs" claimed but not implemented** | AuditLog entity exists; no immutability enforcement (append-only, cryptographic chaining) |
| 35 | **DPDP/GDPR compliance checks mentioned but undefined** | What checks? Automated or manual? Checklist items unspecified |
| 36 | **DigiLocker "optional KYC" mentioned but no integration pattern** | No OAuth flow, no DigiLocker API integration defined |

### 2.10 Infrastructure Decisions Undecided

| # | Problem | Evidence |
|---|---------|----------|
| 37 | **PostgreSQL vs MySQL confusion spillover** | pom.xml has ONLY postgresql driver; but costs.md mentions both ("PostgreSQL + Redis") |
| 38 | **Redis vs RabbitMQ for agent queue** | "Redis Pub/Sub or RabbitMQ" — still undecided |
| 39 | **Object storage provider undecided** | "Backblaze/Wasabi/S3" — all mentioned with slashes |
| 40 | **CDN provider undecided** | "Cloudflare/NGINX/Traefik" — it's unclear if CDN is separate from load balancer |
| 41 | **GPU inference provider undecided** | "AWS/GCP/Azure" — 3 providers listed; costs.md bundles them as one line item |
| 42 | **SMTP provider: Zoho or no?** | docs say Zoho SMTP; pom.xml has no Zoho/JavaMail dependency |

### 2.11 Duplicate Sections, Broken Links, Terminology

| # | Problem | Evidence |
|---|---------|----------|
| 43 | **Duplicate "Portal & CLI Design" in costs.md** | Bottom of costs.md has a full duplicate of portal_cli_design.md |
| 44 | **Broken reference to traceability_matrix.md** | ucto_playbook.md and screen_review.md link to `traceability_matrix.md` (singular) — actual file is `requirement_traceability_matrix.md` |
| 45 | **Inconsistent naming: "traceability_matrix" vs "requirement_traceability_matrix"** | Both link targets used across docs |
| 46 | **README.md mentions "Analysis Agent" — no such agent defined** | next_refinements.md mentions "Optional Analytics Agent" — not in 6-agent model |
| 47 | **"Solutions Architect" vs "Architect" used interchangeably** | agent_guidelines.md says "Solutions Architect Agent"; use_cases.md says "Solutions Architect Agent"; but system_architecture.md says "Architect" |

---

## 3. Repo-Inferred Decisions

These decisions can be **deduced from the actual implementation** and should be codified in the docs:

| Decision | Inferred Value | Evidence |
|----------|---------------|----------|
| **D1: Frontend framework** | **Flutter + BLoC (Dart)** | `frontend/lib/` has substantive Flutter BLoC code (blocs/, models/, ui/, services/) with ~12 Dart screens. React artifacts are abandoned experiments. |
| **D2: Java version** | **Java 25 (OpenJDK)** | `pom.xml` parent = Spring Boot 4.0.6, which requires Java 17+. The explicit `java.version` property is `25`. |
| **D3: Database** | **PostgreSQL** | `pom.xml` has ONLY `postgresql` driver. All entity code uses JPA annotations consistent with PostgreSQL. No MySQL references in code. |
| **D4: Authentication** | **JWT + OAuth2 (Google) + Email/Password** | `pom.xml` includes `spring-boot-starter-oauth2-client`. `User.java` entity has Google/Facebook ID fields. AuthController exists. |
| **D5: Entity scope** | **8 entities implemented** | User, Project, ProjectMember, Requirement, Screen, AuditLog, AgentRun, Subscription — all present in `entity/` directory. |
| **D6: Controller scope** | **8 controllers implemented** | AuthController, ProjectController, RequirementController, ScreenController, AuditLogController, SubscriptionController, EmailController, HealthController |
| **D7: Subscription engine** | **Chargebee** | `pom.xml` includes `chargebee-java:3.14.0`. Subscription entity exists. SubscriptionController exists. |
| **D8: Project structure** | **Standard Spring Boot 3-layer** | controller/ → service/ (inferred) → repository/ (inferred) pattern followed |
| **D9: Frontend BLoC structure** | **Auth, Project, Requirement, Subscription BLoCs** | 4 BLoC modules exist in `frontend/lib/blocs/` |
| **D10: VS Code Extension** | **TypeScript, commands registered** | `src/extension.ts` exists with `ucto init`, `ucto sprint`, `ucto agent`, `ucto deploy` commands registered |

---

## 4. Open Decisions (Requiring Product Owner Confirmation)

| ID | Decision | Options | Recommendation | Rationale |
|----|----------|---------|---------------|-----------|
| **OD1** | **Frontend: Flutter vs React — FINAL** | (a) Flutter (keep) | **(a) Flutter** | Repo has substantive Flutter code. React artifacts are minimal and likely exploratory. Switching to React would discard ~800+ lines of Dart. |
| | | (b) React (migrate) | | |
| | | (c) Both (Flutter for mobile, React for web portal) | | |
| **OD2** | **Agent message broker** | (a) Redis Pub/Sub | **(a) Redis Pub/Sub** | Redis already in stack for cache. Avoids adding RabbitMQ for MVP. Can migrate later. |
| | | (b) RabbitMQ | | |
| | | (c) Direct API calls (synchronous) | | |
| **OD3** | **"Agent run" definition** | (a) One complete sprint cycle | **(c) One API call to an agent** | Simplest to meter and bill. Aligns with common AI API billing models. |
| | | (b) One requirement processed by all agents | | |
| | | (c) One API call to an agent | | |
| **OD4** | **Mobile app priority** | (a) MVP: Web-only (Flutter web) | **(b) MVP: Flutter Web only** | Faster MVP. Mobile (iOS/Android) can be Phase 2 since the BLoC code is shared. |
| | | (b) MVP: Web only, mobile Phase 2 | | |
| | | (c) MVP: Web + Android simultaneously | | |
| **OD5** | **Facebook OAuth: MVP or Phase 2?** | (a) MVP: Include Facebook | **(b) Phase 2: Skip for MVP** | Reduces auth complexity. Google OAuth + Email/Password covers vast majority of users. Facebook OAuth has ongoing API stability issues. |
| | | (b) Phase 2: Defer Facebook | | |

---

## 5. Files To Update

| # | File | Changes Needed | Contradictions Fixed |
|---|------|---------------|---------------------|
| 1 | `docs/README.md` | Replace "Flutter + BLoC" with final decision (recommended: Flutter). Remove React references. Fix broken links. Add explicit Java 25 mention. Fix "Analysis Agent" reference. Add deprecation note for React artifacts. | 1, 2, 46 |
| 2 | `docs/system_architecture.md` | Replace stack table to match repo reality. Add explicit agent communication mechanism (recommended: Redis Pub/Sub). Remove MySQL references. Add Java 25. Remove Facebook OAuth from MVP. Add "agent run" definition. Remove "Architect" in favor of "Solutions Architect Agent". | 1, 2, 5, 15, 30, 37, 38, 47 |
| 3 | `docs/agent_guidelines.md` | Add "Flutter Web (MVP) + Mobile (Phase 2)" platform target. Add "Redis Pub/Sub" as agent communication mechanism. Remove "must include localization hooks" from MVP agent guidelines (move to next_refinements.md). | 1, 11, 15 |
| 4 | `docs/solutions_architect.md` | Remove Stripe/PayPal from MVP. Remove Facebook mention or mark deferred. Add Flutter Web as delivery platform. Add Redis Pub/Sub as broker decision. | 1, 9, 15, 38 |
| 5 | `docs/business_requirements.md` | Add explicit "For MVP:" labels to in-scope items. Mark Facebook OAuth as Phase 2. Define "agent run" explicitly. Add tier enforcement behavior at run limits. | 9, 30, 33 |
| 6 | `docs/use_cases.md` | Add UC08: Usage Metering & Tier Enforcement. Add UC09: Project Member Management. Add UC10: Screen Approval Workflow. | 33, 23 |
| 7 | `docs/requirement_traceability_matrix.md` | Fix duplicate header. Add TestCase ID column. Add "last validated by" column. Add "agent run count" column. Add usage metering row. Fix broken link names. | 26, 27, 28, 29, 44, 45 |
| 8 | `docs/screen_review.md` | Add approval quorum rules. Add rejection granularity rules (single-screen or batch). Add revision cycle limits (max 3 rounds). Add state machine for screen statuses. | 23, 24, 25 |
| 9 | `docs/ucto_playbook.md` | Add sprint state machine. Add "max clarification rounds" limit. Fix link to `requirement_traceability_matrix.md`. Add escalation path for stuck sprints. | 22, 44, 45 |
| 10 | `docs/deployment_readiness_plan.md` | Replace "React" with "Flutter Web" for portal frontend. Remove "Mock Chargebee" (pom.xml has chargebee dependency). Add Facebook OAuth deferral note. Update tech stack to match Flutter decision. | 1, 7, 8, 9 |
| 11 | `docs/costs.md` | Remove duplicate "Portal & CLI Design" section at bottom. Consolidate analytics: pick Mixpanel (leave others as future). Add cost-per-agent-run model. | 10, 43 |

---

## 6. New Files To Create

| # | File | Purpose | Priority |
|---|------|---------|----------|
| 1 | `docs/agent_orchestration_design.md` | Detailed design of agent communication: Redis Pub/Sub topics, event payload schemas, retry policies, timeout handling, error recovery, audit interception | P0 |
| 2 | `docs/state_machines.md` | Formal state machines for: Sprint lifecycle, Screen approval, BA clarification loop, Signup flow, Auth flow. Each with valid states, transitions, guards, error states. | P0 |
| 3 | `docs/usage_metering_design.md` | Design for usage metering and tier enforcement: "agent run" definition, counter increments, tier limit configuration, enforcement at run limits (graceful vs hard block), usage dashboard | P1 |
| 4 | `docs/glossary.md` | Single source of truth for all UCTO terminology: Agent Run, Sprint, Screen Gate, BA Clarification, Tier, Project, etc. Prevents terminology drift across 17 docs. | P1 |
| 5 | `docs/compliance_checklist.md` | Explicit DPDP (India) and GDPR checklist items: what is checked, how, by which agent, automated vs manual, frequency | P2 |
| 6 | `docs/decision_log.md` | Architecture Decision Record (ADR) log: track OD1-OD5 and future decisions with date, rationale, alternatives considered | P1 |

---

## 7. Detailed Change Plan (Section-by-Section)

### 7.1 `docs/README.md`

| Section | Change |
|---------|--------|
| "Technology Stack" | Replace "Flutter + BLoC Architecture (Dart)" with final decision. Add Java 25. Remove MySQL. Add "For MVP: Flutter Web, Phase 2: Flutter Mobile." |
| "Documentation Index" | Add new files (glossary.md, state_machines.md, agent_orchestration_design.md, usage_metering_design.md, compliance_checklist.md, decision_log.md) |
| "Agent Guardrails" | Fix "6 AI Agents" count: remove "Analytics Agent" reference. |
| "Key Principles" | Remove "India-first integrations → DigiLocker" from MVP (mark Phase 2). |
| "File Structure" | Add deprecation note: `frontend/package.json`, `vite.config.ts`, `tsconfig*.json` are orphan artifacts from React experiment. |
| All sections | Fix broken/placeholder content |

### 7.2 `docs/system_architecture.md`

| Section | Change |
|---------|--------|
| "Technology Stack" | Replace entire table: Frontend=Flutter + BLoC (Dart, Web MVP). Backend=Spring Boot 4.0.6 / Java 25. DB=PostgreSQL. |
| "Core Services > Agent Orchestration Service" | Replace "event-driven messaging" with "Redis Pub/Sub" for MVP. Add link to agent_orchestration_design.md. |
| "Integrations" | Remove Facebook OAuth from MVP. Mark DigiLocker as Phase 2. |
| "Deployment Layers > MVP" | Replace "Flutter Web (Nginx)" with explicit port/config. Add Java 25 in diagram. |
| "Performance" | Add explicit object storage decision: unified under S3 API (Backblaze/Wasabi accepted). |
| "Security" | Add JWT 15-min access token + refresh token pattern (codify from code). |

### 7.3 `docs/agent_guidelines.md`

| Section | Change |
|---------|--------|
| "Communication Protocol" | Add "Implementation: Redis Pub/Sub topics follow pattern `agent.<type>.<action>`." |
| "UI/UX Expert Agent > Responsibilities" | Replace "Include localization hooks" with "Include localization hooks (Phase 2 only for MVP)" |
| "Developer Agent > Responsibilities" | Add "Target platform: Flutter Web for MVP. BLoC pattern strictly enforced." |
| "Agent Interaction Flow" | Add topic names and event payload structure reference to agent_orchestration_design.md. |

### 7.4 `docs/business_requirements.md`

| Section | Change |
|--------|--------|
| "Core Modules > Unified Signup/Signin" | Add "(MVP: Google OAuth + Email/Password. Phase 2: Facebook OAuth)." |
| "Subscription Tiers and Limits" | Add definition of "agent run." Add explicit behavior at limit exhaustion. Add usage dashboard mention. |

### 7.5 `docs/requirement_traceability_matrix.md`

| Section | Change |
|---------|--------|
| Header | Fix duplicate `# Requirements Traceability Matrix (RTM)` — keep one |
| Table | Add columns: "Test Case ID", "Last Validated By", "Agent Runs Used" |
| Rows | Add missing requirements: Usage Metering, Project Management, Screen Approval, Tier Enforcement |
| Links | Fix `traceability_matrix.md` → `requirement_traceability_matrix.md` |

### 7.6 `docs/screen_review.md`

| Section | Change |
|---------|--------|
| "Approval Gates" | Add voting rules: "Gate 1: Customer approves/rejects per individual screen. Gate 2: Compliance auto-passes or flags. Gate 3: Tester validates against approved screens." |
| "Feedback Loop" | Add revision limit: "Maximum 3 revision rounds per screen batch. Escalate to BA after round 3." |
| New section | Add "Status State Machine": Draft → Submitted → InReview → Approved/Rejected → RevisionsRequested ↔ InReview → Final. |

### 7.7 `docs/ucto_playbook.md`

| Section | Change |
|---------|--------|
| "Agile Ceremonies" | Add state machine: NotStarted → Planning → Design → ArchitectureReview → ComplianceCheck → Development → Testing → Review → Retrospective → Done. Define valid transitions. |
| "Communication Protocol" | Add "BA max 3 clarification rounds per requirement. After 3rd round, BA must accept customer input or escalate." |
| Links | Fix `traceability_matrix.md` → `requirement_traceability_matrix.md` |

### 7.8 `docs/costs.md`

| Section | Change |
|---------|--------|
| Bottom | **Remove** the duplicate "Portal & CLI Design" section that appears after line 38 |
| "Monitoring & Analytics" | Consolidate: "MVP: Prometheus + Grafana (self-hosted). Phase 2: Mixpanel." |
| New section | Add "Per Agent Run Cost Estimate" with formula: `(LLM tokens per run × cost per token) + (API overhead × multiplier)` |

### 7.9 `docs/deployment_readiness_plan.md`

| Section | Change |
|---------|--------|
| "Tech Stack for Frontend" | Replace entire section: "Flutter Web (not React). BLoC architecture maintained from day 1." |
| "Backend Gaps" | Update statuses: JWT Auth = ❌Missing (not ✅Done — AuthController returns string). Mark Facebook OAuth = Phase 2. |
| "Conversion Mechanics" | Add usage limit behavior: "At run limit: Graceful degradation (read-only mode, data accessible, new agent runs blocked, upgrade CTA shown)." |
| "Implementation Gaps" | Add Flutter Web deployment gap. |

---

## 8. Priority Order

### Phase 1 — Foundation (must fix before any development can proceed reliably)

| Priority | Task | Files Affected | Effort |
|----------|------|---------------|--------|
| P0 | Resolve Flutter vs React decision (OD1) | README.md, system_architecture.md, deployment_readiness_plan.md | 1 decision |
| P0 | Codify Repo-Inferred Decisions (D1-D10) | All docs | 2h |
| P0 | Create agent_orchestration_design.md | New file | 3h |
| P0 | Create state_machines.md | New file | 3h |
| P0 | Fix duplicate RTM header + broken links | requirement_traceability_matrix.md, ucto_playbook.md, screen_review.md | 1h |
| P0 | Remove React orphan artifact confusion | README.md + deprecation note | 0.5h |

### Phase 2 — MVP Scope Clarification

| Priority | Task | Files Affected | Effort |
|----------|------|---------------|--------|
| P1 | Add "For MVP:" labels to business_requirements.md | business_requirements.md | 1h |
| P1 | Create glossary.md | New file | 2h |
| P1 | Create usage_metering_design.md | New file | 2h |
| P1 | Create decision_log.md (capture OD1-OD5) | New file | 1h |
| P1 | Fix duplicate costs.md section | costs.md | 0.5h |
| P1 | Fix agent guidelines communication mechanism | agent_guidelines.md | 1h |

### Phase 3 — Detail & Polish

| Priority | Task | Files Affected | Effort |
|----------|------|---------------|--------|
| P2 | Add screen approval state machine | screen_review.md | 2h |
| P2 | Add sprint state machine | ucto_playbook.md | 2h |
| P2 | Create compliance_checklist.md | New file | 2h |
| P2 | Update solutions_architect.md with final decisions | solutions_architect.md | 1h |
| P2 | Update system_architecture.md with Redis Pub/Sub decision | system_architecture.md | 1h |

---

## 9. Consistency Checklist

After all changes, validate cross-document consistency by checking:

- [ ] **1. Frontend framework**: All 17 docs agree on "Flutter + BLoC". No doc references React.
- [ ] **2. Java version**: All docs reference Java 25 (or "25+") consistently.
- [ ] **3. Database**: All docs say PostgreSQL. No MySQL references.
- [ ] **4. Agent count**: Exactly 6 agents: BA, Developer, Tester, Compliance, UI/UX Expert, Solutions Architect. No "Analytics Agent."
- [ ] **5. Agent communication mechanism**: Single mechanism (recommended: Redis Pub/Sub). No "or RabbitMQ."
- [ ] **6. MVP platform**: All docs agree on Flutter Web (not mobile, not React). Mobile deferred to Phase 2.
- [ ] **7. OAuth scope**: MVP = Google OAuth + Email/Password. Facebook OAuth = Phase 2.
- [ ] **8. "Agent run" definition**: Single consistent definition across BRD, costs.md, use_cases.md.
- [ ] **9. Screen approval rules**: screen_review.md, agent_guidelines.md, and playbook agree on voting, rejection granularity, and revision limits.
- [ ] **10. Sprint lifecycle**: ucto_playbook.md and state_machines.md have consistent states and transitions.
- [ ] **11. BA clarification limits**: agent_guidelines.md and ucto_playbook.md agree on max rounds (3).
- [ ] **12. RTM links**: All cross-doc links to `requirement_traceability_matrix.md` use the correct filename.
- [ ] **13. Subscription tier limits**: BRD, deployment_readiness_plan.md, and usage_metering_design.md agree on Free/Startup/Growth/Enterprise limits.
- [ ] **14. Compliance scope**: compliance_checklist.md defines exactly what DPDP/GDPR checks are automated vs manual.
- [ ] **15. CDN vs load balancer**: CDN (Cloudflare) and load balancer (NGINX/Traefik) are distinguished, not conflated.
- [ ] **16. Object storage**: Unified under S3-compatible API. No conflicting provider decisions.
- [ ] **17. SMTP provider**: Single provider named (excluding backup/fallback).
- [ ] **18. "Solutions Architect" naming**: No "Architect" shorthand. Full name used everywhere.
- [ ] **19. costs.md**: No duplicate sections. Single cost model per component.
- [ ] **20. Entity list**: backend entity list (8 entities) matches docs references.

---

## 10. Risks If Not Fixed

| Risk | Likelihood | Impact | Description |
|------|-----------|--------|-------------|
| **AI agent produces wrong framework code** | **Very High** | **High** | Agent reads "Flutter" in README but also finds `package.json` (React). Ambiguity causes it to generate React code that doesn't compile. |
| **Sprint deadlock with no resolution** | **Medium** | **High** | No BA clarification loop limit. Agent can loop indefinitely on requirements. No escalation path defined. |
| **Billing disputes with customers** | **Medium** | **High** | "Agent run" not formally defined. Customer uses 50 "runs" in one sprint. Cannot bill accurately. Tier enforcement triggers unexpectedly. |
| **Compliance audit failure** | **Low** | **Critical** | "Immutable audit logs" claimed but not implemented. DPDP/GDPR "compliance checks" mentioned but no checklist. Regulatory risk. |
| **Screen approval stalemate** | **Medium** | **Medium** | No rejection granularity rules. Customer rejects 1 of 10 screens. Unclear if whole batch must be regenerated. Delays sprints. |
| **Orchestration implementation paralysis** | **High** | **Medium** | Agent communication mechanism undecided (Redis vs RabbitMQ). Developer cannot implement orchestration service. Blocked on P0 feature. |
| **Lost traceability** | **Medium** | **Medium** | RTM not linked to test cases. Unable to prove which requirements are tested. Compliance risk. |
| **Duplicate work** | **Medium** | **Low** | Two developers read different docs, build different implementations (e.g., Flutter screens vs React pages). |
| **Tech debt from orphan React code** | **High** | **Low** | `package.json`, `vite.config.ts`, `tsconfig*.json` remain in repo. New devs waste time investigating whether React is needed. |
| **Cost overrun** | **Medium** | **Medium** | Per-agent-run cost not modeled. LLM API costs could exceed subscription revenue without visibility. |

---

## Appendix A: Doc Dependency Graph

```
README.md (index, single source of truth)
├── business_requirements.md (BRD — what we build)
├── use_cases.md (UCD — how users interact)
├── system_architecture.md (SA — how it works)
├── solutions_architect.md (integration choices)
├── agent_guidelines.md (agent responsibilities)
├── screen_review.md (screen workflow)
├── ucto_playbook.md (sprint ceremonies)
├── requirement_traceability_matrix.md (RTM — cross-reference)
├── test_cases.md (TCD — test scenarios)
├── portal_cli_design.md (portal + extension)
├── deployment_readiness_plan.md (go-to-market)
├── costs.md (financial model)
├── next_refinements.md (Phase 2+ items)
├── analysis_and_plan.md (existing gap analysis — will be superseded by this plan)
│
└── NEW FILES (this plan):
    ├── agent_orchestration_design.md (agent communication details)
    ├── state_machines.md (formal state machines)
    ├── usage_metering_design.md (tier enforcement design)
    ├── glossary.md (terminology hub)
    ├── compliance_checklist.md (DPDP/GDPR specifics)
    └── decision_log.md (ADR history)
```

## Appendix B: File Status Summary

| File | Status | Action |
|------|--------|--------|
| `README.md` | ❌ Has inconsistencies | Update |
| `business_requirements.md` | ❌ Has gaps | Update |
| `system_architecture.md` | ❌ Has inconsistencies | Update |
| `agent_guidelines.md` | ❌ Has gaps | Update |
| `solutions_architect.md` | ❌ Needs updates | Update |
| `use_cases.md` | ⚠️ Needs additions | Update |
| `requirement_traceability_matrix.md` | ❌ Has errors + gaps | Update |
| `screen_review.md` | ❌ Has gaps | Update |
| `ucto_playbook.md` | ⚠️ Needs additions | Update |
| `deployment_readiness_plan.md` | ❌ Has contradictions | Update |
| `costs.md` | ❌ Has duplicate content | Update |
| `test_cases.md` | ⚠️ Needs linking to RTM | Will be updated when RTM is fixed |
| `next_refinements.md` | ✅ Adequate for vision doc | No changes needed |
| `portal_cli_design.md` | ✅ Adequate | No changes needed |
| `analysis_and_plan.md` | ⚠️ Superseded by this plan | Add deprecation notice pointing to this plan |
| (new) `agent_orchestration_design.md` | ❌ Missing | Create |
| (new) `state_machines.md` | ❌ Missing | Create |
| (new) `usage_metering_design.md` | ❌ Missing | Create |
| (new) `glossary.md` | ❌ Missing | Create |
| (new) `compliance_checklist.md` | ❌ Missing | Create |
| (new) `decision_log.md` | ❌ Missing | Create |
