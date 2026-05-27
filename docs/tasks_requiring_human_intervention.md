# Tasks Requiring Human Intervention / External Credentials / Infrastructure

> **Document Purpose:** Track all configuration and setup tasks that require a human administrator, third-party account creation, or external infrastructure provisioning. These cannot be automated or coded by an AI agent.

> **Generated:** May 8, 2026  
> **Status:** Pending human action

---

## Table of Contents

1. [Chargebee Subscription & Billing](#1-chargebee-subscription--billing)
2. [Sentry Error Monitoring](#2-sentry-error-monitoring)
3. [Google OAuth Credentials](#3-google-oauth-credentials)
4. [JWT Secret Key (Production)](#4-jwt-secret-key-production)
5. [Database Credentials (Production/Staging)](#5-database-credentials-productionstaging)
6. [Redis Configuration (Production)](#6-redis-configuration-production)
7. [Email/SMTP Server Configuration](#7-emailsmtp-server-configuration)
8. [Deployment Infrastructure](#8-deployment-infrastructure)
9. [OTP/SMS Service](#9-otpsms-service)
10. [Code Coverage Tool Configuration](#10-code-coverage-tool-configuration)
11. [Performance / Load Testing Infrastructure](#11-performance--load-testing-infrastructure)
12. [Flutter Build Signing (Release)](#12-flutter-build-signing-release)
13. [Android / iOS Push Notification Configuration](#13-android--ios-push-notification-configuration)
14. [SSL/TLS Certificates & Domain](#14-ssltls-certificates--domain)

---

## 1. Chargebee Subscription & Billing

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Chargebee Site Creation** | Register a Chargebee account and create a site (e.g., `ucto.chargebee.com`) | 15 min | P0 |
| **API Key Generation** | Generate a Publishable Key and Secret Key from Chargebee Settings → API Keys | 5 min | P0 |
| **Webhook Configuration** | Register webhook endpoint URL (e.g., `https://api.ucto.com/api/webhooks/chargebee`) with events: `subscription_created`, `subscription_cancelled`, `subscription_renewed`, `payment_failed`, `payment_succeeded` | 10 min | P0 |
| **Pricing Plans Creation** | Create 4 plans in Chargebee catalog:
- **FREE** ($0/mo, 1 project, 5 agent runs)
- **STARTUP** ($29/mo, 5 projects, 50 agent runs)
- **GROWTH** ($99/mo, 20 projects, 200 agent runs)
- **ENTERPRISE** ($299/mo, unlimited projects, unlimited runs)
- **STARTUP_TRIAL** (14-day free trial, same limits as STARTUP) | 30 min | P0 |
| **Webhook Signing Secret** | Copy webhook signing secret for webhook signature verification | 5 min | P0 |
| **Tax & Invoice Settings** | Configure tax rates, invoice generation, currency (USD/INR) | 20 min | P2 |
| **Test Mode vs Live Mode** | Start in Chargebee Test Mode for development; switch to Live Mode for production | Ongoing | P1 |

### Configuration Values Needed

```properties
# application.properties / environment variables
CHARGEBEE_SITE=ucto
CHARGEBEE_API_KEY=live_xxxxxxxxxxxxxxxxxxxx
CHARGEBEE_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxxxxxxxxx
```

---

## 2. Sentry Error Monitoring

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Sentry Account** | Register a Sentry.io account (or self-hosted Sentry instance) | 10 min | P1 |
| **Sentry Project Creation** | Create a new project → select Spring Boot → copy DSN | 5 min | P1 |
| **Sentry DSN Configuration** | Set `SENTRY_DSN` environment variable or `sentry.dsn` property | 2 min | P1 |
| **Release Tracking** | Configure release name/version for source map uploads | 10 min | P2 |
| **Alert Rules** | Configure alert rules (e.g., notify on 500 errors > 5/min) | 15 min | P2 |

### Configuration Values Needed

```properties
sentry.dsn=https://xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx@oXXXXXX.ingest.sentry.io/XXXXXX
```

---

## 3. Google OAuth Credentials

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Google Cloud Project** | Create a Google Cloud Console project (or use existing) | 10 min | P0 |
| **OAuth Consent Screen** | Configure OAuth consent screen (User Type: External, App name: "Unicornator UCTO", support email, authorized domains) | 20 min | P0 |
| **OAuth 2.0 Client ID** | Create OAuth 2.0 Web Client credentials:
- Authorized JavaScript origins: `https://ucto.com`, `http://localhost:3000`
- Authorized redirect URIs: `https://ucto.com/api/auth/oauth/callback`, `http://localhost:8080/api/auth/oauth/callback` | 10 min | P0 |
| **Client Secret** | Copy Client ID and Client Secret securely | 2 min | P0 |
| **Flutter Web OAuth** | For Flutter Web, configure the same Client ID in frontend OAuth setup | 5 min | P0 |

### Configuration Values Needed

```properties
spring.security.oauth2.client.registration.google.client-id=XXXXXX.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=GOCSPX-XXXXXXXXXXXX
```

---

## 4. JWT Secret Key (Production)

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Generate Strong Secret** | Generate a cryptographically secure 256-bit+ secret: `openssl rand -base64 64` | 1 min | P0 |
| **Secure Storage** | Store in environment variable, not in source code. Options:
- GitHub Actions Secret
- Kubernetes Secret
- AWS Secrets Manager / GCP Secret Manager
- HashiCorp Vault | 15 min | P0 |
| **Key Rotation Policy** | Document rotation schedule (every 90 days recommended) | 10 min | P2 |

### Configuration Values Needed

```properties
jwt.secret=<base64-encoded-256-bit-secret>
```

---

## 5. Database Credentials (Production/Staging)

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **PostgreSQL Instance** | Provision PostgreSQL (options):
- AWS RDS / GCP Cloud SQL / Azure Database
- Self-hosted on VM
- Managed: Supabase, Render, Railway | 30 min | P0 |
| **Database Creation** | Create database: `CREATE DATABASE ucto;` | 5 min | P0 |
| **User & Password** | Create user with strong password, grant access to `ucto` database | 10 min | P0 |
| **SSL/TLS Connection** | Download CA certificate, configure SSL mode | 10 min | P1 |
| **Connection Pool Settings** | Tune HikariCP pool size, max connections, timeout | 10 min | P2 |
| **Backup Strategy** | Configure automated daily backups + 7-day retention | 20 min | P1 |

### Configuration Values Needed

```properties
spring.datasource.url=jdbc:postgresql://<host>:5432/ucto?ssl=true&sslmode=require
spring.datasource.username=ucto_app
spring.datasource.password=<strong-password>
```

---

## 6. Redis Configuration (Production)

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Redis Instance** | Provision Redis (options):
- AWS ElastiCache / GCP Memorystore / Azure Cache
- Self-hosted on VM
- Managed: Redis Labs, Upstash | 20 min | P0 |
| **Connection Details** | Get host, port, password | 5 min | P0 |
| **TLS Configuration** | Enable TLS for Redis connection if using managed service | 10 min | P1 |
| **Max Memory Policy** | Set maxmemory-policy to `allkeys-lru` or `volatile-lru` | 5 min | P1 |

### Configuration Values Needed

```properties
spring.redis.host=<redis-host>
spring.redis.port=6379
spring.redis.password=<redis-password>
spring.redis.ssl=true
```

---

## 7. Email / SMTP Server Configuration

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Email Provider** | Choose provider (options):
- AWS SES
- SendGrid
- Mailgun
- Gmail SMTP (dev only)
- SMTP2GO | 15 min | P1 |
| **API Key / SMTP Credentials** | Obtain SMTP username, password, or API key | 5 min | P1 |
| **Verified Sender** | Verify sender email address (e.g., `noreply@ucto.com`) | 10 min | P1 |
| **Email Templates** | Create/configure templates for:
- Password reset email
- Account verification email
- Welcome email | 1 hr | P1 |

### Configuration Values Needed

```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=<sendgrid-api-key>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 8. Deployment Infrastructure

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Container Registry** | Set up Docker registry:
- Docker Hub
- AWS ECR
- GCP Artifact Registry
- GitHub Container Registry | 15 min | P0 |
| **Cloud Provider Account** | Choose and provision:
- AWS (ECS/EKS/EC2)
- GCP (Cloud Run/GKE)
- Azure (AKS/App Service)
- Dedicated VPS (DigitalOcean, Linode) | 1 hr | P0 |
| **GitHub Actions Secrets** | Add to repository Settings → Secrets and variables → Actions:
- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`
- `SSH_PRIVATE_KEY`
- `HOST`
- `ENV_FILE` | 15 min | P0 |
| **Kubernetes Cluster** | If using K8s, provision cluster and create:
- Deployment YAMLs (already written)
- Service YAMLs
- Ingress Controller
- Secret resources | 2 hr | P1 |
| **Docker Compose (Dev)** | For single-server deployment, configure `docker-compose.yml` with:
- Postgres service
- Redis service
- Backend service (with env vars)
- Frontend service (via nginx) | 30 min | P1 |
| **Nginx Configuration** | Review/customize the existing `nginx.conf` for:
- SSL termination
- Rate limiting
- Static file caching
- Reverse proxy to backend | 30 min | P1 |

### Required Credentials

```
DOCKER_USERNAME=your_dockerhub_username
DOCKER_PASSWORD=your_dockerhub_token_or_password
SSH_PRIVATE_KEY=-----BEGIN OPENSSH PRIVATE KEY-----\n...
HOST=your-server-ip-or-domain.com
```

---

## 9. OTP / SMS Service

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **SMS Provider** | Choose provider:
- Twilio
- AWS SNS
- Vonage (Nexmo)
- MSG91 | 15 min | P2 |
| **API Credentials** | Get Account SID, Auth Token (Twilio) or API Key | 5 min | P2 |
| **Phone Number** | Purchase SMS-capable phone number (e.g., Twilio number) | 10 min | P2 |
| **OTP Template Message** | Configure OTP message template: "Your UCTO verification code is: {code}" | 5 min | P2 |

### Configuration Values Needed

```properties
twilio.account-sid=ACxxxxxxxxxxxxxxxxxxxx
twilio.auth-token=xxxxxxxxxxxxxxxxxxxx
twilio.phone-number=+1XXXXXXXXXX
```

---

## 10. Code Coverage Tool Configuration

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **JaCoCo in pom.xml** | Already added by automation (pending verification). | — | P2 |
| **Coverage Thresholds** | **Human decision required:** Set minimum coverage thresholds:
- Instruction coverage: ___%
- Branch coverage: ___%
- Line coverage: ___%
- Method coverage: ___% | 10 min | P2 |
| **Exclusion Patterns** | Identify patterns to exclude from coverage:
- DTO classes?
- Entity classes?
- Configuration classes? | 10 min | P2 |
| **CI Integration** | Decide whether coverage check should FAIL the build or only WARN | 5 min | P2 |
| **Coverage Badge** | Configure Codecov or Coveralls badge for README | 15 min | P3 |

---

## 11. Performance / Load Testing Infrastructure

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Test Environment** | Need a deployed staging environment first (depends on #8) | — | P2 |
| **Load Testing Tool** | Choose tool:
- Apache JMeter (free, Java-based)
- k6 (scriptable, JavaScript)
- Locust (Python-based)
- Gatling (Scala-based) | 30 min | P2 |
| **Test Scripts** | Write test scenarios (can be automated for some, but manual tuning needed):
- Concurrent user login
- Concurrent agent triggers
- Large project listing | 2 hr | P2 |
| **Thresholds Definition** | Define acceptable performance thresholds:
- P95 response time < 500ms for API
- P95 response time < 2s for agent trigger
- Throughput > 100 req/s | 15 min | P2 |

---

## 12. Flutter Build Signing (Release)

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Android Keystore** | Generate or use existing keystore:
```bash
keytool -genkey -v -keystore upload-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```
Store in `android/app/upload-keystore.jks` (DO NOT commit to git) | 15 min | P0 |
| **key.properties** | Create `android/key.properties`:
```properties
storePassword=<password>
keyPassword=<password>
keyAlias=upload
storeFile=upload-keystore.jks
``` | 5 min | P0 |
| **iOS Signing** | Need Apple Developer Account ($99/year) + Distribution Certificate + Provisioning Profile | 1 hr | P1 |
| **Flutter Build Commands** | Test builds:
- `flutter build apk --release`
- `flutter build appbundle --release`
- `flutter build web --release` | 15 min | P1 |

---

## 13. Android / iOS Push Notification Configuration

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Firebase Project** | Create Firebase project in Firebase Console | 10 min | P2 |
| **FCM Server Key** | Obtain FCM server key (for backend) | 5 min | P2 |
| **google-services.json** | Download and place in `android/app/` | 5 min | P2 |
| **iOS APNs** | For iOS push: need Apple Developer Account + APNs certificate/key | 30 min | P3 |

---

## 14. SSL/TLS Certificates & Domain

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **Domain Registration** | Register domain name (e.g., `ucto.com`, `app.ucto.com`) | 15 min | P0 |
| **DNS Configuration** | Set A/AAAA records pointing to server IP or CNAME to cloud load balancer | 15 min | P0 |
| **SSL Certificate** | Options:
- Let's Encrypt (free, auto-renew via certbot)
- Cloudflare (free, managed SSL)
- AWS Certificate Manager (free for ALB/CloudFront)
- Paid: DigiCert, Sectigo | 30 min | P0 |
| **Auto-Renewal** | Set up cron job or cloud auto-renewal for Let's Encrypt | 10 min | P1 |

---

## 15. DeepSeek LLM API Key

| Item | Details | Effort | Priority |
|------|---------|--------|----------|
| **DeepSeek API Key** | Obtain from platform.deepseek.com after creating an account. Set as DEEPSEEK_API_KEY in .env | 5 min | P1 |
| **Fallback Behavior** | If unset, StubLLMAgentClient returns placeholder JSON. All simulated sprints work without it | — | — |
| **Rate Limits** | DeepSeek free tier ~60 calls/min. For production, upgrade to paid tier | 5 min | P2 |

### Config
```properties
ucto.llm.deepseek.api-key=${DEEPSEEK_API_KEY:}
ucto.llm.deepseek.model=deepseek-chat
ucto.llm.deepseek.timeout-ms=30000
ucto.llm.deepseek.max-tokens=2048
ucto.llm.deepseek.temperature=0.3
```

---

## Summary Checklist

| # | Task | Required Before Launch | Done |
|---|------|------------------------|------|
| | 1 | Chargebee site + API keys | ✅ Yes | 🟡 PENDING (manual) |
| | 2 | Sentry DSN | ❌ No | 🟢 **DONE** |
| | 3 | Google OAuth Client ID | ✅ Yes | 🟡 PENDING (manual) |
| | 4 | JWT Secret Key | ✅ Yes | 🟢 DONE |
| | 5 | PostgreSQL credentials | ✅ Yes | 🟡 PENDING (manual) |
| | 6 | Redis credentials | ✅ Yes | 🟢 Can run via Docker |
| | 7 | SMTP credentials | ⚠️ For password reset | 🟡 PENDING (manual) |
| | 8 | Docker registry + cloud deploy | ✅ Yes | 🟢 Configs generated |
| | 9 | SMS provider (Fast2SMS OTP) | ❌ No (P2) | 🟢 **DONE** ❗ |
| | 10 | Code coverage thresholds | ❌ No | 🟢 DONE |
| | 11 | Load test infrastructure | ❌ No | ⚪ DEFERRED |
| | 12 | Android keystore / iOS signing | ⚠️ For mobile | 🟢 Android done |
| | 13 | Push notifications | ❌ No | 🟡 PENDING (manual) |
| | 14 | Domain + SSL certificate | ✅ Yes | 🟢 Dev SSL done |
| | **15** | **DeepSeek API Key** | ❌ **No (stub fallback)** | 🟢 Fallback configured |
