# System Architecture

## Technology Stack
| Layer | Technology | Notes |
|-------|-----------|-------|
| **Frontend** | Flutter + BLoC (Dart) | Mobile + Web: separate blocs/, models/, ui/ layers |
| **Backend** | Spring Boot (Java) | REST APIs: auth, requirements, audit, compliance |
| **Database** | PostgreSQL | Primary data store |
| **Cache** | Redis | Session cache, rate limiting, agent message queue |
| **Object Storage** | S3-compatible (Backblaze/Wasabi) | Screens, documents, artifacts |
| **Infrastructure** | Docker Compose → Kubernetes | MVP to enterprise scaling |

## Core Services

### Spring Boot Backend
- **Auth Service**: JWT-based, OAuth2 (Google/Facebook), OTP (SMS), email+password fallback
- **Project Service**: CRUD for customer projects, member management
- **Requirements Service**: BA-managed requirements lifecycle
- **Agent Orchestration Service**: Coordinates 6 AI agents via event-driven messaging
- **Audit Service**: Immutable audit log for all actions
- **Compliance Service**: DPDP/GDPR validation, privacy policy generation
- **Subscription Service**: Chargebee integration for tier management
- **Screen Service**: Upload, preview, approve/reject mockups

### Agent Communication Pattern
- **Message Broker**: Redis Pub/Sub or RabbitMQ for async agent communication
- **Agent Events**: `agent.<type>.trigger`, `agent.<type>.complete`, `agent.<type>.error`
- **BA is bridge**: Only BA agent communicates externally with customers
- **Audit Interceptor**: All agent messages logged to AuditLog

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
- `users` - Auth, roles, social IDs
- `projects` - Customer projects, tier, members
- `requirements` - BRD requirements per project
- `audit_logs` - Immutable audit trail
- `agent_logs` - Agent interactions and outputs
- `screens` - Generated screen metadata and storage URLs
- `compliance_checks` - DPDP/GDPR check results
- `subscriptions` - Chargebee subscription data

### Redis Usage
- JWT token blacklist (logout)
- Rate limiting per tier
- Agent message queue (Pub/Sub)
- Session cache

## Integrations
| Service | Purpose | Priority |
|---------|---------|----------|
| Zoho SMTP | Transactional emails | Required |
| Zoho / MSG91 SMS | OTP delivery | Required |
| Razorpay | Payment processing | Required |
| Chargebee | Subscription management | Required |
| Google OAuth | Social login | Required |
| Facebook OAuth | Social login | Required |
| DigiLocker | Optional KYC | Optional |
| Prometheus + Grafana | Monitoring | Future |
| Mixpanel / Amplitude | Analytics | Future |

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
                          |   (Cache/Queue)  |
                          +------------------+
```

### Scaling (Kubernetes)
- Load Balancer: NGINX / Traefik
- Auto-scaling API pods
- Managed PostgreSQL (RDS / Cloud SQL)
- Redis Cluster
- CDN (Cloudflare) for static assets and screens

## Security
- **JWT**: Short-lived access tokens + refresh tokens
- **OAuth2**: Google and Facebook for social login
- **Password**: BCrypt hashing for email+password fallback
- **CORS**: Restricted to frontend origin
- **Audit**: Immutable logs for all sensitive operations
- **Compliance**: DPDP (India) and GDPR (global) data protection

## Performance
- Redis caching for frequent queries
- CDN for screen assets
- Database indexing on frequently queried columns
- Connection pooling (HikariCP)
- Pagination for list endpoints

## Enhancements (Future)
- Design System Repository (shared Flutter component library)
- Localization Hooks (Hindi, Tamil, Bengali, English, Spanish, French)
- Load Balancer (NGINX/Traefik)
- Analytics (Mixpanel/Amplitude)
- A/B testing support
