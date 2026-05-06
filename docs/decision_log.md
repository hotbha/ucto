# Decision Log

## Purpose
Track all open and resolved architecture decisions for UCTO. Each entry documents: decision ID, date, status, options considered, rationale, and follow-up actions.

---

### OD1: Frontend Framework (Flutter vs. React)

| Field | Value |
|-------|-------|
| **Decision ID** | OD1 |
| **Date** | 2026-05-05 |
| **Status** | Accepted — Recommended default: Flutter |
| **Options** | (a) Flutter (keep) / (b) React (migrate) / (c) Both |
| **Recommended Default** | Flutter + BLoC (Dart) |
| **Rationale** | Repo has ~800+ lines of substantive Flutter BLoC code across 12+ screens, 4 BLoC modules. React artifacts in `package.json`/`vite.config.ts` are orphan/experimental with no user-facing code. Switching to React would discard existing investment. |
| **Consequences (if Flutter)** | All docs must standardize on Flutter + BLoC. React artifacts documented as deprecated. Mobile (iOS/Android) deferred to Phase 2. |
| **Consequences (if React)** | Must rewrite all existing Flutter BLoC code. Frontend MVP delayed by 2-4 weeks. |
| **Follow-up** | Product owner to confirm. Until resolved, docs use "Flutter + BLoC (recommended)" wording. |

---

### OD2: Agent Message Broker

| Field | Value |
|-------|-------|
| **Decision ID** | OD2 |
| **Date** | 2026-05-05 |
| **Status** | Accepted — Recommended default: Redis Pub/Sub |
| **Options** | (a) Redis Pub/Sub / (b) RabbitMQ / (c) Direct API calls (synchronous) |
| **Recommended Default** | Redis Pub/Sub (for MVP) |
| **Rationale** | Redis already in stack for cache/sessions. Zero additional infrastructure. Topic naming maps cleanly to agent event patterns. RabbitMQ can be added in Phase 2 when persistent delivery and consumer groups are needed. |
| **Consequences** | MVP docs standardize on Redis Pub/Sub. Topic convention `agent.<type>.<action>` documented. RabbitMQ migration path noted for Phase 2. |
| **Follow-up** | Product owner to confirm. Until resolved, docs use "Redis Pub/Sub for MVP; RabbitMQ Phase 2." |

---

### OD3: "Agent Run" Definition

| Field | Value |
|-------|-------|
| **Decision ID** | OD3 |
| **Date** | 2026-05-05 |
| **Status** | Accepted — Recommended default: One API trigger call |
| **Options** | (a) One complete sprint cycle / (b) One requirement processed by all agents / (c) One API trigger event to any single agent |
| **Recommended Default** | One API `trigger` event to any single agent (option c) |
| **Rationale** | Simplest to meter and bill. Aligns with common AI API billing models. Clear, auditable, non-ambiguous. |
| **Consequences** | All tier limits (Free=5, Startup=50, Growth=200) expressed in terms of trigger events. Usage counting, metering, and enforcement designed accordingly. |
| **Follow-up** | Product owner to confirm. `usage_metering_design.md` written with this definition. |

---

### OD4: Mobile App Priority

| Field | Value |
|-------|-------|
| **Decision ID** | OD4 |
| **Date** | 2026-05-05 |
| **Status** | Accepted — Web + Android simultaneously |
| **Options** | (a) Web-only (Flutter Web) / (b) Web + Android simultaneously / (c) Web + iOS + Android |
| **Recommended Default** | Flutter Web only for MVP; mobile (iOS/Android) in Phase 2 |
| **Rationale** | Faster MVP by targeting web only. BLoC code is shared across platforms, so Phase 2 mobile will build on existing code. Reduces QA surface area for initial launch. |
| **Consequences** | Deployment targets Flutter Web via Nginx. Mobile build pipelines deferred. `pubspec.yaml` mobile plugins (e.g., `flutter_facebook_auth`) are pre-configured but not MVP requirements. |
| **Follow-up** | Product owner to confirm. Until resolved, docs use "Flutter Web (MVP) → Mobile Phase 2." |

---

### OD5: Facebook OAuth Timing

| Field | Value |
|-------|-------|
| **Decision ID** | OD5 |
| **Date** | 2026-05-05 |
| **Status** | Accepted — Recommended default: Phase 2 |
| **Options** | (a) Include Facebook in MVP / (b) Defer to Phase 2 |
| **Recommended Default** | Defer Facebook OAuth to Phase 2 |
| **Rationale** | Google OAuth + Email/Password covers ~95% of user authentication needs. Facebook OAuth has ongoing API stability issues and requires additional platform review. Reducing auth surface simplifies MVP security audit. |
| **Consequences** | MVP auth: Google OAuth + Email/Password + OTP (SMS). Facebook OAuth removed from MVP documentation. `flutter_facebook_auth` dependency remains in `pubspec.yaml` for Phase 2 readiness. |
| **Follow-up** | Product owner to confirm. `next_refinements.md` lists Facebook OAuth as Phase 2 item. |

---

## Summary Table

| ID | Decision | Status | Recommended Default | Priority |
|----|----------|--------|-------------------|----------|
| OD1 | Frontend: Flutter vs React | Accepted | Flutter + BLoC | P0 |
| OD2 | Agent message broker | Accepted | Redis Pub/Sub | P0 |
| OD3 | "Agent run" definition | Accepted | One trigger event per agent | P0 |
| OD4 | Mobile app priority | Accepted | Web+Android | P1 |
| OD5 | Facebook OAuth timing | Accepted | Phase 2 | P1 |
