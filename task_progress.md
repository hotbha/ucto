# Repo-Aware Developer Agent — Phase 1 Implementation

- [x] 1. Create `RepoProvider` enum
- [x] 2. Add repo fields to `Project` entity + JPA migration
- [x] 3. Create `RepoConfigDTO` for API layer
- [x] 4. Add `GET/PUT /api/projects/{id}/repo` endpoints in `ProjectController`
- [x] 5. Add `getRepoConfig`/`updateRepoConfig` in `ProjectService`
- [x] 6. Implement `RepoWorkspaceService` with git clone/pull, workspace lifecycle
- [x] 7. Extend `AgentEventListener` with workspace_ready/workspace_error publishing
- [x] 8. Add unit tests for RepoWorkspaceService, ProjectService repo methods, controller endpoints
- [x] 9. Compile and run `mvn test`
