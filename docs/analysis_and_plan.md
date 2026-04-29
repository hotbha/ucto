# UCTO Project - Gap Analysis & Implementation Plan

> Generated: 28-Apr-2026

---

## 1. Documentation Review - Issues & Ambiguities

### CRITICAL ISSUES

| # | Issue | Severity | Details |
|---|-------|----------|---------|
| 1 | **Tech Stack Mismatch** | **CRITICAL** | Docs repeatedly mention **Flutter BLoC architecture** (Developer Agent guideline: "Must use Flutter BLoC architecture - separate blocs, models, UI") but the actual project has a **React (Vite+TS) frontend**. This is a fundamental conflict that must be resolved. Either the docs are wrong (should be React) or the project is wrong (should be Flutter). |
| 2 | **No Agent Communication Pattern** | **MAJOR** | Docs describe agent communication protocols but no implementation mechanism. Is it event bus? Message queue? Direct API calls? WebSocket? No pattern defined. |
| 3 | **Authentication Missing JWT** | **MAJOR** | AuthController returns "Login successful" string with no JWT token. All security is based on token-based auth per BRD. |
| 4 | **Screen Generation AI Unspecified** | **MAJOR** | "Generative AI to create wireframes/mockups/design specs" - no specific AI service/API identified. |
| 5 | **BA Communication Channel** | **MEDIUM** | "Portal chat or email" - is this real-time (WebSocket) or async? No implementation pattern defined. |

### MEDIUM ISSUES

| # | Issue | Severity | Details |
|---|-------|----------|---------|
| 6 | **Role Model Incomplete** | MEDIUM | User entity has only UCTO_ADMIN/CUSTOMER roles. Customer sub-roles (Founder, Developer, Viewer) and Enterprise roles (Product Owner, Finance, Compliance) not modeled. |
| 7 | **Account Collision Handling** | MEDIUM | Linking social accounts with existing email is mentioned but no flow defined. |
| 8 | **No Docker/Deployment Config** | MEDIUM | Docker Compose mentioned for MVP but no Dockerfile or docker-compose.yml exists. |
| 9 | **Cost Tracking Not Implemented** | MEDIUM | "Per agent run, with sprint-based reporting" mentioned in BRD but no implementation pattern. |
| 10 | **Analytics Analytics** | LOW | Multiple analytics tools mentioned (Mixpanel, Amplitude, Prometheus, Grafana, MoEngage, Hevo) - which ones are actually used? |
| 11 | **Duplicate Content in Docs** | LOW | costs.md has duplicate "Portal & CLI Design" section at bottom. README.md contains broken/placeholder content ("[Looks like the result wasn't safe to show...]"). |

---

## 2. RTM Status - Current State

| Requirement | Status | Implementation Status |
|-------------|--------|----------------------|
| Unified Signup/Signin | Approved | **PARTIAL** - Email/password register/login exists, no Google OAuth, no Facebook OAuth, no OTP, no JWT |
| Role-based Access | Approved | **PARTIAL** - Basic User entity with role field, no sub-role enforcement |
| Audit Logs | In Review | **NOT STARTED** |
| Compliance Enforcement | Approved | **NOT STARTED** |
| UI/UX Design | Approved | **NOT STARTED** (default Vite template) |
| Architectural Guidance | Pending | **NOT STARTED** |
| CLI Extension | Pending | **STUB** - Commands registered but no implementation |

---

## 3. Pending Implementation - Detailed Breakdown

### 3.1 BACKEND (Spring Boot)

#### Authentication & Authorization (MVP)
- [ ] **JWT Token Service** - Generate, validate, refresh JWT tokens (/api/auth/login should return JWT)
- [ ] **Google OAuth2 Login** - Implement OAuth2 with Google
- [ ] **Facebook OAuth2 Login** - Implement OAuth2 with Facebook
- [ ] **OTP Auth** - SMS-based OTP generation/verification via Zoho/MSG91
- [ ] **Account Linking** - Handle social+email account collisions
- [ ] **Role Resolution** - Founder, Developer, Viewer role enforcement in security context
- [ ] **CORS Configuration** - Allow frontend origin

#### Project Management
- [ ] **Project Entity** - id, name, description, ownerId, tier, createdAt, updatedAt
- [ ] **Project Service** - CRUD, member management
- [ ] **Project Controller** - REST endpoints
- [ ] **Project Member Entity** - userId, projectId, role (Founder/Developer/Viewer)

#### Requirements Management
- [ ] **Requirement Entity** - id, projectId, title, description, status, createdBy, createdAt
- [ ] **Requirement Service/Controller** - CRUD with BA workflow integration

#### Audit Logging
- [ ] **AuditLog Entity** - id, userId, projectId, action, details, timestamp, ipAddress
- [ ] **AuditLog Service/Controller** - Create, query with filters
- [ ] **Failed attempt logging** - Authentication failure recording

#### Agent System
- [ ] **Agent Entity/Model** - id, name, type (BA/DEVELOPER/TESTER/COMPLIANCE/UX/ARCHITECT), status
- [ ] **Agent Orchestration Service** - Trigger agents, collect responses
- [ ] **Agent Communication Model** - Message passing between agents
- [ ] **Agent Controller** - REST endpoints for agent triggers

#### Screen Management
- [ ] **Screen Entity** - id, projectId, requirementId, type (WIREFRAME/MOCKUP/DESIGN_SPEC), status, storageUrl
- [ ] **Screen Service/Controller** - Upload, approve, reject, comment
- [ ] **Object Storage Integration** - Local/S3 file storage

#### Compliance
- [ ] **Compliance Entity** - id, projectId, checkType, status, findings, createdAt
- [ ] **DPDP/GDPR Validation Logic** - Automated compliance checks
- [ ] **Compliance Controller** - Trigger checks, view results

#### Subscription (Chargebee)
- [ ] **Subscription Entity** - id, userId, tier, status, startDate, endDate
- [ ] **Chargebee Integration** - Webhook handling, subscription sync
- [ ] **Tier Enforcement** - API middleware to enforce limits

#### Integrations
- [ ] **Zoho SMTP Service** - Email sending
- [ ] **Zoho SMS Service** - OTP sending
- [ ] **Razorpay Service** - Payment integration
- [ ] **DigiLocker Service** - Optional KYC

### 3.2 FRONTEND (React)

#### Core Setup
- [ ] **React Router** - Add react-router-dom for navigation
- [ ] **API Client** - Axios setup with interceptors
- [ ] **Auth Context** - Auth state management (JWT storage, user info)
- [ ] **Protected Routes** - Route guards based on auth/role

#### Pages
- [ ] **Login Page** - Google/Facebook OAuth buttons, email/password form, OTP login
- [ ] **Register Page** - Email/password registration with role selection
- [ ] **Dashboard Page** - Status cards (Requirements, Design, Development, Compliance, Deployment)
- [ ] **Project List Page** - View/create/manage projects
- [ ] **Project Detail Page** - Requirements view, agent outputs, traceability
- [ ] **BA Clarification Chat** - Chat interface for BA-customer communication
- [ ] **Screen Preview Page** - Interactive screen viewer with approve/reject/comment
- [ ] **Audit Log Page** - Visual timeline with filters
- [ ] **Subscription Page** - Tier management, billing
- [ ] **Settings Page** - Profile, integrations

#### UI Components
- [ ] **Layout Component** - Header, sidebar, main content area
- [ ] **Status Card** - Requirements/Design/Development/Compliance/Deployment cards
- [ ] **Agent Output Panel** - Display agent results with "What this means" explanation
- [ ] **Screen Viewer** - Interactive screen mockup display
- [ ] **Audit Timeline** - Visual timeline component
- [ ] **Tier Badge** - Show current subscription tier

### 3.3 VS CODE EXTENSION

- [ ] **Project Scaffolding** - `ucto init` creates folder structure + boilerplate
- [ ] **Sprint Execution** - `ucto sprint` triggers agent orchestration
- [ ] **Agent Trigger** - `ucto agent` launches specific agent
- [ ] **Deploy Flow** - `ucto deploy` containerizes and deploys
- [ ] **Traceability Panel** - Side panel showing audit logs + outputs
- [ ] **CI/CD Hooks** - GitHub Actions YAML generation

### 3.4 INFRASTRUCTURE

- [ ] **Dockerfile** - For backend (Spring Boot)
- [ ] **Dockerfile** - For frontend (React/Vite nginx)
- [ ] **docker-compose.yml** - PostgreSQL, Redis, Backend, Frontend
- [ ] **NGINX Config** - Reverse proxy for production

---

## 4. Implementation Plan - Phased Approach

### Phase 1: Core Backend Foundation
1. Fix JWT authentication (replace string response with JWT)
2. Complete OAuth2 Google/Facebook integration
3. Add CORS configuration
4. Implement Project entity + CRUD
5. Implement AuditLog entity + service
6. Add Customer sub-roles (Founder, Developer, Viewer)

### Phase 2: Frontend Foundation
1. Install react-router-dom, axios
2. Build Login/Register pages with OAuth buttons
3. Build Dashboard page with status cards
4. Build Project list + detail pages
5. Implement auth context + protected routes

### Phase 3: Agent System & Screen Workflow
1. Build agent orchestration service
2. Implement screen entity + upload/view/approve
3. Build BA clarification interface
4. Build screen preview/approval page
5. Implement compliance check logic

### Phase 4: Subscriptions & Integrations
1. Chargebee integration
2. Zoho SMTP/SMS
3. Razorpay integration
4. Tier-based access control
5. Audit log viewer UI

### Phase 5: VS Code Extension & Infrastructure
1. Implement extension commands properly
2. Add traceability panel
3. CI/CD hooks
4. Docker configuration
5. docker-compose.yml

### Phase 6: Polish & Refinements
1. Analytics integration
2. Localization hooks
3. Performance optimization (Redis caching)
4. Test coverage
5. Documentation cleanup (fix duplicates, placeholders)

---

## 5. Priority Matrix

| Priority | Items | Rationale |
|----------|-------|-----------|
| **P0 - Critical** | JWT auth, OAuth2, CORS | Without auth, nothing works |
| **P0 - Critical** | Role model (Founder/Developer/Viewer) | Core access control |
| **P1 - High** | Project CRUD, Dashboard UI | First user-facing functionality |
| **P1 - High** | Audit Logging | Compliance requirement |
| **P2 - Medium** | Agent System (BA, Developer, etc.) | Core value proposition |
| **P2 - Medium** | Screen Workflow | Screen-first development |
| **P3 - Low** | Chargebee Subscriptions | Needed before monetization |
| **P3 - Low** | VS Code Extension | Secondary access method |
| **P4 - Optional** | Analytics, Localization | Future refinements |

---

## 6. Documentation Cleanup Needed

- [ ] **README.md** - Fix placeholder text ("[Looks like the result wasn't safe to show...]")
- [ ] **costs.md** - Remove duplicate "Portal & CLI Design" section at bottom
- [ ] **Document Tech Stack Decision** - Clarify React (not Flutter) as the frontend framework, OR decide to switch to Flutter
- [ ] **system_architecture.md** - Add explicit agent communication pattern (e.g., RabbitMQ/event bus)
- [ ] **Add deprecation notice** if React was replaced from original Flutter plan

---

*This analysis was generated by reviewing all docs/ files and the current codebase state.*
