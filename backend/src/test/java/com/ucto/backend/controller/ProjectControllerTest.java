package com.ucto.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.config.TestRedisConfig;
import com.ucto.backend.entity.Project;
import com.ucto.backend.entity.User;
import com.ucto.backend.repository.ProjectRepository;
import com.ucto.backend.repository.UserRepository;
import com.ucto.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String founderToken;
    private Long founderId;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        userRepository.deleteAll();

        User founder = new User();
        founder.setEmail("project-test@test.com");
        founder.setPassword(passwordEncoder.encode("password"));
        founder.setName("Project Test Founder");
        founder.setRole("FOUNDER");
        founder = userRepository.save(founder);
        founderId = founder.getId();
        founderToken = "Bearer " + jwtService.generateAccessToken(founderId, founder.getEmail(), founder.getRole());
    }

    @Test
    void getProjects_ShouldReturnProjectList() throws Exception {
        // Create a project
        Project p1 = new Project();
        p1.setTitle("Project 1");
        p1.setOwnerId(founderId);
        p1.setStatus("ACTIVE");
        p1.setTier("FREE");
        projectRepository.save(p1);

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", founderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Project 1"));
    }

    @Test
    void createProject_ShouldReturn201() throws Exception {
        Map<String, String> request = Map.of(
                "title", "New Project",
                "description", "A test project",
                "tier", "FREE"
        );

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Project"));
    }

    @Test
    void getProject_WhenExists_ShouldReturnProject() throws Exception {
        Project project = new Project();
        project.setTitle("Existing Project");
        project.setOwnerId(founderId);
        project.setStatus("DRAFT");
        project.setTier("FREE");
        project = projectRepository.save(project);

        mockMvc.perform(get("/api/projects/" + project.getId())
                        .header("Authorization", founderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Existing Project"));
    }

    @Test
    void getProject_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/projects/99999")
                        .header("Authorization", founderToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProject_ShouldReturn200() throws Exception {
        Project project = new Project();
        project.setTitle("Test Project");
        project.setOwnerId(founderId);
        project.setStatus("DRAFT");
        project.setTier("FREE");
        project = projectRepository.save(project);

        mockMvc.perform(delete("/api/projects/" + project.getId())
                        .header("Authorization", founderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project deleted successfully"));
    }
}
