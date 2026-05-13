# UCTO MVP Implementation Checklist

## Phase 1 — Foundation Build Fixes
- [x] Fix pom.xml: `spring-boot-starter-webmvc` → `spring-boot-starter-web`
- [x] Fix pom.xml: Remove `spring-boot-starter-webmvc-test`, use `spring-boot-starter-test`
- [x] Remove `flutter_facebook_auth` from pubspec.yaml (Phase 2 per docs)
- [x] Remove Facebook login button from login_screen.dart
- [x] Remove Facebook OAuth from User entity, AuthBloc, frontend

## Phase 1B — Backend Missing Core Services
- [x] Add RedisConfig.java — RedisTemplate, RedisMessageListenerContainer, StringRedisTemplate
- [x] Add AgentOrchestrationService.java — Redis Pub/Sub publish/subscribe for agent events
- [x] Add AgentEventListener.java — Redis message listener subscribing to `agent.*` topics
- [x] Add GlobalExceptionHandler.java — @ControllerAdvice for consistent error responses
- [x] Add screen revision tracking: revisionCount, approvedBy, approvedAt fields on Screen entity
- [x] Add BA clarification round tracking: clarificationRound field on Requirement entity
- [x] Add `ProjectRepository.countByOwnerId` for usage metering
- [x] Fix UsageMeterService.getProjectCount() to use actual project count
- [x] Fix EmailController password reset URL from React to Flutter web
- [x] Fix SecurityConfig CORS origins for Flutter web
- [x] Add proper Redis Pub/Sub event publishing on agent trigger
- [x] Add ProjectController DELETE endpoint
- [x] Add project member authorization checks
- [x] Fix ProjectService.deleteProject() method
- [x] Update OAuthRequest comment to reflect Google-only MVP scope

## Phase 2 — Auth & Project Core
- [x] Fix auth token check to handle expired tokens gracefully (via decodeJwt check)
- [x] Add route definitions in main.dart MaterialApp (all 8 screens)
- [x] Fix ProjectBloc._onLoadProjects to call actual API
- [x] Fix RequirementBloc._onLoad to call actual API
- [x] Fix DashboardScreen CreateProject dialog to pass correct constructor args
- [x] Add project member management endpoints (addMember, getMembers)

## Phase 3 — Agent MVP
- [x] Implement Redis Pub/Sub agent trigger flow via AgentOrchestrationService
- [x] Implement AgentRun persistence on trigger via UsageMeterService
- [x] Implement BA clarification loop (max 3 rounds) — clarificationRound on Requirement
- [x] Implement usage metering enforcement (402 when exhausted) via GlobalExceptionHandler
- [x] Implement agent.trigger audit interception via AgentOrchestrationService

## Phase 4 — Screen Workflow
- [x] Add revision tracking to Screen entity (revisionCount, approvedBy, approvedAt)
- [x] Implement per-screen approval/rejection in ScreenController with state machine enforcement
- [x] Add screen status state machine enforcement (PENDING → APPROVED/REJECTED/CHANGES_REQUESTED)

## Phase 5 — Deployment
- [x] Fix Dockerfile.backend build paths
- [x] Fix Dockerfile.frontend build paths
- [x] Add .dockerignore
- [x] Add .env.example
- [x] Add application-dev.properties, application-prod.properties
- [x] Add Makefile / build scripts
- [x] Verify build compiles (mvn clean compile — BUILD SUCCESS)

## Bugs Fixed During Build Verification
- [x] Fix RedisConfig.java: Replace deprecated Jackson2JsonRedisSerializer with RedisSerializer, remove ObjectMapper/JsonMapper imports that fail on Jackson 3 vs 2 classpath conflict
- [x] Fix AgentOrchestrationService.java: Replace tools.jackson imports with com.fasterxml.jackson, add missing SubscriptionService injection for usage check
- [x] Fix User.java: Remove stale facebookId getter/setter stubs (facebookId field already removed, but stubs remained causing compile error)
- [x] Fix pom.xml: Add explicit Jackson databind, datatype-jsr310, core dependencies (needed for RedisConfig serialization)

## Phase 6 — Post MVP Cleanup & Production Readiness (Completed May 2026)
- [x] A1: Create application-test.properties (H2 + mocked Redis for test context)
- [x] A2: Fix ProjectDetailScreen bugs (wrong event CreateProject→CreateRequirement, empty onPressed, hardcoded stats)
- [x] A3: Fix PricingScreen Chargebee snackbar (removed "integration pending" text)
- [x] B1: Implement Chargebee webhook handling (subscription_created, cancelled, updated, renewed)
- [x] B2: Implement ZohoEmailService using spring-boot-starter-mail
- [x] B3: Implement ConsoleSmsService (interface-based SMS service)
- [x] B4: Implement Agent Event Pipeline (BA→Developer→Tester→Compliance routing with error handling)
- [x] C1: Production CORS Configuration (added ucto.app, www.ucto.app, app.ucto.app)
- [x] D1: Delete React orphaned files (package.json, tsconfig, vite.config, index.html, public/)
- [x] D3: Remove unnecessary code artifacts (src/ directory with VS Code extension artifacts)

## Completed May 2026 Session — Tests, Rate Limiting, Onboarding, Documentation
- [x] A4: Implement missing exhaustive test cases (ERR-01–ERR-05 in GlobalExceptionHandlerTest, SEC-01–SEC-04 in SecurityEdgeCaseTest)
- [x] B5: Implement Rate Limiting (RateLimitingFilter.java using Bucket4j token bucket, configurable limits per endpoint)
- [x] D2: Align Onboarding flow with documented user journey (OnboardingScreen 3-step wizard → first project prompt on DashboardScreen)
- [x] E3: Update docs as needed (portal_cli_design.md dedup, next_refinements.md status, deployment_readiness_plan.md section 3 accuracy)

## Completed May 2026 Session — Final Items
- [x] B6: Sentry/Error Monitoring (SentryConfig.java, SentryLoggingInterceptor.java, WebConfig.java, pom.xml deps, application.properties config)
- [x] Frontend: Screen Preview UI (screen_model.dart, screen_bloc, _ScreensTab with approve/reject/changes-requested workflow)
- [x] Frontend: Help/Tooltips (help_tooltip.dart with HelpTooltip, HelpSection, HelpOverlay widgets; added to DashboardScreen AppBar)
- [x] Frontend: Password Reset screens (forgot_password_screen.dart, reset_password_screen.dart, routes in main.dart, link in login_screen.dart)
- [x] Frontend: Email Verification screen (verify_email_screen.dart with token-based verification flow)

## Completed May 2026 Session — BA Chat Test Suite
- [x] Backend: BAChatServiceTest.java — 16 tests covering greeting, clarification, decision extraction, escalation, finalization, history, ambiguity detection, round tracking, usage metering integration, agent trigger on each message
- [x] Backend: BAChatControllerTest.java — 7 tests covering send message, 402 payment required, chat history, missing fields, empty message, auth enforcement
- [x] Backend: UsageMeterServiceTest.java — 10 tests covering monthly run counter, per-user tracking, start-of-month boundary, project count tracking
- [x] Backend: RequirementControllerTest.java — 8 tests covering CRUD, status transition, clarification round cap, 400/404 error handling
- [x] Backend: SubscriptionControllerTest.java — 8 tests covering plans listing, my subscription, free defaults, upgrade, trial, auth enforcement
- [x] Frontend: ba_chat_model_test.dart — 11 tests covering BAChatMessage and BAChatHistoryResponse fromJson for all message types
- [x] Frontend: ba_chat_bloc_test.dart — 10 test cases covering send message, load history, clear chat, error handling, all message types
- [x] Frontend: ba_chat_screen_test.dart — 13 widget tests covering all UI states (initial, loading, error, send, escalation banner, complete banner, empty, message bubble)
- [x] IMPLEMENTATION_TODO.md updated with test suite completion

## Remaining — Future (Infrastructure / Non-Code)
- [ ] C2: Domain + SSL Configuration (nginx certbot for ucto.app)
- [ ] C3: Database Backup Configuration (automated PostgreSQL backups)
