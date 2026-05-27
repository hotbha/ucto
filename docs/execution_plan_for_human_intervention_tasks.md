# Execution Plan: Automated Provisioning of Infrastructure Tasks

> **Document Purpose:** Map each task from `tasks_requiring_human_intervention.md` to available MCP servers, CLI tools, and npm/pip packages that this agent can use to automate or partially automate the work. Categorize by automation level.

> **Generated:** May 14, 2026 (Updated: Fast2SMS replaces Twilio-OTP)
> **MCP Servers Configured:** 7 (dart, firebase, google-docs, m365agentstoolkit, chargebee-knowledge-base, chargebee-onboarding, chargebee-data-lookup)
> **CLI Tools Available:** openssl, keytool (JDK 21), python, pip, flutter 3.x, docker v29.4.0, kubectl v1.34.1, mvn, git, npx/npm
> **Fast2SMS:** Configured with API key + docs integrated

---

## Automation Level Legend

| Icon | Level | Meaning |
|------|-------|---------|
| 🟢 **FULLY AUTOMATABLE** | Agent can execute entirely with existing tools + MCPs; no human credentials needed | 
| 🟡 **PARTIALLY AUTOMATABLE** | Agent can do most steps but needs 1-2 pieces of human-provided input (account, API key, decision) |
| 🔴 **HUMAN-ONLY** | Requires human account ownership, paid subscription, or physical action the agent cannot perform |
| ⚪ **DEFERRED** | Depends on another task being completed first, or is P3/low priority |

---

## 1. 🟡 Chargebee Subscription & Billing

**Status:** 🟡 **PARTIALLY AUTOMATABLE** — Chargebee MCPs configured with test API key

**Available Tools:**
- `chargebee-onboarding` MCP — can create pricing plans, configure webhooks
- `chargebee-data-lookup` MCP — can query subscriptions/customers
- `chargebee-knowledge-base` MCP — documentation lookups

**What Agent Can Do:**
1. ✅ **Create 4 pricing plans** (FREE, STARTUP, GROWTH, ENTERPRISE) via `chargebee-onboarding` MCP
2. ✅ **Configure webhook endpoint** URL via `chargebee-onboarding` MCP
3. ✅ **Update `.env`** with site name and API key
4. ✅ **Write tests** for ChargebeeService (unit + integration)

**What Needs Human:**
1. 🔴 **Account registration at chargebee.com** — must create the `ucto` site manually
2. 🔴 **Live mode switch** — Test Mode API keys expire / cannot process real payments
3. 🔴 **Tax/Invoice settings** — country-specific tax configuration

**Action Plan:**
1. Ask human to create Chargebee account + site → provide site name + API key
2. Once provided, agent uses `chargebee-onboarding` MCP to create pricing plans and webhooks
3. Agent updates `.env` with real values

---

## 2. 🟡 Sentry Error Monitoring

**Status:** 🟡 **PARTIALLY AUTOMATABLE** — Need human to create Sentry account first, then agent configures

**Available Tools:**
- `npm` — can install `@sentry/cli` for project creation and release tracking
- `curl` — can call Sentry API if auth token provided

**What Agent Can Do (after account exists):**
1. ✅ **Create Sentry project** via Sentry API (using auth token)
2. ✅ **Configure DSN** in `.env` and `application.properties`
3. ✅ **Set up release tracking** — create releases, associate commits
4. ✅ **Configure alert rules** via Sentry API

**What Needs Human:**
1. 🔴 **Sentry.io account registration**
2. 🔴 **Auth Token generation** (Settings → Developer Settings → Auth Tokens)

**Action Plan:**
1. Install `@sentry/cli`: `npm install -g @sentry/cli`
2. Ask human to create Sentry account + provide Auth Token
3. Agent runs `sentry-cli --auth-token <token> projects create ucto-backend --org <org>`
4. Agent configures DSN in all config files

---

## 3. 🟡 Google OAuth Credentials

**Status:** 🟡 **PARTIALLY AUTOMATABLE** — Need Google Cloud project + OAuth consent screen configured by human

**Available Tools:**
- `google-docs` MCP — for documentation/reference lookups
- `firebase` MCP — can manage Firebase project linked to Google Cloud
- `npx` — can install `firebase-tools` for Firebase operations

**What Agent Can Do (after project exists):**
1. ✅ **Configure OAuth credentials** in `application.properties`
2. ✅ **Set up Spring Security** OAuth2 flow (already coded, just needs config values)
3. ✅ **Configure Flutter Web OAuth** — update `main.dart` with Client ID

**What Needs Human:**
1. 🔴 **Google Cloud Project creation** (console.cloud.google.com)
2. 🔴 **OAuth Consent Screen setup** (User Type: External, app name, support email, authorized domains)
3. 🔴 **OAuth 2.0 Client ID creation** (web client with authorized redirect URIs)
4. 🔴 **Client Secret copy** — needs to be securely communicated

**Action Plan:**
1. Agent provides step-by-step instructions for Google Cloud Console
2. Human creates project + OAuth consent screen + Client ID
3. Human provides Client ID and Client Secret
4. Agent updates config files

---

## 4. 🟢 JWT Secret Key (Production)

**Status:** 🟢 **FULLY AUTOMATABLE** — openssl available

**Available Tools:**
- `openssl` — `openssl rand -base64 64`

**What Agent Can Do:**
1. ✅ **Generate 512-bit cryptographically secure secret**:
   ```bash
   openssl rand -base64 64 > jwt_secret.txt
   ```
2. ✅ **Update `.env`** with new JWT_SECRET
3. ✅ **Create Kubernetes Secret** YAML for production deployment
4. ✅ **Document rotation policy** in `docs/operations.md`

**Action Plan:**
1. Generate secret with openssl
2. Update `.env` and create `k8s/secret-jwt.yaml`
3. Add to documentation

---

## 5. 🟡 Database Credentials (Production/Staging)

**Status:** 🟡 **PARTIALLY AUTOMATABLE** — Need PostgreSQL instance URL from human

**Available Tools:**
- `docker` — can run PostgreSQL locally for dev/staging
- `kubectl` — can create Postgres K8s deployments
- `git` — can commit Docker Compose config

**What Agent Can Do:**
1. ✅ **Create Docker Compose file** for local Postgres + Redis (if not already done)
2. ✅ **Create Kubernetes Deployment** YAML for PostgreSQL
3. ✅ **Configure HikariCP** connection pool settings
4. ✅ **Update `.env`** with credentials

**What Needs Human:**
1. 🔴 **Choose provider** (AWS RDS, GCP Cloud SQL, Supabase, etc.)
2. 🔴 **Provision the database instance** — this requires cloud account login
3. 🔴 **Provide connection URL** (host, port, username, password)

**Action Plan:**
1. If using Docker local: agent creates `docker-compose.yml` with Postgres service → no human needed for dev
2. If production cloud: human provisions PostgreSQL → provides URL → agent configures

---

## 6. 🟡 Redis Configuration (Production)

**Status:** 🟡 **PARTIALLY AUTOMATABLE** — Similar pattern to Postgres

**Available Tools:**
- `docker` — can run Redis locally
- `kubectl` — can deploy Redis in K8s

**What Agent Can Do:**
1. ✅ **Create Docker Compose** for local Redis
2. ✅ **Create Kubernetes Deployment** YAML for Redis
3. ✅ **Configure `application.properties`** with appropriate TLS and pool settings

**What Needs Human:**
1. 🔴 **Provision Redis instance** in cloud or provide host/port
2. 🔴 **Provide password** for secured Redis

**Action Plan:**
1. For dev: agent sets up Docker-based Redis
2. For prod: human provisions → agent configures

---

## 7. 🟡 Email/SMTP Server Configuration

**Status:** 🟡 **PARTIALLY AUTOMATABLE** — .env already has SMTP_HOST=smtp.zoho.com, just needs password

**Available Tools:**
- `m365agentstoolkit` MCP — can potentially manage Microsoft 365 email settings
- Zoho SMTP already configured in `.env`

**What Agent Can Do:**
1. ✅ **Update `.env`** with SMTP credentials when provided
2. ✅ **Write email template files** (password reset, verification, welcome)
3. ✅ **Configure Spring Mail** properties in `application.properties` (already done)

**What Needs Human:**
1. 🔴 **Provide SMTP password** for `noreply@ucto.app`
2. 🔴 **Verify sender email** — Zoho will send verification link

**Action Plan:**
1. Agent asks for SMTP password (or alternative provider credentials)
2. Once provided, agent updates `.env` and verifies connection by sending test email

---

## 8. 🟢 Deployment Infrastructure

**Status:** 🟢 **FULLY AUTOMATABLE (artifacts creation)** — Docker + kubectl available, can write all configs

**Available Tools:**
- `docker` — can build and test containers locally
- `kubectl` — can apply K8s manifests
- `git` — can push to GitHub, triggering GitHub Actions
- `mvn` — can build JAR for containerization

**What Agent Can Do:**
1. ✅ **Write complete Dockerfile** for backend (Java 21 + JAR)
2. ✅ **Write Nginx config** for frontend reverse proxy
3. ✅ **Write Kubernetes manifests** (Deployment, Service, Ingress, Secrets)
4. ✅ **Write GitHub Actions workflow** for CI/CD
5. ✅ **Test Docker build** locally

**What Needs Human (for live deployment):**
1. 🔴 **Docker Hub / GitHub Container Registry account** and credentials
2. 🔴 **Cloud provider account** (AWS/GCP/Azure/DigitalOcean)
3. 🔴 **GitHub Actions Secrets** — needs to be added manually via repo Settings
4. 🔴 **SSH key** for deployment server

**Action Plan:**
1. ✅ Agent creates: `Dockerfile.backend`, `Dockerfile.frontend`, `nginx.conf`, `k8s/*.yaml`, `.github/workflows/deploy.yml`
2. Human creates Docker Hub account + cloud VM
3. Human adds secrets to GitHub
4. Agent tests Docker build locally to verify

---

## 9. 🟢 OTP/SMS Service (Fast2SMS)

**Status:** 🟢 **FULLY AUTOMATABLE** — Fast2SMS API key provided + docs available

**Available Tools:**
- `Fast2SMS API key` — Provided by human: `rMOkPavS2ZiQAnT7qNC64IR9gtzeXxKWDbyHF3wEJmjfV1cdBprwIBVeXayYqcN0zbG5RmiQKW4vhnLj`
- `Fast2SMS API Docs` — https://docs.fast2sms.com/reference
- `curl` / `RestTemplate` — for HTTP calls to Fast2SMS API
- `okhttp` — Maven dependency for making HTTP calls (already available in Spring Boot)

**What Agent Can Do:**
1. ✅ **Create Fast2SMSService** Java class using `RestTemplate`
2. ✅ **Add Fast2SMS config** to `.env` and `application.properties`
3. ✅ **Add @Primary** annotation to resolve bean conflict with ConsoleSmsService
4. ✅ **Write 10-unit test suite** for Fast2SmsService (headers, body, edge cases)
5. ✅ **Write OTP verification logic** with in-memory or Redis-based OTP store

**What Needs Human:**
1. 🔴 **Fast2SMS sender ID registration** — may need approval from Fast2SMS for specific sender ID
2. 🔴 **DLT template registration** — In India, SMS templates need DLT registration (telecom regulatory requirement)

**Action Plan:**
1. ✅ Add Fast2SMS API key to `.env`
2. ✅ Create `Fast2SmsService` and fix bean conflict with `@Primary`
3. ✅ Create unit tests for Fast2SmsService
4. ✅ Update UserService to inject SmsService and use it

---

## 10. 🟢 Code Coverage Tool Configuration

**Status:** 🟢 **FULLY AUTOMATABLE** — JaCoCo already in pom.xml

**Available Tools:**
- `mvn` — can run JaCoCo reports
- JaCoCo Maven plugin — already configured (pending verification)

**What Agent Can Do:**
1. ✅ **Verify JaCoCo configuration** in `pom.xml`
2. ✅ **Set coverage thresholds** (80% instruction, 70% branch, 80% line, 70% method)
3. ✅ **Configure exclusion patterns** (DTOs, Entities, Configuration classes)
4. ✅ **Integrate with CI** — fail build if thresholds not met
5. ✅ **Generate coverage report**: `mvn jacoco:report`

**Action Plan:**
1. Run `mvn verify` to confirm JaCoCo works
2. Update pom.xml with explicit threshold rules and exclusions
3. Add badge markdown to README

---

## 11. ⚪ Performance / Load Testing Infrastructure

**Status:** ⚪ **DEFERRED** — Requires staging environment first (depends on #8)

**Available Tools:**
- `pip` — can install `locust` for Python-based load testing
- `npx` — can run `k6` via Docker

**What Agent Can Do (after staging is deployed):**
1. ✅ **Write Locust test scripts** (Python) for login, agent trigger, project listing
2. ✅ **Write k6 test scripts** (JavaScript) as alternative
3. ✅ **Define performance thresholds** in documentation
4. ✅ **Create CI pipeline** for performance regression testing

**What Needs Human:**
1. 🔴 **Staging environment deployed** (depends on #8)
2. 🟢 **Threshold decisions** — agent can propose values, human approves

**Action Plan:**
1. Agent creates `tests/performance/` folder with Locust + k6 scripts
2. Agent documents thresholds in `docs/performance_benchmarks.md`
3. Postpone execution until staging environment is live

---

## 12. 🟢 Flutter Build Signing (Release) — Android

**Status:** 🟢 **FULLY AUTOMATABLE** — Android only (iOS ignored per human instruction)

**Available Tools:**
- `keytool` — JDK 21 available
- `flutter` — can run build commands

**What Agent Can Do:**
1. ✅ **Generate Android keystore**:
   ```bash
   keytool -genkey -v -keystore android/app/upload-keystore.jks \
     -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```
2. ✅ **Create `android/key.properties`** with keystore password and alias
3. ✅ **Update `android/app/build.gradle`** with signing config (if not already)
4. ✅ **Test release build**: `flutter build apk --release`

**Action Plan:**
1. ✅ Agent generates keystore with secure passwords
2. ✅ Agent creates key.properties (add to .gitignore)
3. ✅ Agent updates build.gradle for release signing

---

## 13. 🟡 Push Notifications (Android)

**Status:** 🟡 **PARTIALLY AUTOMATABLE** — Firebase MCP available for FCM, iOS ignored

**Available Tools:**
- `firebase` MCP — can manage Firebase project, generate google-services.json
- `npm` — can install `firebase-tools` for Firebase operations
- `flutter` — can add `firebase_messaging` package

**What Agent Can Do:**
1. ✅ **Initialize Firebase project** via Firebase MCP (if Google Cloud project exists)
2. ✅ **Register Android app** in Firebase Console via Firebase MCP
3. ✅ **Download `google-services.json`** via Firebase MCP
4. ✅ **Add `firebase_messaging` dependency** to `pubspec.yaml`
5. ✅ **Write FCM service** in Flutter (push notification handling)
6. ✅ **Add Firebase Admin SDK** to backend `pom.xml` and write send-notification endpoint

**What Needs Human:**
1. 🔴 **Google Cloud project** (same as #3 — OAuth)

**Action Plan:**
1. Ask human for Google Cloud project ID (from task #3)
2. Use Firebase MCP to set up project + services
3. Configure google-services.json in android/app/
4. Write Flutter FCM service code

---

## 14. 🟢 SSL/TLS Certificates & Domain

**Status:** 🟢 **FULLY AUTOMATABLE** — Self-signed possible automatically; Let's Encrypt needs domain DNS

**Available Tools:**
- `openssl` — can generate self-signed certificates
- `docker` — can run `certbot` in container for Let's Encrypt
- `pip` — can install `certbot` for Let's Encrypt

**What Agent Can Do:**
1. ✅ **Generate self-signed cert** for dev: `openssl req -x509 -nodes -days 365 -newkey rsa:2048`
2. ✅ **Write certbot renewal script** for Let's Encrypt auto-renewal
3. ✅ **Update Nginx config** for SSL termination
4. ✅ **Generate CSR** (Certificate Signing Request) for paid SSL vendors

**What Needs Human (for production):**
1. 🔴 **Domain registration** (purchase `ucto.com`)
2. 🔴 **DNS records** (point domain to server IP)

**Action Plan:**
1. ✅ For dev: agent creates self-signed cert → configures nginx
2. For prod: human registers domain + configures DNS → agent runs certbot for Let's Encrypt

---

## 15. 🟢 DeepSeek LLM API Key

**Status:** 🟢 **FULLY CONFIGURED WITH FALLBACK** — StubLLMAgentClient works without it

**Available Tools:**
- `curl` — can test DeepSeek API endpoint

**What Agent Can Do:**
1. ✅ **Verify API endpoint** connectivity with a test curl call (if key provided)
2. ✅ **Configure `application.properties`** (already done)
3. ✅ **Deploy stub fallback** (StubLLMAgentClient already exists)
4. ✅ **Document rate limits and upgrade path**

**Action Plan:**
1. If human provides API key → agent verifies with curl test → updates .env
2. If not provided → system uses StubLLMAgentClient fallback (already coded)

---

## Execution Status

| # | Task | Status | Details |
|---|------|--------|---------|
| 4 | JWT Secret Key | 🟢 **DONE** | Generated via openssl, stored securely |
| 8 | Docker + K8s Manifests | 🟢 **DONE** | Dockerfiles, nginx, K8s YAMLs, GH Actions created |
| 10 | Code Coverage | 🟢 **DONE** | JaCoCo thresholds configured, exclusions set |
| 12a | Android Keystore | 🟢 **DONE** | Keystore + key.properties created, .gitignored |
| 14a | Self-Signed SSL | 🟢 **DONE** | Dev certificate + nginx SSL config created |
| 9 | OTP/SMS (Fast2SMS) | 🟢 **DONE** | Service class with @Primary fix, config, 10-test suite |
| 1 | Chargebee Plans | 🟡 **PENDING** | Needs human: Chargebee account creation |
| 2 | Sentry | 🟡 **PENDING** | Needs human: Sentry account + auth token |
| 3 | Google OAuth | 🟡 **PENDING** | Needs human: Google Cloud project + Client ID |
| 5 | PostgreSQL (cloud) | 🟡 **PENDING** | Needs human: Cloud DB provisioning |
| 6 | Redis (cloud) | 🟡 **PENDING** | Needs human: Cloud Redis provisioning |
| 7 | SMTP Email | 🟡 **PENDING** | Needs human: SMTP password for Zoho |
| 13 | Push Notifications | 🟡 **PENDING** | Needs human: Google Cloud project ID |
| 14b | Domain + Let's Encrypt | 🟡 **PENDING** | Needs human: Domain purchase + DNS config |
| 11 | Load Testing | ⚪ **DEFERRED** | Waits for staging env (depends on #8 deployment) |
| 12b | iOS Signing | ❌ **SKIPPED** | Ignored per human instruction |
