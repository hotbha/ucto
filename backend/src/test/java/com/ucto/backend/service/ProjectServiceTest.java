package com.ucto.backend.service;

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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    void createProject_ShouldSaveProjectAndAddOwnerMember() {
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> {
            Project p = i.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(i -> i.getArgument(0));

        Project project = projectService.createProject("Test Project", "A test", 1L, "FREE");

        assertEquals("Test Project", project.getTitle());
        assertEquals("A test", project.getDescription());
        assertEquals(1L, project.getOwnerId());
        assertEquals("DRAFT", project.getStatus());
        assertEquals("FREE", project.getTier());
        assertNotNull(project.getId());

        verify(projectMemberRepository).save(argThat(m ->
                m.getProjectId().equals(1L) && m.getUserId().equals(1L) && "FOUNDER".equals(m.getRole())
        ));
    }

    @Test
    void createProject_ShouldDefaultTierToFreeWhenNull() {
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> {
            Project p = i.getArgument(0);
            p.setId(2L);
            return p;
        });
        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(i -> i.getArgument(0));

        Project project = projectService.createProject("Title", "Desc", 1L, null);
        assertEquals("FREE", project.getTier());
    }

    @Test
    void getProjectsByOwner_ShouldReturnOwnerProjects() {
        Project p1 = new Project();
        p1.setId(1L);
        p1.setOwnerId(1L);
        Project p2 = new Project();
        p2.setId(2L);
        p2.setOwnerId(1L);

        when(projectRepository.findByOwnerId(1L)).thenReturn(List.of(p1, p2));

        List<Project> projects = projectService.getProjectsByOwner(1L);
        assertEquals(2, projects.size());
    }

    @Test
    void getProjectById_WhenExists_ShouldReturnProject() {
        Project p = new Project();
        p.setId(1L);
        p.setTitle("Existing");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

        Project result = projectService.getProjectById(1L);
        assertNotNull(result);
        assertEquals("Existing", result.getTitle());
    }

    @Test
    void getProjectById_WhenNotExists_ShouldReturnNull() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        assertNull(projectService.getProjectById(999L));
    }

    @Test
    void updateProject_ShouldUpdateOnlyProvidedFields() {
        Project existing = new Project();
        existing.setId(1L);
        existing.setTitle("Old Title");
        existing.setDescription("Old Desc");
        existing.setStatus("DRAFT");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

        Project updated = projectService.updateProject(1L, "New Title", null, "IN_PROGRESS");

        assertEquals("New Title", updated.getTitle());
        assertEquals("Old Desc", updated.getDescription()); // unchanged
        assertEquals("IN_PROGRESS", updated.getStatus());
    }

    @Test
    void updateProject_WhenNotExists_ShouldReturnNull() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        assertNull(projectService.updateProject(999L, "Title", null, null));
    }

    @Test
    void addMember_ShouldSaveMember() {
        projectService.addMember(1L, 2L, "DEVELOPER");

        verify(projectMemberRepository).save(argThat(m ->
                m.getProjectId().equals(1L) && m.getUserId().equals(2L) && "DEVELOPER".equals(m.getRole())
        ));
    }

    @Test
    void deleteProject_ShouldDeleteMembersAndProject() {
        ProjectMember m1 = new ProjectMember();
        m1.setId(1L);

        when(projectMemberRepository.findByProjectId(1L)).thenReturn(List.of(m1));

        projectService.deleteProject(1L);

        verify(projectMemberRepository).delete(m1);
        verify(projectRepository).deleteById(1L);
    }
}
