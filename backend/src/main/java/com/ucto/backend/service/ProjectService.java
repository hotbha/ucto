package com.ucto.backend.service;

import com.ucto.backend.entity.Project;
import com.ucto.backend.entity.ProjectMember;
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
}
