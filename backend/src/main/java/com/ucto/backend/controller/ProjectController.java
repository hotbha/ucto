package com.ucto.backend.controller;

import com.ucto.backend.dto.RepoConfigDTO;
import com.ucto.backend.dto.RepoValidationException;
import com.ucto.backend.entity.Project;
import com.ucto.backend.service.AuditLogService;
import com.ucto.backend.service.ProjectService;
import com.ucto.backend.service.QualityGateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private QualityGateService qualityGateService;

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Map<String, String> request,
                                            Authentication auth,
                                            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(auth);
            Project project = projectService.createProject(
                    request.get("title"),
                    request.get("description"),
                    userId,
                    request.get("tier")
            );

            auditLogService.log(userId, project.getId(), "PROJECT_CREATE",
                    "Created project: " + project.getTitle(), httpRequest.getRemoteAddr(), true);

            return ResponseEntity.status(HttpStatus.CREATED).body(project);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Project>> getMyProjects(Authentication auth) {
        Long userId = getUserId(auth);
        return ResponseEntity.ok(projectService.getProjectsByOwner(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Map<String, String> request,
                                            Authentication auth,
                                            HttpServletRequest httpRequest) {
        Project project = projectService.getProjectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        Long userId = getUserId(auth);
        if (!project.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only the project owner can update this project"));
        }

        Project updated = projectService.updateProject(
                id, request.get("title"), request.get("description"), request.get("status"));

        auditLogService.log(userId, id, "PROJECT_UPDATE",
                "Updated project: " + updated.getTitle(), httpRequest.getRemoteAddr(), true);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id,
                                            Authentication auth,
                                            HttpServletRequest httpRequest) {
        Project project = projectService.getProjectById(id);
        if (project == null) return ResponseEntity.notFound().build();

        Long userId = getUserId(auth);
        if (!project.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only the project owner can delete this project"));
        }

        projectService.deleteProject(id);
        auditLogService.log(userId, id, "PROJECT_DELETE",
                "Deleted project: " + project.getTitle(), httpRequest.getRemoteAddr(), true);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }

    // ── Repo configuration endpoints ──

    @GetMapping("/{id}/repo")
    public ResponseEntity<?> getRepoConfig(@PathVariable Long id, Authentication auth) {
        Project project = projectService.getProjectById(id);
        if (project == null) return ResponseEntity.notFound().build();
        if (!canAccessProject(project, getUserId(auth)))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));

        RepoConfigDTO config = projectService.getRepoConfig(id);
        if (config == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(config);
    }

    @PutMapping("/{id}/repo")
    public ResponseEntity<?> updateRepoConfig(@PathVariable Long id, @RequestBody RepoConfigDTO config,
                                               Authentication auth, HttpServletRequest httpRequest) {
        Project project = projectService.getProjectById(id);
        if (project == null) return ResponseEntity.notFound().build();

        Long userId = getUserId(auth);
        if (!project.getOwnerId().equals(userId))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only the project owner can update repo configuration"));

        try {
            RepoConfigDTO updated = projectService.updateRepoConfig(id, config);
            if (updated == null) return ResponseEntity.notFound().build();
            auditLogService.log(userId, id, "PROJECT_REPO_UPDATE",
                    "Updated repo config for project " + id, httpRequest.getRemoteAddr(), true);
            return ResponseEntity.ok(updated);
        } catch (RepoValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Quality gate endpoints ──

    @GetMapping("/{id}/branches/{branch}/gate-status")
    public ResponseEntity<?> getGateStatus(@PathVariable Long id, @PathVariable String branch,
                                            Authentication auth) {
        Project project = projectService.getProjectById(id);
        if (project == null) return ResponseEntity.notFound().build();
        if (!canAccessProject(project, getUserId(auth)))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));

        return ResponseEntity.ok(qualityGateService.getGateStatus(id, branch));
    }

    private boolean canAccessProject(Project project, Long userId) {
        if (project.getOwnerId().equals(userId)) return true;
        return projectService.getProjectMembers(project.getId()).stream()
                .anyMatch(m -> m.getUserId().equals(userId));
    }

    private Long getUserId(Authentication auth) {
        if (auth != null && auth.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) auth.getDetails();
            return (Long) details.get("userId");
        }
        throw new RuntimeException("User not authenticated");
    }
}
