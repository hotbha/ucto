# Deployment Readiness Plan - First 10 Customers

## Executive Summary
Goal: Onboard first 10 paying customers with a "Google-level" experience while maintaining sustainable margins.
Core thesis: Founders need to go from idea → screens → code in hours, not months.
Sales funnel: Free (hooked) → Startup ($29, converted) → Growth ($99, expanded) → Enterprise ($499+, retained)

---

## 1. Pricing & Sales Funnel Strategy

### Subscription Tiers (Designed for Conversion)

| Tier | Price (INR) | Price (USD) | Projects | Agent Runs/Month | Key Features | Conversion Hook |
|------|------------|------------|----------|-----------------|--------------|-----------------|
| **Free** | ₹0 | $0 | 1 | 5 | BA Agent + Screen generation + 1 deploy | **Enough to build 1 full MVP** |
| **Startup** | ₹2,499 | $29 | 5 | 50 | All agents + Audit + Email support | **Build unlimited MVPs, perfect for testing** |
| **Growth** | ₹7,999 | $99 | 50 | 200 | Compliance + Priority support + Custom domain | **Launch to production** |
| **Enterprise** | Custom | $499+ | Unlimited | Unlimited | Dedicated infra + SLA + Custom integrations | **Scale with confidence** |

### Conversion Mechanics
1. **Free → Startup**: After 5 runs, show "You've used all runs. Upgrade to continue building."
2. **Startup → Growth**: When project needs compliance (DPDP/GDPR), prompt upgrade.
3. **Growth → Enterprise**: Custom integration request triggers sales conversation.
4. **Time-limited trials**: 14-day Startup trial on signup (no credit card needed), then auto-downgrade to Free.
5. **Annual discount**: 20% off annual billing to improve retention.

### Margin Analysis
| Tier | Revenue/Month | Est. Cost/Month | Gross Margin | Notes |
|------|--------------|----------------|-------------|-------|
| Free | $0 | $2 | Negative | Customer acquisition cost (OK for funnel top) |
| Startup ($29) | $290 (10 users) | ~$100 (LLM + infra) | **65%** | Core profit center at scale |
| Growth ($99) | $990 (10 users) | ~$200 | **80%** | High margin, sticky product |
| Enterprise ($499) | $4,990 (10 users) | ~$500 | **90%** | White glove, low churn |

---

## 2. Customer Journey & Onboarding

### Step-by-step Onboarding Flow
```
Signup (Google OAuth, 5s)
  ↓
Welcome Screen ("Build your first app in 3 steps")
  ↓
Step 1: Create Project (name + description, 30s)
  ↓
Step 2: Describe Requirements (guided prompt OR free text)
  ↓
BA Agent Interrogates (AI clarifies requirements)
  ↓
Step 3: See Generated Screens (preview + approve/reject)
  ↓
Dashboard (project status, "Upgrade for more" prompt)
  ↓
[FREE TIER] 5 runs used → "Upgrade to Startup for unlimited projects"
```

### Conversion Touchpoints
| Stage | Action | Conversion Opportunity |
|-------|--------|----------------------|
| After 1st project created | "Want to add team members? Upgrade to Startup" | Social proof / collaboration |
| After 3 agent runs | "You've used 60% of your runs" + Upgrade CTA | Scarcity |
| After all runs used | "Unlock unlimited runs for $29/mo" | Pain point |
| On deploy success | "Deploy to custom domain? Upgrade to Growth" | Aspiration |
| 7-day email sequence | "Here's what else you can build..." | Re-engagement |

---

## 3. Implementation Gaps (Technical)

### Backend Gaps
| Component | Status | Priority | Effort |
|-----------|--------|----------|--------|
| JWT Auth + OAuth2 (Google) | ✅ Done | P0 | Done |
| User/Project CRUD | ✅ Done | P0 | Done |
| Audit Logging | ✅ Done (entity + controller + service) | P1 | Done |
| **Subscription/Chargebee** | ✅ Done (entity + service + Chargebee webhook handler) | P0 | Done |
| **Usage Metering** | ✅ Done (AgentRun tracking, monthly counters) | P0 | Done |
| **Tier Enforcement** | ✅ Done (canRunAgent, canCreateProject limits) | P0 | Done |
| **Agent Orchestration Service** | ✅ Done (Redis Pub/Sub + pipeline routing BA→Developer→Tester→Compliance) | P1 | Done |
| **Email Service (Zoho)** | ✅ Done (ZohoEmailService implementation) | P1 | Done |
| **SMS Service (MSG91)** | ⏸️ Deferred to Phase 2 (ConsoleSmsService stub exists for testing) | P2 | Deferred |
| **Razorpay Integration** | ⏸️ Deferred to Phase 2 (Chargebee handles payments) | P2 | Deferred |
| **Password Reset** | ⏸️ Deferred to Phase 2 (endpoint not yet implemented) | P2 | Deferred |
| **Email Verification** | ⏸️ Deferred to Phase 2 (User entity has emailVerified field, flow not wired) | P2 | Deferred |
| **Facebook OAuth** | ⏸️ Deferred to Phase 2 | P2 | Deferred |

### Frontend Gaps (Flutter Web MVP)
| Component | Status | Priority | Effort |
|-----------|--------|----------|--------|
| **Landing Page** | ❌ Missing (Flutter Web app starts at SplashScreen) | P1 | 4h |
| **Auth UI (Login/Register)** | ✅ Done (LoginScreen + RegisterScreen with email/password, Google OAuth, OTP flow) | P0 | Done |
| **Google OAuth Button** | ✅ Done (Social button on LoginScreen, wired to AuthBloc) | P0 | Done |
| **Dashboard** | ✅ Done (DashboardScreen with subscription banner, quick actions, project list) | P0 | Done |
| **Project Management** | ✅ Done (Create/edit/delete projects via DashboardScreen + ProjectDetailScreen) | P0 | Done |
| **Requirements Input** | ✅ Done (RequirementsTab in ProjectDetailScreen with add dialog) | P0 | Done |
| **Screen Preview** | ❌ Missing (ScreensTab shows placeholder, no actual screen preview) | P1 | 4h |
| **Subscription/Pricing** | ✅ Done (PricingScreen with plan comparison + upgrade flow) | P0 | Done |
| **Upgrade Flow** | ✅ Done (PricingScreen triggers subscription creation, upgrade CTA in Dashboard) | P0 | Done |
| **Onboarding Wizard** | ✅ Done (OnboardingScreen with 3-step carousel) | P0 | Done |
| **Audit Log Viewer** | ✅ Done (AuditLogsScreen with filterable timeline) | P2 | Done |
| **Help/Tooltips** | ❌ Missing | P2 | 2h |

### Infrastructure Gaps
| Component | Status | Priority | Effort |
|-----------|--------|----------|--------|
| Docker Compose | ✅ Done | P0 | Done |
| Dockerfiles | ✅ Done | P0 | Done |
| **Domain Config (ucto.app)** | ❌ Missing | P0 | 1h |
| **SSL (Let's Encrypt)** | ❌ Missing | P0 | 1h |
| **Production CORS** | ❌ Missing | P0 | 0.5h |
| **Database Backup** | ❌ Missing | P1 | 1h |
| **Monitoring (Sentry)** | ❌ Missing | P1 | 1h |
| **Error Logging** | ❌ Missing | P1 | 1h |
| **Rate Limiting** | ❌ Missing | P1 | 2h |

---

## 4. Implementation Plan (MVP Launch)

### Phase 0: Immediate (Day 1-2)
- [ ] Fix JWT token issuance in AuthController
- [ ] Fix CORS for production domains
- [ ] Deploy backend to VPS with Docker Compose
- [ ] Configure PostgreSQL + Redis in production
- [ ] Set up domain + SSL (Caddy or Let's Encrypt)
- [ ] Add Sentry error tracking

### Phase 1: Core Portal (Day 3-5) — Flutter Web
- [ ] Build landing page (value proposition + pricing)
- [ ] Implement auth UI (Google OAuth + email/password)
- [ ] Build dashboard + project management
- [ ] Implement subscription/pricing page with Chargebee
- [ ] Add onboarding wizard

### Phase 2: Agent Integration (Day 6-8)
- [ ] Build requirements controller + input UI
- [ ] Wire BA agent for clarification (LLM-powered via Redis Pub/Sub)
- [ ] Screen generation + preview UI
- [ ] Usage metering + tier enforcement

### Phase 3: Polish & Launch (Day 9-10)
- [ ] Email verification flow
- [ ] Password reset
- [ ] Help text / tooltips throughout
- [ ] Beta launch for 10 customers
- [ ] Monitor + iterate on conversion

---

## 5. Tech Stack for Frontend

The canonical frontend is **Flutter + BLoC (Dart)** targeting **Flutter Web for MVP**:
- **Flutter Web** (NOT React — React artifacts are deprecated)
- **BLoC architecture**: separate `blocs/`, `models/`, `ui/` directories
- **Shared BLoC code**: same codebase will power mobile (iOS/Android) in Phase 2
- **Dart HTTP client**: for API calls with JWT interceptor

> ⚠️ React artifacts (`package.json`, `vite.config.ts`, `tsconfig*.json`) are orphan/experimental files from an earlier exploration. They are NOT the MVP frontend.

---

## 6. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| LLM API costs exceed margins | Medium | High | Cache responses, use cheaper models for simple tasks, set hard limits |
| Customers churn after free tier | High | Medium | 14-day trial with credit card; email drip campaigns; show value early |
| Tech stack confusion (React vs Flutter) | Medium | Medium | React artifacts documented as deprecated. All docs standardize on Flutter Web. |
| OAuth token validation fails | Low | High | Validate tokens server-side; handle edge cases gracefully |
| Chargebee integration breaks payments | Low | Medium | Test thoroughly before beta launch |
