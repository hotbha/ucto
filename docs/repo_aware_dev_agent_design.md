# Repo-Aware Developer Agent Design

> **Purpose:** Define how the developer agent becomes repo-aware — linking projects to Git repositories, cloning workspaces, and executing code changes via Redis Pub/Sub events.
> **Status:** Draft | **Last Updated:** 2026-05-13

---

## 1. Project ↔ Git Repo Linking

### 1.1 New Fields on `Project` Entity

Add the following columns to the `projects` table:

| Column | Type | Required | Default | Description |
|--------|------|----------|---------|-------------|
| `repo_url` | `VARCHAR(512)` | No (null until set) | `null` | Clone URL of the Git repository |
| `repo_provider` | `VARCHAR(20)` | No | `null` | Enum: `GITHUB`, `GITLAB`, `BITBUCKET`, `OTHER` |
| `repo_branch` | `VARCHAR(128)` | No | `main` | Default branch for clone and PR |
| `repo_token_ref` | `VARCHAR(256)` | No | `null` | Reference key to stored credential (e.g., `cred_abc123`) |

### 1.2 Enum `RepoProvider`

```java
public enum RepoProvider {
    GITHUB,
    GITLAB,
    BITBUCKET,
    OTHER
}
```

### 1.3 Validation Rules

- `repo_url` **must** be non-empty when `repo_provider` is set (i.e., if a provider is specified, a URL is mandatory).
- `repo_url` **must** be a valid HTTPS or SSH URL format.
- `repo_branch` defaults to `main` if not provided.
- `repo_token_ref` is a string reference, not the token value itself. The actual credential is stored in a separate secure vault (see section 5.3).

### 1.4 REST Endpoints

```
GET    /api/projects/{id}/repo   → Returns repo configuration as RepoConfigDTO
PUT    /api/projects/{id}/repo   → Creates/updates repo configuration
```

**RepoConfigDTO:**

```json
{
  "projectId": 1,
  "repoUrl": "https://github.com/org/my-project.git",
  "repoProvider": "GITHUB",
  "repoBranch": "main",
  "repoTokenRef": "cred_abc123"
}
```

**PUT request body** uses the same shape. Validation returns `400 Bad Request` with field-level errors if rules are violated.

### 1.5 Authorization

- Only the **project owner** (FOUNDER) can GET/PUT repo configuration.
- `AuditLogService.log()` is called on every PUT with action `PROJECT_REPO_UPDATE`.

---

## 2. Workspace Preparation

### 2.1 Workspace Directory Layout

When a developer agent triggers with a repo-linked project, the orchestrator prepares a local workspace:

```
/ucto/workspaces/{projectId}/
  ├── source/              # Git clone destination
  │   ├── .git/
  │   └── ...              # Project files
  ├── plans/               # Agent-generated plans
  ├── patches/             # Generated diffs/patches
  └── logs/                # Clone/build/log output
```

### 2.2 Clone Flow

1. `RepoWorkspaceService` receives `agent.developer.trigger` event
2. Checks if workspace exists at `/ucto/workspaces/{projectId}/source/.git`
3. If **not cloned**:
   - Resolves credentials via `repoTokenRef` from secure vault
   - Executes `git clone --branch {branch} {url} /ucto/workspaces/{projectId}/source`
   - Logs success/failure to audit log
4. If **already cloned**:
   - Executes `git fetch origin` + `git reset --hard origin/{branch}`
   - Clears unstaged changes
5. On **failure**: publishes `agent.developer.error` with clone failure details

### 2.3 Token Resolution

```java
public class RepoWorkspaceService {
    
    /**
     * Resolves a credential reference to an actual token/credential.
     * Delegates to a secure CredentialStore (vault, env, or encrypted DB).
     */
    public String resolveToken(String tokenRef) {
        // Lookup tokenRef in credential store
        // Return the raw credential value, or null if not found
    }
    
    /**
     * Clones (or updates) the repository for a given project.
     * Returns the absolute path to the workspace.
     */
    public Path prepareWorkspace(Long projectId, String repoUrl, 
                                  String branch, String tokenRef) {
        // 1. Resolve credentials
        // 2. Build authenticated clone URL (if token provided)
        // 3. Clone or pull
        // 4. Return workspace path
    }
}
```

---

## 3. Event Flow: `agent.developer.trigger` to Concrete Actions

### 3.1 Event Payload Extension

The existing `agent.developer.trigger` payload is extended with a `repoConfig` block:

```json
{
  "eventId": "evt_dev_trigger_001",
  "eventType": "agent.developer.trigger",
  "projectId": "1",
  "agentId": "agent_developer_01",
  "timestamp": "2026-05-13T10:00:00Z",
  "correlationId": "proj_1_run_42",
  "data": {
    "requirementIds": ["req_1", "req_2"],
    "screenIds": ["scr_1", "scr_2"],
    "repoConfig": {
      "repoUrl": "https://github.com/org/my-project.git",
      "repoBranch": "main",
      "repoTokenRef": "cred_abc123"
    },
    "action": "implement_changes",
    "description": "Implement invite endpoint POST /api/projects/{id}/invite"
  }
}
```

### 3.2 Action Types

The `data.action` field determines what the developer agent does:

| Action | Description | Expected Output |
|--------|-------------|-----------------|
| `implement_changes` | Edit files based on requirements/screens | Patch file + updated files list |
| `generate_skeleton` | Generate initial project scaffold | Full project structure |
| `run_tests` | Execute existing test suite | Test results JSON |
| `create_branch` | Create a feature branch from main | Branch name |
| `create_pr` | Push branch and create PR | PR URL |

### 3.3 Pipeline from Trigger to Complete

```
agent.developer.trigger
  ↓
RepoWorkspaceService.prepareWorkspace()   ← Clone/pull repo
  ↓
Developer agent processes requirements/screens
  ↓
Developer agent generates code changes (plan → edit → verify)
  ↓
Developer agent runs tests
  ↓
Developer agent creates branch + commits
  ↓
agent.developer.complete (with code artifacts, test results, branch info)
```

### 3.4 Branch Naming Convention

```
ucto/{projectId}/{correlationId}/{timestamp-short}
```

Example: `ucto/1/proj_1_run_42/20260513`

---

## 4. Integration with Existing Services

### 4.1 `AgentOrchestrationService` Changes

The existing `triggerAgent()` method remains unchanged. The repo-awareness happens at the **event listener level** — in `AgentEventListener.handleAgentComplete()`:

- When `agent.ba.complete` is received with `nextAction: "development"`, the existing code already publishes `agent.developer.trigger`.
- A new `RepoWorkspaceService` component listens for `agent.developer.trigger` events and prepares the workspace **before** the developer agent processes the event.

New Redis topics (added to `subscribeToAgentTopics()`):

```
agent.developer.workspace_ready
agent.developer.workspace_error
```

### 4.2 New Service: `RepoWorkspaceService`

```java
@Service
public class RepoWorkspaceService {
    // Dependencies:
    //   - StringRedisTemplate (for publishing workspace_ready)
    //   - AuditLogService
    //   - CredentialStore (for resolving tokens)
    
    public void prepareWorkspaceFromEvent(Map<String, Object> triggerPayload);
    // 1. Extract repoConfig from data
    // 2. Validate repoConfig
    // 3. Clone/update workspace
    // 4. Publish agent.developer.workspace_ready or .workspace_error
}
```

### 4.3 Audit Logging

Every repo operation is audited:

| Action | Description |
|--------|-------------|
| `REPO_CLONE` | Repository cloned for project |
| `REPO_PULL` | Repository updated (fetch + reset) |
| `REPO_BRANCH_CREATE` | Feature branch created |
| `REPO_PR_CREATE` | Pull request created |
| `REPO_CONFIG_UPDATE` | Repo config changed via PUT endpoint |

---

## 5. Error Handling & Timeouts

### 5.1 Clone/Pull Failures

| Failure | Behavior |
|---------|----------|
| Invalid URL | Publish `agent.developer.workspace_error` immediately |
| Authentication failure | Publish `agent.developer.workspace_error` with credential hint |
| Network timeout | Retry 2x with exponential backoff (5s, 25s), then fail |
| Disk space | Publish error; mark project workspace as `BLOCKED` |
| Branch not found | Fall back to `main`; log warning |

### 5.2 Timeouts

| Operation | Timeout | Behavior |
|-----------|---------|----------|
| Git clone | 120s (large repos) | Publish `agent.developer.workspace_error` |
| Git fetch | 60s | Use existing clone (stale) as fallback |
| Test suite run | 300s | Timeout; mark tests as incomplete |
| Branch + commit | 30s | Retry once; skip commit on failure |

### 5.3 Credential Storage

Credentials referenced by `repoTokenRef` are stored in:

- **MVP**: Encrypted environment variable `REPO_CREDENTIALS_JSON` (JSON map of `cred_ref → token`)
- **Phase 2**: Dedicated credential vault (HashiCorp Vault or AWS Secrets Manager)

---

## 6. Configuration Flags

```properties
# Enable/disable repo-aware developer agent
ucto.agent.developer.repo-aware=true

# Base workspace directory
ucto.agent.developer.workspace-base=/ucto/workspaces

# Git operation timeouts (seconds)
ucto.agent.developer.clone-timeout=120
ucto.agent.developer.fetch-timeout=60
ucto.agent.developer.commit-timeout=30

# Max retries for transient git failures
ucto.agent.developer.git-retries=2
```

---

## 7. Future Extensions

- **Multi-repo projects**: Support multiple repos per project (frontend + backend).
- **SSH key auth**: Support SSH-based clone URLs with key-based auth.
- **Webhook integration**: Listen for GitHub/GitLab push webhooks to auto-sync workspace.
- **PR auto-merge**: Automatically merge PRs when all quality gates pass (see `quality_gates_and_simulation_design.md`).

[← Back to README](README.md) | Related: [agent_orchestration_design.md](agent_orchestration_design.md), [quality_gates_and_simulation_design.md](quality_gates_and_simulation_design.md)
