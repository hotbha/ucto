# System Architecture

> **Last Updated:** 2026-05-09

## Technology Stack
| Layer | Technology | Notes |
|-------|-----------|-------|
| **Frontend** | Flutter + BLoC (Dart) | **Flutter Web for MVP**; mobile (iOS/Android) Phase 2. BLoC layers: blocs/, models/, ui/ |
| **Backend** | Spring Boot 4.0.6 (Java 25) | REST APIs: auth, projects, requirements, agents, audit, compliance, subscriptions, screens |
| **Database** | PostgreSQL | Primary data store. Only database used — no MySQL or alternative |
| **Cache** | Redis | Session cache, rate limiting, **agent message queue (Pub/Sub for MVP)** |
| **Object Storage** | S3-compatible (Backblaze/Wasabi/AWS S3) | Screens, documents, artifacts — abstraction is S3 API |
| **Infrastructure** | Docker Compose → Kubernetes | MVP to enterprise scaling |
| **CDN** | Cloudflare | Static assets and screen delivery |
| **Agent Broker** | **Redis Pub/Sub (MVP)** → RabbitMQ (Phase 2) | See [agent_orchestration_design.md](agent_orchestration_design.md) |

## Core Services

### Spring Boot Backend (Java 25)
- **Auth Service**: JWT-based (15-min access + refresh tokens), OAuth2 (Google), OTP (SMS), email+password fallback. Facebook OAuth deferred to Phase 2.
- **Project Service**: CRUD for customer projects, member management
- **Requirements Service**: BA-managed requirements lifecycle with clarification state machine
- **Agent Orchestration Service**: Coordinates **9 agents** (BA, PM, Developer, QA, Architect, UI/UX, Compliance, Documentation) via standardized message format. See [message_structure.md](message_structure.md) and [orchestrator_prompt_template.md](orchestrator_prompt_template.md).
- **PM / Scrum Master Service**: Backlog management, sprint lifecycle, loop coordination, DoR/DoD enforcement
- **Documentation Service**: Maintains living documentation aligned with implemented features and architecture decisions
- **Audit Service**: Append-only audit log for all user and agent actions. See [compliance_checklist.md](compliance_checklist.md) for scope.
- **Compliance Service**: DPDP/GDPR validation with automated checklist execution
- **Subscription Service**: Chargebee integration for tier management
- **Screen Service**: Upload, preview, approve/reject mockups with state machine
- **Usage Metering Service**: Count agent runs, enforce tier limits, graceful degradation

### Resilience Patterns
The system implements two key resilience patterns to handle external service failures:

#### DeepSeek Circuit Breaker
`DeepSeekAgentClient` includes an in-memory circuit breaker with three states:

| State | Behavior |
|-------|----------|
| **CLOSED** | Normal operation; API calls proceed normally |
| **OPEN** | All calls fast-fail with `{"circuitBreakerOpen": true}` error JSON; no HTTP calls made |
| **HALF_OPEN** | Single probe call allowed after cooldown; success resets to CLOSED, failure reopens |

**Configuration:** `ucto.llm.deepseek.circuit-breaker.threshold` (default: 3), `ucto.llm.deepseek.circuit-breaker.cooldown-ms` (default: 30,000)

- After `threshold` consecutive failures, the breaker transitions from CLOSED → OPEN
- After `cooldown-ms` elapses, the breaker transitions from OPEN → HALF_OPEN (probe)
- A successful probe resets the failure count and transitions HALF_OPEN → CLOSED
- A failed probe transitions HALF_OPEN → OPEN (cooldown restarts)
- The `SimulatedSprintService.parseOrFallback()` downstream handles any error JSON gracefully by falling back to empty DTOs, so circuit breaker fast-fails degrade without crashing the sprint flow

#### Git Clone Retry with Timeout
`RepoWorkspaceService.cloneWorkspace()` includes configurable retry with per-attempt timeout:

- **Max attempts:** `ucto.agent.developer.clone-retry.max-attempts` (default: 3)
- **Per-attempt timeout:** `ucto.agent.developer.clone-timeout` (default: 120s)
- **Backoff:** `ucto.agent.developer.clone-retry.backoff-ms` (default: 5,000ms fixed)

**Retry flow:**
1. Attempt git clone via `ProcessBuilder` with `process.waitFor(120, SECONDS)` timeout
2. On timeout → destroy process forcibly, clean up partial artifacts via `deleteDirectory()`, wait 5s, retry
3. On non-zero exit code → read error stream, clean up, wait 5s, retry
4. After exhausting all attempts → throw `RepoWorkspaceException` with message format: `"Clone failed for project {id} after {attempts} attempt(s). Last error: {lastError}"`

### Agent Communication Pattern
- **Message Broker**: Redis Pub/Sub (MVP). Messages follow the standardized structure defined in [message_structure.md](message_structure.md).
- **Message Format**: `{ fromAgent, toAgent, type, storyId, projectId, correlationId, timestamp, needsHuman, humanQuestions, payload }`
- **`needs_human = true` routing**: Origin Agent → PM → BA → PO (human). Only BA communicates with PO.
- **BA is bridge**: Only BA agent communicates externally with PO.
- **Audit Interceptor**: All agent messages automatically logged to AuditLog.
- **See**: [message_structure.md](message_structure.md) for full message catalog and payload schemas.

### AI Agents
| Agent | Role | Communication |
|-------|------|--------------|
| **Business Analyst (BA)** | PO interface, requirements owner, DoR gatekeeper | External (PO) + Internal |
| **Project Manager / Scrum Master** | Orchestrator, backlog owner, loop coordinator, DoR/DoD enforcer | Internal only |
| **Developer** | Implementation owner — small incremental code changes | Internal only |
| **Tester / QA** | Quality gatekeeper, DoD validator, test derivation from AC | Internal only |
| **Solution Architect** | Architecture owner, ADRs, non-functional design, feasibility review | Internal only |
| **UI/UX** | Experience owner, flows, screens, states, accessibility | Internal only |
| **Compliance / Governance** | Risk owner, security/privacy/regulatory evaluation | Internal only |
| **Documentation** | Knowledge owner, living docs, API docs, release notes | Internal only |

### Closed-Loop Workflows
The system uses four concurrent, repeating closed loops instead of a linear sprint pipeline:
- **Discovery Loop**: PO ↔ BA ↔ Architect ↔ UI/UX ↔ Compliance
- **Build Loop**: PO ↔ BA ↔ PM ↔ Dev ↔ QA
- **Risk Loop**: PO ↔ BA ↔ Compliance ↔ Architect/Dev
- **UX/Doc Loop**: PO ↔ BA ↔ UI/UX ↔ Documentation

See [closed_loop_workflows.md](closed_loop_workflows.md) for detailed loop definitions.

## Databases

### PostgreSQL Schemas
- `users` — Auth, roles, social IDs
- `projects` — Customer projects, tier, members
- `requirements` — BRD requirements per project
- `backlog_items` — Epics, stories, tasks with DoR/DoD status
- `sprints` — Sprint lifecycle with loop tracking
- `audit_logs` — Append-only audit trail
- `agent_logs` — Agent interactions and outputs
- `screens` — Generated screen metadata and storage URLs
- `compliance_checks` — DPDP/GDPR check results
- `subscriptions` — Chargebee subscription data
- `project_members` — User-project membership with Founder/Developer/Viewer roles
- `documentation_records` — Living documentation history and versions
- `agent_messages` — Structured message log for traceability and needs_human tracking

### Redis Usage
- JWT token blacklist (logout)
- Rate limiting per tier
- Agent message queue (Redis Pub/Sub for MVP)
- Session cache
- Processed event ID store (24h dedup for idempotency)

## Integrations

### MVP (Phase 0)
| Service | Purpose |
|---------|---------|
| Zoho SMTP | Transactional emails |
| Zoho / MSG91 SMS | OTP delivery |
| Razorpay | Payment processing |
| Chargebee | Subscription management |
| Google OAuth | Social login |
| Prometheus + Grafana | Monitoring (self-hosted) |

### Phase 2
| Service | Purpose |
|---------|---------|
| Facebook OAuth | Social login (deferred) |
| DigiLocker | Optional KYC |
| Mixpanel | Product analytics |
| Sentry | Error tracking |

## Deployment Layers

### MVP (Docker Compose)
```
+------------------+     +------------------+     +------------------+
|   Flutter Web    | <-- | Spring Boot API  | --> |   PostgreSQL     |
|   (Nginx)        |     | (Java 25)        |     |   (Primary)      |
+------------------+     +------------------+     +------------------+
                                  |
                          +------------------+
                          |     Redis        |
                          | (Cache + Pub/Sub)|
                          +------------------+
```

### Scaling (Kubernetes)
- Load Balancer: NGINX / Traefik
- Auto-scaling API pods
- Managed PostgreSQL (RDS / Cloud SQL)
- Redis Cluster
- CDN (Cloudflare) for static assets and screens

## Security
- **JWT**: Short-lived access tokens (15 min) + refresh tokens (7 days)
- **OAuth2**: Google for social login (MVP); Facebook deferred to Phase 2
- **Password**: BCrypt hashing for email+password fallback
- **CORS**: Restricted to frontend origin
- **Audit**: Append-only logs for all sensitive operations
- **Compliance**: DPDP (India) and GDPR (global) data protection — see [compliance_checklist.md](compliance_checklist.md)

## Performance
- Redis caching for frequent queries
- CDN for screen assets
- Database indexing on frequently queried columns
- Connection pooling (HikariCP)
- Pagination for list endpoints

## "Agent Run" Definition
An **agent run** is counted each time the orchestration service publishes a `trigger` event to any agent topic. See [usage_metering_design.md](usage_metering_design.md) for exact rules and tier enforcement.

## Enhancements (Future)
- Design System Repository (shared Flutter component library)
- Localization Hooks (Hindi, Tamil, Bengali, English, Spanish, French)
- RabbitMQ migration from Redis Pub/Sub
- A/B testing support

[← Back to README](README.md) | Related: [closed_loop_workflows.md](closed_loop_workflows.md), [message_structure.md](message_structure.md), [agent_guidelines.md](agent_guidelines.md), [orchestrator_prompt_template.md](orchestrator_prompt_template.md)
