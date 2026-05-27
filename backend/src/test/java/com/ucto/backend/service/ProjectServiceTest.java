package com.ucto.backend.service;

import com.ucto.backend.dto.RepoConfigDTO;
import com.ucto.backend.dto.RepoValidationException;
import com.ucto.backend.entity.Project;
import com.ucto.backend.entity.ProjectMember;
import com.ucto.backend.repository.ProjectMemberRepository;
import com.ucto.backend.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProjectService repo configuration methods.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService();
        ReflectionTestUtils.setField(projectService, "projectRepository", projectRepository);
        ReflectionTestUtils.setField(projectService, "projectMemberRepository", projectMemberRepository);
    }

    @Test
    void testGetRepoConfig_projectNotFound_returnsNull() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        RepoConfigDTO config = projectService.getRepoConfig(1L);
        assertNull(config);
    }

    @Test
    void testGetRepoConfig_projectExists_returnsConfig() {
        Project project = createProjectWithRepo(1L, "https://github.com/org/repo.git",
                "GITHUB", "main", "cred_abc123");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        RepoConfigDTO config = projectService.getRepoConfig(1L);
        assertNotNull(config);
        assertEquals(1L, config.getProjectId());
        assertEquals("https://github.com/org/repo.git", config.getRepoUrl());
        assertEquals("GITHUB", config.getRepoProvider());
        assertEquals("main", config.getRepoBranch());
        assertEquals("cred_abc123", config.getRepoTokenRef());
    }

    @Test
    void testUpdateRepoConfig_projectNotFound_returnsNull() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        RepoConfigDTO config = projectService.updateRepoConfig(1L, new RepoConfigDTO());
        assertNull(config);
    }

    @Test
    void testUpdateRepoConfig_validConfig_updatesSuccessfully() {
        Project project = createEmptyProject(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

        RepoConfigDTO input = new RepoConfigDTO();
        input.setProjectId(1L);
        input.setRepoUrl("https://github.com/org/my-project.git");
        input.setRepoProvider("GITHUB");
        input.setRepoBranch("develop");
        input.setRepoTokenRef("cred_xyz789");

        RepoConfigDTO result = projectService.updateRepoConfig(1L, input);
        assertNotNull(result);
        assertEquals("https://github.com/org/my-project.git", result.getRepoUrl());
        assertEquals("GITHUB", result.getRepoProvider());
        assertEquals("develop", result.getRepoBranch());
        assertEquals("cred_xyz789", result.getRepoTokenRef());
    }

    @Test
    void testUpdateRepoConfig_withoutProvider_allowsEmptyUrl() {
        Project project = createEmptyProject(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

        RepoConfigDTO input = new RepoConfigDTO();
        input.setProjectId(1L);
        input.setRepoUrl("");
        input.setRepoProvider("");
        input.setRepoBranch("main");

        // Should succeed because provider is not set
        RepoConfigDTO result = projectService.updateRepoConfig(1L, input);
        assertNotNull(result);
    }

    @Test
    void testUpdateRepoConfig_providerWithoutUrl_throwsValidationException() {
        Project project = createEmptyProject(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        RepoConfigDTO input = new RepoConfigDTO();
        input.setProjectId(1L);
        input.setRepoUrl("");
        input.setRepoProvider("GITHUB");

        assertThrows(RepoValidationException.class,
                () -> projectService.updateRepoConfig(1L, input));
    }

    @Test
    void testUpdateRepoConfig_invalidProvider_throwsValidationException() {
        Project project = createEmptyProject(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        RepoConfigDTO input = new RepoConfigDTO();
        input.setProjectId(1L);
        input.setRepoUrl("https://example.com/repo.git");
        input.setRepoProvider("INVALID_PROVIDER");

        assertThrows(RepoValidationException.class,
                () -> projectService.updateRepoConfig(1L, input));
    }

    @Test
    void testUpdateRepoConfig_defaultsBranchToMain() {
        Project project = createEmptyProject(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

        RepoConfigDTO input = new RepoConfigDTO();
        input.setProjectId(1L);
        input.setRepoUrl("https://github.com/org/repo.git");
        input.setRepoProvider("GITHUB");
        input.setRepoBranch(null); // Not provided

        RepoConfigDTO result = projectService.updateRepoConfig(1L, input);
        assertEquals("main", result.getRepoBranch());
    }

    // ── Helpers ──

    private Project createProjectWithRepo(Long id, String url, String provider,
                                           String branch, String tokenRef) {
        Project p = new Project();
        p.setId(id);
        p.setTitle("Test Project");
        p.setOwnerId(1L);
        p.setStatus("DRAFT");
        p.setTier("FREE");
        p.setRepoUrl(url);
        p.setRepoProvider(provider);
        p.setRepoBranch(branch);
        p.setRepoTokenRef(tokenRef);
        return p;
    }

    private Project createEmptyProject(Long id) {
        Project p = new Project();
        p.setId(id);
        p.setTitle("Test Project");
        p.setOwnerId(1L);
        p.setStatus("DRAFT");
        p.setTier("FREE");
        return p;
    }
}
