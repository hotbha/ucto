# UNICORNATOR Project Guardrail

## CRITICAL: This is the UNICORNATOR project (repo: github.com/hotbha/ucto.git)

**DO NOT confuse with BMJ (BookMyJuice). BMJ is a SEPARATE project at x:\BMJ.**
**All work must stay strictly within x:\Unicornator.**

---

## Guardrails

1. **Frontend = Flutter + BLoC only.** No React, no React Native, no other frontend framework.
2. **MVP platform = Flutter Web.** Mobile (iOS/Android) is Phase 2.
3. **Agent broker = Redis Pub/Sub (MVP).** RabbitMQ is Phase 2. Do not implement RabbitMQ in MVP.
4. **Agent run = one trigger event to one agent.** See docs/usage_metering_design.md and docs/decision_log.md.
5. **Facebook OAuth = Phase 2.** Do not implement Facebook OAuth in MVP.
6. **Backend = Spring Boot + Java.** Current pom.xml uses Spring Boot 3.4.4, Java 21.
7. **Database = PostgreSQL (prod), H2 (tests).** Only PostgreSQL. No MySQL.
8. **All decisions documented in docs/decision_log.md.** Follow documented decisions, do not assume.

## Source of Truth

- **docs/business_requirements.md** — Business requirements
- **docs/system_architecture.md** — System architecture
- **docs/use_cases.md** — Use cases with flow diagrams
- **docs/state_machines.md** — State machines for screens, requirements, sprints
- **docs/decision_log.md** — All MVP decisions recorded
- **docs/exhaustive_test_cases.md** — Complete test coverage document
