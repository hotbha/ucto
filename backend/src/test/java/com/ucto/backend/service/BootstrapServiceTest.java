package com.ucto.backend.service;

import com.ucto.backend.dto.BootstrapResultDTO;
import com.ucto.backend.dto.RepoConfigDTO;
import com.ucto.backend.entity.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BootstrapService.
 * Uses temp directories and mocks — no real network or DB calls.
 */
@ExtendWith(MockitoExtension.class)
class BootstrapServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private AuditLogService auditLogService;

    @Captor
    private ArgumentCaptor<RepoConfigDTO> repoConfigCaptor;

    private BootstrapService bootstrapService;

    @TempDir
    Path tempDir;

    private Path templateDir;

    @BeforeEach
    void setUp() throws IOException {
        bootstrapService = new BootstrapService();
        ReflectionTestUtils.setField(bootstrapService, "projectService", projectService);
        ReflectionTestUtils.setField(bootstrapService, "auditLogService", auditLogService);
        ReflectionTestUtils.setField(bootstrapService, "workspaceBase", tempDir.resolve("skeletons").toString());
        ReflectionTestUtils.setField(bootstrapService, "bootstrapEnabled", true);

        // Create a minimal template structure for testing
        templateDir = tempDir.resolve("templates").resolve("spring-react");
        Files.createDirectories(templateDir.resolve("backend/src/main/java/com/__package__/config"));
        Files.createDirectories(templateDir.resolve("backend/src/main/java/com/__package__/controller"));
        Files.createDirectories(templateDir.resolve("backend/src/main/java/com/__package__/entity"));
        Files.createDirectories(templateDir.resolve("backend/src/main/java/com/__package__/repository"));
        Files.createDirectories(templateDir.resolve("backend/src/main/java/com/__package__/service"));
        Files.createDirectories(templateDir.resolve("backend/src/main/resources/db/migration"));
        Files.createDirectories(templateDir.resolve("backend/src/test/java/com/__package__"));
        Files.createDirectories(templateDir.resolve("frontend/src/api"));
        Files.createDirectories(templateDir.resolve("frontend/src/components"));
        Files.createDirectories(templateDir.resolve("frontend/src/pages"));
        Files.createDirectories(templateDir.resolve("frontend/src/hooks"));
        Files.createDirectories(templateDir.resolve("frontend/src/types"));
        Files.createDirectories(templateDir.resolve("frontend/tests"));

        // Write template files
        Files.writeString(templateDir.resolve("backend/pom.xml"), "<!-- __project_slug__ | __project_title__ -->");
        Files.writeString(templateDir.resolve("backend/src/main/resources/application.properties"), "app.name=__project_slug__");
        Files.writeString(templateDir.resolve("backend/src/main/java/com/__package__/__AppName__Application.java"),
                "package com.__package__;\npublic class __AppName__Application {}");
        Files.writeString(templateDir.resolve("backend/src/main/java/com/__package__/controller/HealthController.java"),
                "package com.__package__.controller;\n// Health check");
        Files.writeString(templateDir.resolve("backend/src/main/java/com/__package__/entity/SampleEntity.java"),
                "package com.__package__.entity;\n// Sample entity");
        Files.writeString(templateDir.resolve("backend/src/main/java/com/__package__/repository/SampleEntityRepository.java"),
                "package com.__package__.repository;\n// Repository");
        Files.writeString(templateDir.resolve("backend/src/main/java/com/__package__/service/SampleEntityService.java"),
                "package com.__package__.service;\n// Service");
        Files.writeString(templateDir.resolve("backend/src/main/resources/db/migration/V1__init.sql"), "-- __project_slug__ migration");
        Files.writeString(templateDir.resolve("backend/src/test/java/com/__package__/__AppName__ApplicationTests.java"),
                "package com.__package__;\n// Test");
        Files.writeString(templateDir.resolve("frontend/package.json"), "{\"name\": \"__project_slug__\"}");
        Files.writeString(templateDir.resolve("frontend/src/App.tsx"), "// __project_title__");
        Files.writeString(templateDir.resolve("frontend/src/api/client.ts"), "// api client");
        Files.writeString(templateDir.resolve("frontend/src/components/Layout.tsx"), "// Layout");
        Files.writeString(templateDir.resolve("frontend/src/pages/Home.tsx"), "// Home");
        Files.writeString(templateDir.resolve("frontend/src/hooks/useApi.ts"), "// useApi");
        Files.writeString(templateDir.resolve("frontend/src/types/index.ts"), "// types");
        Files.writeString(templateDir.resolve("frontend/tests/App.test.tsx"), "// test");

        // Override getTemplateDir() to use our test templates
        ReflectionTestUtils.setField(bootstrapService, "templateDirOverride", templateDir);

    }

    @Test
    void testBootstrap_createsProjectAndWorkspace() {
        // Mock project creation
        Project project = new Project();
        project.setId(1L);
        project.setTitle("Task Manager");
        project.setOwnerId(42L);
        project.setStatus("DRAFT");
        project.setTier("FREE");
        // extractTitle() returns full prompt (58 chars, under 60-char limit), extractDescription() falls back to full prompt
        when(projectService.createProject(eq("Task manager with team collaboration and real-time updates"), eq("Task manager with team collaboration and real-time updates"), eq(42L), eq("FREE"))).thenReturn(project);
        when(projectService.updateRepoConfig(eq(1L), any(RepoConfigDTO.class))).thenReturn(new RepoConfigDTO());

        BootstrapResultDTO result = bootstrapService.bootstrap(
                "Task manager with team collaboration and real-time updates",
                "SPRING_REACT",
                42L);

        assertNotNull(result);
        assertEquals(1L, result.getProjectId());
        assertEquals("Task manager with team collaboration and real-time updates", result.getProjectTitle());
        assertEquals("SPRING_REACT", result.getTargetStack());
        assertEquals("CREATED", result.getStatus());

        // Verify workspace directory exists
        Path workspace = Paths.get(result.getWorkspacePath());
        assertTrue(Files.exists(workspace));
        assertTrue(Files.exists(workspace.resolve("backend")));
        assertTrue(Files.exists(workspace.resolve("frontend")));

        // Verify repo config was set with default values
        verify(projectService).updateRepoConfig(eq(1L), repoConfigCaptor.capture());
        RepoConfigDTO config = repoConfigCaptor.getValue();
        assertEquals("main", config.getRepoBranch());
        assertNull(config.getRepoUrl());
        assertNull(config.getRepoProvider());
    }

    @Test
    void testBootstrap_emptyPrompt_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> bootstrapService.bootstrap("", "SPRING_REACT", 1L));
    }

    @Test
    void testBootstrap_nullPrompt_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> bootstrapService.bootstrap(null, "SPRING_REACT", 1L));
    }

    @Test
    void testBootstrap_unsupportedStack_throwsException() {
        // mock project creation for when titles are extracted
        Project project = new Project();
        project.setId(99L);
        project.setTitle("My project");
        project.setOwnerId(1L);
        when(projectService.createProject(eq("My project"), anyString(), eq(1L), eq("FREE"))).thenReturn(project);

        assertThrows(IllegalArgumentException.class,
                () -> bootstrapService.bootstrap("My project", "UNSUPPORTED_STACK", 1L));
    }

    @Test
    void testExtractTitle_simpleSentence() {
        String title = bootstrapService.extractTitle("Task manager app for teams");
        assertEquals("Task manager app for teams", title);
    }

    @Test
    void testExtractTitle_withPeriod() {
        String title = bootstrapService.extractTitle("Task manager. An app for team collaboration.");
        assertEquals("Task manager", title);
    }

    @Test
    void testExtractTitle_blankReturnsDefault() {
        String title = bootstrapService.extractTitle("   ");
        assertEquals("Untitled Project", title);
    }

    @Test
    void testBackendSkeleton_substitutesPlaceholders() throws IOException {
        Path workspaceDir = tempDir.resolve("backend-test");
        int fileCount = bootstrapService.generateBackendSkeleton(workspaceDir, "my-app", "My App", "A test app");

        assertTrue(fileCount > 0);
        assertTrue(Files.exists(workspaceDir.resolve("backend/pom.xml")));

        // Verify substitution
        String pomContent = Files.readString(workspaceDir.resolve("backend/pom.xml"));
        assertTrue(pomContent.contains("my-app"));
        assertTrue(pomContent.contains("My App"));

        // Verify package substitution (directory names containing __package__ are copied as-is)
        Path appDir = workspaceDir.resolve("backend/src/main/java/com/__package__");
        assertTrue(Files.exists(appDir), "Package directory should exist at " + appDir);
    }

    @Test
    void testFrontendSkeleton_substitutesPlaceholders() throws IOException {
        Path workspaceDir = tempDir.resolve("frontend-test");
        int fileCount = bootstrapService.generateFrontendSkeleton(workspaceDir, "my-app", "My App");

        assertTrue(fileCount > 0);
        assertTrue(Files.exists(workspaceDir.resolve("frontend/package.json")));

        String pkgContent = Files.readString(workspaceDir.resolve("frontend/package.json"));
        assertTrue(pkgContent.contains("my-app"));
    }

    @Test
    void testCopyDirectory_createsFiles() throws IOException {
        Path source = templateDir.resolve("backend");
        Path target = tempDir.resolve("copy-test");

        List<Path> copied = bootstrapService.copyDirectory(source, target, content -> content);
        assertFalse(copied.isEmpty());
        assertTrue(Files.exists(target.resolve("pom.xml")));
    }
}
