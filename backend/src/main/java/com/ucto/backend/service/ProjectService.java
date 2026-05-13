package com.ucto.backend.service;

import com.ucto.backend.dto.RepoConfigDTO;
import com.ucto.backend.dto.RepoValidationException;
import com.ucto.backend.entity.Project;
import com.ucto.backend.entity.ProjectMember;
import com.ucto.backend.entity.RepoProvider;
import com.ucto.backend.repository.ProjectMemberRepository;
import com.ucto.backend.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Transactional
    public Project createProject(String title, String description, Long ownerId, String tier) {
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(description);
        project.setOwnerId(ownerId);
        project.setStatus("DRAFT");
        project.setTier(tier != null ? tier : "FREE");
        project = projectRepository.save(project);

        // Add owner as FOUNDER member
        ProjectMember member = new ProjectMember();
        member.setProjectId(project.getId());
        member.setUserId(ownerId);
        member.setRole("FOUNDER");
        projectMemberRepository.save(member);

        return project;
    }

    public List<Project> getProjectsByOwner(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId);
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    public Project updateProject(Long id, String title, String description, String status) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) return null;

        if (title != null) project.setTitle(title);
        if (description != null) project.setDescription(description);
        if (status != null) project.setStatus(status);

        return projectRepository.save(project);
    }

    // ── Repo configuration ──

    /**
     * Returns the repo configuration for a project, or null if none set.
     */
    public RepoConfigDTO getRepoConfig(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return null;

        return new RepoConfigDTO(
                project.getId(),
                project.getRepoUrl(),
                project.getRepoProvider(),
                project.getRepoBranch(),
                project.getRepoTokenRef()
        );
    }

    /**
     * Updates the repo configuration for a project with validation.
     *
     * @throws RepoValidationException if validation fails
     */
    @Transactional
    public RepoConfigDTO updateRepoConfig(Long projectId, RepoConfigDTO config) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return null;

        // Validation: if provider is set, repoUrl must be non-empty
        if (config.getRepoProvider() != null && !config.getRepoProvider().isBlank()) {
            if (config.getRepoUrl() == null || config.getRepoUrl().isBlank()) {
                throw new RepoValidationException(
                        "repoUrl must be non-empty when repoProvider is set");
            }
            // Validate provider enum value
            try {
                RepoProvider.valueOf(config.getRepoProvider().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RepoValidationException(
                        "Invalid repoProvider: " + config.getRepoProvider()
                        + ". Must be one of: GITHUB, GITLAB, BITBUCKET, OTHER");
            }
        }

        project.setRepoUrl(config.getRepoUrl());
        project.setRepoProvider(config.getRepoProvider());
        project.setRepoBranch(config.getRepoBranch() != null ? config.getRepoBranch() : "main");
        project.setRepoTokenRef(config.getRepoTokenRef());

        projectRepository.save(project);

        return new RepoConfigDTO(
                project.getId(),
                project.getRepoUrl(),
                project.getRepoProvider(),
                project.getRepoBranch(),
                project.getRepoTokenRef()
        );
    }

    @Transactional
    public void addMember(Long projectId, Long userId, String role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        projectMemberRepository.save(member);
    }

    public List<ProjectMember> getProjectMembers(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }

    @Transactional
    public void deleteProject(Long id) {
        // Delete all project members first, then the project
        projectMemberRepository.findByProjectId(id)
                .forEach(member -> projectMemberRepository.delete(member));
        projectRepository.deleteById(id);
    }
}
