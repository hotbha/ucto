package com.ucto.backend.controller;

import com.ucto.backend.entity.Project;
import com.ucto.backend.service.AuditLogService;
import com.ucto.backend.service.ProjectService;
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
    public ResponseEntity<?> updateProject(@PathVariable Long id,
                                            @RequestBody Map<String, String> request) {
        Project updated = projectService.updateProject(
                id,
                request.get("title"),
                request.get("description"),
                request.get("status")
        );
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
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
