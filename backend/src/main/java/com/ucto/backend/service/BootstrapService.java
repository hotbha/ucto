package com.ucto.backend.service;

import com.ucto.backend.dto.BootstrapResultDTO;
import com.ucto.backend.entity.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service that implements the "prompt-to-app" bootstrap flow.
 * For MVP, uses internal Java methods to produce a simple spec from a prompt,
 * copies a Spring Boot + React template into a workspace directory,
 * performs basic text substitutions, and configures the project's repo fields.
 *
 * See docs/prompt_to_app_bootstrap_design.md for full design.
 */
@Service
public class BootstrapService {

    private static final Logger log = LoggerFactory.getLogger(BootstrapService.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${ucto.bootstrap.workspace-base:/tmp/ucto/bootstrap-skeletons}")
    private String workspaceBase;

    @Value("${ucto.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    // Injectable for tests; if null, getTemplateDir() auto-detects
    Path templateDirOverride;


    /**
     * Bootstrap a new project from a user prompt.
     *
     * @param prompt      The user's natural-language project description
     * @param targetStack The target stack identifier (default: "SPRING_REACT")
     * @param userId      The authenticated user's ID
     * @return BootstrapResultDTO with project details and workspace info
     */
    public BootstrapResultDTO bootstrap(String prompt, String targetStack, Long userId) {
        if (!bootstrapEnabled) {
            throw new RuntimeException("Bootstrap is disabled");
        }

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }

        String stack = (targetStack != null && !targetStack.isBlank()) ? targetStack : "SPRING_REACT";

        // 1. Parse the prompt to extract project metadata
        String projectTitle = extractTitle(prompt);
        String projectDescription = extractDescription(prompt, projectTitle);

        // 2. Create the Project record
        Project project = projectService.createProject(projectTitle, projectDescription, userId, "FREE");

        // 3. Determine workspace path
        String projectSlug = projectTitle.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (projectSlug.isBlank()) projectSlug = "project-" + project.getId();

        Path workspaceDir = Paths.get(workspaceBase, projectSlug + "-" + project.getId());

        // 4. Generate skeleton from template
        int backendFiles = 0;
        int frontendFiles = 0;

        if ("SPRING_REACT".equalsIgnoreCase(stack)) {
            try {
                backendFiles = generateBackendSkeleton(workspaceDir, projectSlug, projectTitle, projectDescription);
                frontendFiles = generateFrontendSkeleton(workspaceDir, projectSlug, projectTitle);
            } catch (IOException e) {
                log.error("Failed to generate skeleton for project {}", project.getId(), e);
                throw new RuntimeException("Failed to generate project skeleton: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported stack: " + stack + ". Supported: SPRING_REACT");
        }

        // 5. Configure repo fields on the project (placeholder values for now)
        projectService.updateRepoConfig(project.getId(), new com.ucto.backend.dto.RepoConfigDTO(
                project.getId(),
                null,  // repoUrl — will be set when repo is created/pushed
                null,  // repoProvider
                "main",
                null   // repoTokenRef
        ));

        // 6. Audit log
        auditLogService.log(userId, project.getId(), "BOOTSTRAP_PROJECT",
                "Bootstrapped project '" + projectTitle + "' with stack " + stack
                        + ", workspace: " + workspaceDir,
                "", true);

        log.info("Bootstrapped project {} ({}) at {}", project.getId(), projectTitle, workspaceDir);

        return new BootstrapResultDTO(
                project.getId(),
                projectTitle,
                stack,
                workspaceDir.toAbsolutePath().toString(),
                backendFiles,
                frontendFiles,
                "CREATED",
                "Project skeleton generated successfully"
        );
    }

    /**
     * Extract a project title from the user prompt.
     * For MVP, this is a simple heuristic: take the first sentence or first 60 chars.
     * In Phase 2, this would be handled by the BA agent with LLM.
     */
    String extractTitle(String prompt) {
        String cleaned = prompt.trim();
        // Take first sentence up to 60 chars
        int period = cleaned.indexOf('.');
        int newline = cleaned.indexOf('\n');
        int breakPoint = Math.min(
                period > 0 ? period : Integer.MAX_VALUE,
                newline > 0 ? newline : Integer.MAX_VALUE
        );
        String title;
        if (breakPoint < 60) {
            title = cleaned.substring(0, breakPoint).trim();
        } else {
            title = cleaned.substring(0, Math.min(60, cleaned.length())).trim();
        }
        // Remove trailing punctuation
        title = title.replaceAll("[.\\n]+$", "").trim();
        if (title.isBlank()) title = "Untitled Project";
        // Capitalize first letter
        return Character.toUpperCase(title.charAt(0)) + title.substring(1);
    }

    /**
     * Extract a description from the prompt.
     * For MVP, uses the full prompt text minus the title.
     */
    String extractDescription(String prompt, String title) {
        String desc = prompt.trim();
        if (desc.startsWith(title)) {
            desc = desc.substring(title.length()).trim();
        }
        if (desc.isBlank()) desc = prompt.trim();
        return desc;
    }

    /**
     * Generate the backend skeleton from the spring-react template.
     * Copies all template files, performs placeholder substitutions.
     */
    int generateBackendSkeleton(Path workspaceDir, String projectSlug,
                                 String projectTitle, String projectDescription) throws IOException {
        Path backendDir = workspaceDir.resolve("backend");
        Path templateDir = getTemplateDir().resolve("backend");

        if (!Files.exists(templateDir)) {
            throw new IOException("Backend template not found at " + templateDir);
        }

        // Copy template and substitute placeholders
        String appName = toCamelCase(projectSlug) + "App";
        String javaPackage = "com." + projectSlug.replaceAll("-", "");

        List<Path> copiedFiles = copyDirectory(templateDir, backendDir, content ->
                content
                        .replace("__package__", javaPackage)
                        .replace("__AppName__", appName)
                        .replace("__project_slug__", projectSlug)
                        .replace("__project_title__", projectTitle)
                        .replace("__project_description__", projectDescription)
        );

        // Rename placeholder files
        renamePlaceholderFiles(backendDir, "__AppName__", appName);

        log.info("Generated backend with {} files at {}", copiedFiles.size(), backendDir);
        return copiedFiles.size();
    }

    /**
     * Generate the frontend skeleton from the spring-react template.
     */
    int generateFrontendSkeleton(Path workspaceDir, String projectSlug,
                                  String projectTitle) throws IOException {
        Path frontendDir = workspaceDir.resolve("frontend");
        Path templateDir = getTemplateDir().resolve("frontend");

        if (!Files.exists(templateDir)) {
            throw new IOException("Frontend template not found at " + templateDir);
        }

        List<Path> copiedFiles = copyDirectory(templateDir, frontendDir, content ->
                content
                        .replace("__project_slug__", projectSlug)
                        .replace("__project_title__", projectTitle)
        );

        log.info("Generated frontend with {} files at {}", copiedFiles.size(), frontendDir);
        return copiedFiles.size();
    }

    /**
     * Copy a directory recursively, applying a content transformation to each file.
     */
    List<Path> copyDirectory(Path source, Path target, java.util.function.UnaryOperator<String> transformer)
            throws IOException {
        List<Path> copied = new ArrayList<>();

        Files.createDirectories(target);

        try (var walk = Files.walk(source)) {
            List<Path> paths = walk.collect(Collectors.toList());
            for (Path src : paths) {
                Path relative = source.relativize(src);
                Path dest = target.resolve(relative.toString());

                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    // Read content, transform, write
                    String content = Files.readString(src, StandardCharsets.UTF_8);
                    String transformed = transformer.apply(content);
                    Files.createDirectories(dest.getParent());
                    Files.writeString(dest, transformed, StandardCharsets.UTF_8);
                    copied.add(dest);
                }
            }
        }

        return copied;
    }

    /**
     * Rename files containing a placeholder string.
     */
    private void renamePlaceholderFiles(Path dir, String placeholder, String replacement) throws IOException {
        try (var walk = Files.walk(dir)) {
            List<Path> files = walk.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().contains(placeholder))
                    .collect(Collectors.toList());

            for (Path file : files) {
                String newName = file.getFileName().toString().replace(placeholder, replacement);
                Path newPath = file.resolveSibling(newName);
                Files.move(file, newPath);
                log.debug("Renamed {} -> {}", file.getFileName(), newName);
            }
        }
    }

    /**
     * Get the template directory for spring-react stack.
     * Templates are stored in the classpath under templates/spring-react/.
     * For MVP, we also check the filesystem for development convenience.
     */
    Path getTemplateDir() {
        // Use override if set (for tests)
        if (templateDirOverride != null && Files.isDirectory(templateDirOverride)) {
            return templateDirOverride;
        }
        // First try classpath resource

        try {
            var resource = getClass().getClassLoader().getResource("templates/spring-react");
            if (resource != null) {
                // If it's a directory on the filesystem (dev mode)
                String path = resource.toURI().getPath();
                if (path != null) {
                    Path fsPath = Paths.get(path);
                    if (Files.isDirectory(fsPath)) {
                        return fsPath;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not load template from classpath, trying filesystem", e);
        }

        // Fallback: filesystem path (for development)
        Path fsPath = Paths.get("src/main/resources/templates/spring-react");
        if (Files.isDirectory(fsPath)) {
            return fsPath.toAbsolutePath();
        }

        // Last resort: check from backend module root
        fsPath = Paths.get("backend/src/main/resources/templates/spring-react");
        if (Files.isDirectory(fsPath)) {
            return fsPath.toAbsolutePath();
        }

        throw new RuntimeException("Template directory not found: templates/spring-react");
    }

    /**
     * Convert a kebab-case or hyphenated slug to CamelCase.
     */
    private String toCamelCase(String slug) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = true;
        for (char c : slug.toCharArray()) {
            if (c == '-') {
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
