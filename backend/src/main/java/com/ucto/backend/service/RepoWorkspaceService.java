package com.ucto.backend.service;

import com.ucto.backend.entity.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Manages Git workspace preparation for the repo-aware developer agent.
 *
 * For MVP, shells out to the system `git` command. This is abstracted behind
 * a service interface so the implementation can be swapped for a Java Git
 * library (JGit) or a remote executor in Phase 2.
 *
 * See docs/repo_aware_dev_agent_design.md for full design.
 */
@Service
public class RepoWorkspaceService {

    private static final Logger log = LoggerFactory.getLogger(RepoWorkspaceService.class);

    @Autowired
    private AuditLogService auditLogService;

    @Value("${ucto.agent.developer.workspace-base:/tmp/ucto/workspaces}")
    private String workspaceBase;

    @Value("${ucto.agent.developer.clone-timeout:120}")
    private int cloneTimeoutSeconds;

    @Value("${ucto.agent.developer.fetch-timeout:60}")
    private int fetchTimeoutSeconds;

    @Value("${ucto.agent.developer.clone-retry.max-attempts:3}")
    private int cloneMaxAttempts;

    @Value("${ucto.agent.developer.clone-retry.backoff-ms:5000}")
    private long cloneRetryBackoffMillis;


    /**
     * Returns the workspace directory for a given project.
     */
    public Path getWorkspaceDir(Long projectId) {
        return Paths.get(workspaceBase, String.valueOf(projectId));
    }

    /**
     * Returns the source (clone) directory inside the workspace.
     */
    public Path getSourceDir(Long projectId) {
        return getWorkspaceDir(projectId).resolve("source");
    }

    /**
     * Prepare a workspace for the given project.
     * - If the source directory does not contain a .git folder, clones the repo.
     * - Otherwise, fetches and resets to the configured branch.
     *
     * @param project    the project with repo configuration
     * @param simulation if true, skip real git operations and log simulated actions
     * @return the absolute path to the source directory
     * @throws RepoWorkspaceException if preparation fails
     */
    public Path prepareWorkspace(Project project, boolean simulation) {
        Long projectId = project.getId();
        String repoUrl = project.getRepoUrl();
        String branch = project.getRepoBranch() != null ? project.getRepoBranch() : "main";

        if (repoUrl == null || repoUrl.isBlank()) {
            throw new RepoWorkspaceException("repoUrl is not configured for project " + projectId);
        }

        Path sourceDir = getSourceDir(projectId);
        Path gitDir = sourceDir.resolve(".git");

        try {
            Files.createDirectories(getWorkspaceDir(projectId).resolve("plans"));
            Files.createDirectories(getWorkspaceDir(projectId).resolve("patches"));
            Files.createDirectories(getWorkspaceDir(projectId).resolve("logs"));

            if (simulation) {
                log.info("SIMULATION MODE: Skipping real git operations for project {} branch {}", projectId, branch);
                auditLogService.log(null, null, "REPO_PREPARE_SIMULATED",
                        "Simulated workspace preparation for project " + projectId + " branch " + branch,
                        "", true, true);

                // Create empty source directory so downstream code has a workspace path
                Files.createDirectories(sourceDir);
                return sourceDir;
            }

            if (Files.exists(gitDir)) {
                return pullWorkspace(projectId, repoUrl, branch, sourceDir, gitDir);
            } else {
                return cloneWorkspace(projectId, repoUrl, branch, sourceDir);
            }
        } catch (RepoWorkspaceException e) {
            throw e;
        } catch (Exception e) {
            throw new RepoWorkspaceException("Failed to prepare workspace for project " + projectId + ": " + e.getMessage(), e);
        }
    }


    private Path cloneWorkspace(Long projectId, String repoUrl, String branch, Path sourceDir) {
        log.info("Cloning repo {} branch {} for project {} (max {} attempt(s), {}s timeout each)",
                repoUrl, branch, projectId, cloneMaxAttempts, cloneTimeoutSeconds);

        String lastError = null;
        int attempt = 0;

        while (attempt < cloneMaxAttempts) {
            attempt++;
            log.info("Clone attempt {}/{} for project {}", attempt, cloneMaxAttempts, projectId);

            try {
                // Clean up any partial clone artifacts before retrying
                if (attempt > 1) {
                    deleteDirectory(sourceDir);
                }

                // Ensure parent directory exists
                Files.createDirectories(sourceDir.getParent());

                // Build git clone command
                ProcessBuilder pb = new ProcessBuilder(
                        "git", "clone", "--branch", branch,
                        "--single-branch", repoUrl, sourceDir.toAbsolutePath().toString()
                );
                pb.redirectErrorStream(true);

                Process process = pb.start();
                boolean finished = process.waitFor(cloneTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);

                if (!finished) {
                    process.destroyForcibly();
                    lastError = "Clone timed out after " + cloneTimeoutSeconds + "s";
                    log.warn("Clone attempt {}/{} timed out for project {}", attempt, cloneMaxAttempts, projectId);
                    if (attempt < cloneMaxAttempts) {
                        sleepBeforeRetry(attempt);
                    }
                    continue;
                }

                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    String errorOutput = readStream(process.inputReader());
                    lastError = "Clone failed (exit " + exitCode + "): " + errorOutput;
                    log.warn("Clone attempt {}/{} failed for project {}: {}", attempt, cloneMaxAttempts, projectId, lastError);
                    if (attempt < cloneMaxAttempts) {
                        sleepBeforeRetry(attempt);
                    }
                    continue;
                }

                // Success
                auditLogService.logAuthAction(null, "REPO_CLONE",
                        "Repository cloned for project " + projectId + " from " + repoUrl,
                        "", true);

                log.info("Clone successful for project {} on attempt {}", projectId, attempt);
                return sourceDir;
            } catch (RepoWorkspaceException e) {
                throw e;
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                lastError = e.getMessage();
                log.warn("Clone attempt {}/{} interrupted for project {}: {}", attempt, cloneMaxAttempts, projectId, lastError);
                if (attempt < cloneMaxAttempts) {
                    sleepBeforeRetry(attempt);
                }
            }
        }

        // All attempts exhausted
        auditLogService.logAuthAction(null, "REPO_CLONE_FAILED",
                "Clone failed for project " + projectId + " after " + cloneMaxAttempts + " attempt(s). Last error: " + lastError,
                "", false);
        throw new RepoWorkspaceException("Clone failed for project " + projectId
                + " after " + cloneMaxAttempts + " attempt(s). Last error: " + lastError);
    }

    /** Sleep with exponential-ish backoff (fixed for now). */
    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(cloneRetryBackoffMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /** Recursively delete a directory tree. */
    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (var stream = Files.walk(dir)) {
                stream.sorted(java.util.Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                log.warn("Failed to delete {} during cleanup: {}", p, e.getMessage());
                            }
                        });
            }
        }
    }


    private Path pullWorkspace(Long projectId, String repoUrl, String branch, Path sourceDir, Path gitDir) {
        log.info("Pulling latest for project {} branch {}", projectId, branch);

        try {
            // Fetch latest
            ProcessBuilder fetchPb = new ProcessBuilder("git", "fetch", "origin");
            fetchPb.directory(sourceDir.toFile());
            fetchPb.redirectErrorStream(true);
            Process fetchProcess = fetchPb.start();
            boolean fetchFinished = fetchProcess.waitFor(fetchTimeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);

            if (!fetchFinished) {
                fetchProcess.destroyForcibly();
                log.warn("Fetch timed out for project {}, using existing clone", projectId);
                return sourceDir;
            }

            // Hard reset to branch
            ProcessBuilder resetPb = new ProcessBuilder("git", "reset", "--hard", "origin/" + branch);
            resetPb.directory(sourceDir.toFile());
            resetPb.redirectErrorStream(true);
            Process resetProcess = resetPb.start();
            resetProcess.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);

            // Clean untracked files
            ProcessBuilder cleanPb = new ProcessBuilder("git", "clean", "-fd");
            cleanPb.directory(sourceDir.toFile());
            cleanPb.redirectErrorStream(true);
            Process cleanProcess = cleanPb.start();
            cleanProcess.waitFor(15, java.util.concurrent.TimeUnit.SECONDS);

            auditLogService.logAuthAction(null, "REPO_PULL",
                    "Repository updated for project " + projectId,
                    "", true);

            log.info("Pull successful for project {}", projectId);
            return sourceDir;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Pull failed for project {}, using existing clone", projectId, e);
            return sourceDir;
        }
    }

    /**
     * Resolve a credential reference to an actual token.
     * MVP: looks up from environment variable REPO_CREDENTIALS_JSON.
     * Phase 2: delegate to HashiCorp Vault / AWS Secrets Manager.
     */
    public String resolveToken(String tokenRef) {
        if (tokenRef == null || tokenRef.isBlank()) return null;

        String credsJson = System.getenv("REPO_CREDENTIALS_JSON");
        if (credsJson == null || credsJson.isBlank()) return null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, String> creds = new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}
                    .getClass().newInstance() instanceof Map ? null : null;
            // Simplified MVP parsing — in practice use ObjectMapper
            if (credsJson.contains(tokenRef)) {
                // Very basic extraction: "cred_abc123":"ghp_token123"
                int start = credsJson.indexOf(tokenRef);
                if (start >= 0) {
                    int colon = credsJson.indexOf(':', start);
                    if (colon >= 0) {
                        int quoteStart = credsJson.indexOf('"', colon);
                        int quoteEnd = credsJson.indexOf('"', quoteStart + 1);
                        if (quoteStart >= 0 && quoteEnd > quoteStart) {
                            return credsJson.substring(quoteStart + 1, quoteEnd);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to resolve token ref {}: {}", tokenRef, e.getMessage());
        }
        return null;
    }

    private String readStream(java.io.Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        int n;
        while ((n = reader.read(buf)) > 0) {
            sb.append(buf, 0, n);
        }
        return sb.toString();
    }

    /**
     * Exception thrown when workspace preparation fails.
     */
    public static class RepoWorkspaceException extends RuntimeException {
        public RepoWorkspaceException(String message) {
            super(message);
        }
        public RepoWorkspaceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
