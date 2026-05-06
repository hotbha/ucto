# Solutions Architecture

## Delivery Platform
- **Frontend MVP**: Flutter Web (NOT React — React artifacts in `package.json`/`vite.config.ts` are deprecated)
- **Frontend Phase 2**: Flutter Mobile (iOS + Android) — shared BLoC codebase
- **Backend**: Spring Boot 4.0.6 (Java 25)
- **Agent Broker**: Redis Pub/Sub (MVP) → RabbitMQ (Phase 2)

## Integrations

### MVP
- Payment: Razorpay (preferred), PayU fallback
- SMS: Zoho (preferred), MSG91 fallback
- Email: Zoho SMTP
- Auth: Google OAuth (primary), Email + Password (fallback), Mobile OTP
- Subscriptions: Chargebee
- Monitoring: Prometheus + Grafana (self-hosted)
- Object Storage: S3-compatible (Backblaze / Wasabi / AWS S3)

### Phase 2
- Facebook OAuth
- DigiLocker KYC (optional)
- CRM: Zoho, Freshworks
- Cloud: Netcore, E2E Networks
- Analytics: Mixpanel

## Authentication Strategies
- **MVP**: Google OAuth + Email/Password + Mobile OTP
- **Phase 2**: Facebook OAuth, DigiLocker KYC (optional)
- **Session**: JWT (15-min access + 7-day refresh tokens)

## Database & Architecture
- PostgreSQL primary (no MySQL)
- Redis for cache + Pub/Sub agent queue
- Docker Compose MVP
- Kubernetes scaling

## India-first Solutions
- Payment: Razorpay
- SMS: Zoho, MSG91
- Email: Zoho
- Compliance: DPDP (India), GDPR (global)
- Hosting: Netcore, E2E Networks (optional)

## Open Decisions
See [decision_log.md](decision_log.md) for all open architecture decisions (OD1-OD5).
