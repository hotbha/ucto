# Prompt-to-App Bootstrap Design

> **Purpose:** Define the "prompt‑to‑app" bootstrap flow for creating new full‑stack projects from a single user prompt, using the multi‑agent orchestration pipeline.
> **Status:** Draft | **Last Updated:** 2026-05-13

---

## 1. Overview

The prompt‑to‑app bootstrap flow allows a user to describe a new project idea in natural language. The UCTO multi‑agent system then:

1. Clarifies requirements (BA agent)
2. Designs architecture (Architect agent)
3. Generates a starter full‑stack repo (Developer agent)
4. Registers the new project in UCTO with a linked repository

The result is a **fully initialized UCTO project** with a working code skeleton that implements the described idea.

---

## 2. Flow Stages

```
User Prompt → BA Agent → Architect Agent → Developer Agent → Project Created
                                                                      ↓
                                                              Repo linked + code pushed
```

### Stage 1: User Prompt

The user submits a prompt via the UCTO frontend (or API):

```json
{
  "prompt": "A task management app with team collaboration, real-time updates, and a Spring Boot + React + PostgreSQL stack.",
  "projectTitle": "Task Manager Pro",
  "stack": "spring-boot-react-postgres"
}
```

**Supported stack identifiers (MVP):**

| Identifier | Backend | Frontend | Database |
|------------|---------|----------|----------|
| `spring-boot-react-postgres` | Spring Boot (Java 21+) | React (TypeScript) | PostgreSQL |

### Stage 2: BA Agent — Requirements Clarification

1. `OrchestratorController` receives the prompt.
2. Publishes `agent.ba.trigger` with:
   - `data.action: "clarify_bootstrap"`
   - `data.prompt`: The raw user prompt.
   - `data.stack`: Requested technology stack.
3. BA agent processes the prompt and generates:
   - Structured epic/story breakdown.
   - Functional requirements.
   - Non‑functional requirements.
4. BA publishes `agent.ba.complete` with:
   - `data.requirementIds`
   - `data.summary`
   - `data.nextAction: "architecture_design"`

### Stage 3: Architect Agent — Architecture Design

1. `AgentEventListener` receives `agent.ba.complete` and publishes `agent.architect.trigger`.
2. Architect agent produces:
   - Project structure (packages, modules).
   - API endpoints / data model.
   - Component tree (frontend).
   - Technology-specific choices (ORM, auth, testing).
3. Architect publishes `agent.architect.complete` with:
   - `data.architectureSpec`: JSON structure describing the project layout.
   - `data.nextAction: "skeleton_generation"`

### Stage 4: Developer Agent — Skeleton Generation

1. `AgentEventListener` receives `agent.architect.complete` and publishes `agent.developer.trigger` with:
   - `data.action: "generate_skeleton"`
   - `data.architectureSpec`: From architect.
   - `data.requirementIds`: From BA.
2. Developer agent generates the complete project skeleton:
   - Backend: Spring Boot project with `pom.xml`, application entry, config, domain entities, REST controllers, service stubs, JPA repositories, `application.properties`.
   - Frontend: React + TypeScript project with `package.json`, Vite config, React Router setup, API client stub, component shell, tests scaffolding.
   - Database: SQL migration scripts (Flyway for Spring Boot).
3. Developer agent publishes `agent.developer.complete` with:
   - `data.filesGenerated`: List of created file paths.
   - `data.skeletonPath`: Local workspace path.
   - `data.nextAction: "repo_push"`

### Stage 5: Project Registration & Repo Push

1. `AgentEventListener` detects `agent.developer.complete` with `generate_skeleton` action.
2. System creates a new UCTO project (`POST /api/projects`):
   - Title from user prompt.
   - Tier from user's subscription.
   - Status set to `DRAFT`.
3. System creates a Git repository (if supported provider) or links to an existing one.
4. Developer agent pushes the generated skeleton:
   - Initializes Git in the workspace.
   - Creates initial commit.
   - Pushes to `main` branch.
5. Project is registered with `repo_url` pointing to the new repository.

---

## 3. Event Flow & Topics

### 3.1 Full Topic Sequence

```
1. api_request (ingress from user)
   → agent.ba.trigger
   
2. agent.ba.complete
   → agent.architect.trigger

3. agent.architect.complete
   → agent.developer.trigger

4. agent.developer.complete (skeleton generated)
   → ProjectService.createProject()
   → RepoWorkspaceService.prepareWorkspace()
   → (git init + push)
   → ProjectService.updateRepoConfig()
```

### 3.2 New Topics Required

No new topics. The existing `agent.ba.*`, `agent.architect.*`, and `agent.developer.*` topics are used. The new logic lives in:

- `AgentEventListener.handleAgentComplete()` — extended to detect `generate_skeleton` and `skeleton_generation` next actions.
- `ProjectController` / `ProjectService` — reused for project creation.

---

## 4. Skeleton Outputs by Stack

### 4.1 Spring Boot + React + PostgreSQL

**Backend structure:**

```
backend/
  pom.xml
  src/main/java/com/{domain}/
    {Project}Application.java
    config/
      SecurityConfig.java
      CorsConfig.java
    controller/
      HealthController.java
      {Entity}Controller.java
    entity/
      {Entity}.java
    repository/
      {Entity}Repository.java
    service/
      {Entity}Service.java
    dto/
      {Entity}Request.java
      {Entity}Response.java
  src/main/resources/
    application.properties
    application-dev.properties
    application-prod.properties
    db/migration/
      V1__init.sql
  src/test/java/com/{domain}/
    {Project}ApplicationTests.java
```

**Frontend structure:**

```
frontend/
  package.json
  tsconfig.json
  vite.config.ts
  index.html
  src/
    main.tsx
    App.tsx
    api/
      client.ts
    components/
      Layout.tsx
    pages/
      Home.tsx
    hooks/
      useApi.ts
    types/
      index.ts
  tests/
    App.test.tsx
```

**Database migrations (Flyway):**

```sql
-- V1__init.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Additional tables based on requirements...
```

---

## 5. Project Registration in UCTO

After skeleton generation, the bootstrap flow creates a UCTO project and links the repo:

| Step | Action | Service/Component |
|------|--------|-------------------|
| 1 | Create UCTO project record | `ProjectService.createProject()` |
| 2 | Set `repoUrl`, `repoProvider`, `repoBranch` | `ProjectService.updateProject()` (extended) |
| 3 | Create workspace | `RepoWorkspaceService.prepareWorkspace()` |
| 4 | Initialize Git + commit + push | `RepoWorkspaceService` |
| 5 | Set project status to `DESIGN` | `ProjectService.updateProject()` |

The project is now visible in the UCTO dashboard with status `DESIGN` and a linked repository ready for iterative development.

---

## 6. Non‑Goals / Later Phases

| Feature | Phase | Reason |
|---------|-------|--------|
| Next.js frontend | Phase 2 | React (Vite) sufficient for MVP; Next.js adds SSR complexity |
| Flutter mobile | Phase 2 | UCTO frontend is already Flutter; bootstrap targets full‑stack web apps |
| Python (Django/FastAPI) backend | Phase 3 | Python ecosystem not in current stack |
| AWS/GCP deployment | Phase 2 | Bootstrap generates code only; CI/CD pipeline is separate |
| Docker Compose generation | Phase 2 | Could auto‑generate `docker-compose.yml` for the skeleton |
| AI‑generated test data | Phase 2 | Seed SQL scripts with realistic data |
| Custom project templates | Phase 3 | User‑saved skeleton templates for reuse |
| Monorepo setup | Phase 3 | All skeletons are separate repos in MVP |

---

## 7. Configuration Flags

```properties
# Enable/disable prompt-to-app bootstrap flow
ucto.bootstrap.enabled=true

# Default stack (used if user doesn't specify)
ucto.bootstrap.default-stack=spring-boot-react-postgres

# Workspace directory for skeleton generation
ucto.bootstrap.workspace-base=/ucto/bootstrap-skeletons

# Auto-push generated skeleton to linked repo
ucto.bootstrap.auto-push=true

# If true, keep local skeleton after push (for debugging)
ucto.bootstrap.keep-local-copy=false
```

---

## 8. Implementation Checklist

1. Extend `AgentEventListener.handleAgentComplete()` to route `skeleton_generation` nextAction from architect → developer.
2. Create `SkeletonGeneratorService` that produces the Spring Boot + React + PostgreSQL project structure as files on disk.
3. Extend `RepoWorkspaceService` with `git init` and `git push` support for new repos.
4. Add `SKELETON_GENERATED` and `REPO_INIT` audit actions.
5. Add `POST /api/projects/bootstrap` endpoint to `OrchestratorController` that accepts the user prompt and kicks off `agent.ba.trigger`.
6. Wire the end‑to‑end integration test.

[← Back to README](README.md) | Related: [agent_orchestration_design.md](agent_orchestration_design.md), [repo_aware_dev_agent_design.md](repo_aware_dev_agent_design.md)
