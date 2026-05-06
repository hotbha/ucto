# System Architecture

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
- **Agent Orchestration Service**: Coordinates 6 AI agents via **Redis Pub/Sub** event-driven messaging
- **Audit Service**: Append-only audit log for all user and agent actions. See [compliance_checklist.md](compliance_checklist.md) for scope.
- **Compliance Service**: DPDP/GDPR validation with automated checklist execution
- **Subscription Service**: Chargebee integration for tier management
- **Screen Service**: Upload, preview, approve/reject mockups with state machine
- **Usage Metering Service**: Count agent runs, enforce tier limits, graceful degradation

### Agent Communication Pattern
- **Message Broker**: Redis Pub/Sub (MVP). Topics: `agent.<type>.<action>`
- **Agent Events**: `agent.<type>.trigger`, `agent.<type>.complete`, `agent.<type>.error`, `agent.<type>.clarify`
- **BA is bridge**: Only BA agent communicates externally with customers
- **Audit Interceptor**: All agent messages automatically logged to AuditLog
- **See**: [agent_orchestration_design.md](agent_orchestration_design.md) for full event schema, retry policy, timeout handling

### AI Agents
| Agent | Role | Communication |
|-------|------|--------------|
| Business Analyst | Requirements elicitation, customer communication | External (customer) + Internal |
| Developer | Code generation (Flutter + Spring Boot) | Internal only |
| Tester | Test generation, validation | Internal only |
| Compliance | DPDP/GDPR enforcement | Internal only |
| UI/UX Expert | Screen generation, accessibility | Internal only |
| Solutions Architect | Integration suggestions, architecture | Internal only |

## Databases

### PostgreSQL Schemas
- `users` — Auth, roles, social IDs
- `projects` — Customer projects, tier, members
- `requirements` — BRD requirements per project
- `audit_logs` — Append-only audit trail
- `agent_logs` — Agent interactions and outputs
- `screens` — Generated screen metadata and storage URLs
- `compliance_checks` — DPDP/GDPR check results
- `subscriptions` — Chargebee subscription data
- `project_members` — User-project membership with Founder/Developer/Viewer roles

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
