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

    // ── Repo configuration endpoint tests ──

    @Test
    void getRepoConfig_WhenNotExists_Returns404() throws Exception {
        mockMvc.perform(get("/api/projects/99999/repo")
                        .header("Authorization", founderToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRepoConfig_ValidConfig_ReturnsUpdated() throws Exception {
        Project project = new Project();
        project.setTitle("Repo Test Project");
        project.setOwnerId(founderId);
        project.setStatus("DRAFT");
        project.setTier("FREE");
        project = projectRepository.save(project);

        Map<String, Object> config = Map.of(
                "repoUrl", "https://github.com/org/test-repo.git",
                "repoProvider", "GITHUB",
                "repoBranch", "main",
                "repoTokenRef", "cred_test_001"
        );

        mockMvc.perform(put("/api/projects/" + project.getId() + "/repo")
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repoUrl").value("https://github.com/org/test-repo.git"))
                .andExpect(jsonPath("$.repoProvider").value("GITHUB"))
                .andExpect(jsonPath("$.repoBranch").value("main"))
                .andExpect(jsonPath("$.repoTokenRef").value("cred_test_001"));
    }

    @Test
    void updateRepoConfig_ProviderWithoutUrl_Returns400() throws Exception {
        Project project = new Project();
        project.setTitle("Validation Test");
        project.setOwnerId(founderId);
        project.setStatus("DRAFT");
        project.setTier("FREE");
        project = projectRepository.save(project);

        Map<String, Object> config = Map.of(
                "repoUrl", "",
                "repoProvider", "GITHUB"
        );

        mockMvc.perform(put("/api/projects/" + project.getId() + "/repo")
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("repoUrl must be non-empty when repoProvider is set"));
    }

    @Test
    void updateRepoConfig_InvalidProvider_Returns400() throws Exception {
        Project project = new Project();
        project.setTitle("Provider Validation");
        project.setOwnerId(founderId);
        project.setStatus("DRAFT");
        project.setTier("FREE");
        project = projectRepository.save(project);

        Map<String, Object> config = Map.of(
                "repoUrl", "https://example.com/repo.git",
                "repoProvider", "INVALID_PROVIDER"
        );

        mockMvc.perform(put("/api/projects/" + project.getId() + "/repo")
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Invalid repoProvider")));
    }

    @Test
    void updateRepoConfig_NonOwner_Returns403() throws Exception {
        Project project = new Project();
        project.setTitle("Owner Check");
        project.setOwnerId(founderId); // Not this user — a different one
        project.setStatus("DRAFT");
        project.setTier("FREE");
        project = projectRepository.save(project);

        // Use a different user's token (non-owner)
        User otherUser = new User();
        otherUser.setEmail("other@test.com");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setName("Other User");
        otherUser.setPhone("9999999998");
        otherUser.setRole("USER");
        otherUser = userRepository.save(otherUser);
        String otherToken = jwtService.generateAccessToken(otherUser.getId(), otherUser.getEmail(), otherUser.getRole());

        Map<String, Object> config = Map.of(
                "repoUrl", "https://github.com/org/repo.git",
                "repoProvider", "GITHUB"
        );

        mockMvc.perform(put("/api/projects/" + project.getId() + "/repo")
                        .header("Authorization", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateRepoConfig_ThenGet_ReturnsSameConfig() throws Exception {
        Project project = new Project();
        project.setTitle("Roundtrip Test");
        project.setOwnerId(founderId);
        project.setStatus("DRAFT");
        project.setTier("FREE");
        project = projectRepository.save(project);

        Map<String, Object> config = Map.of(
                "repoUrl", "https://gitlab.com/my-team/app.git",
                "repoProvider", "GITLAB",
                "repoBranch", "develop",
                "repoTokenRef", "cred_gl_002"
        );

        // PUT repo config
        mockMvc.perform(put("/api/projects/" + project.getId() + "/repo")
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk());

        // GET repo config and verify round-trip
        mockMvc.perform(get("/api/projects/" + project.getId() + "/repo")
                        .header("Authorization", founderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repoUrl").value("https://gitlab.com/my-team/app.git"))
                .andExpect(jsonPath("$.repoProvider").value("GITLAB"))
                .andExpect(jsonPath("$.repoBranch").value("develop"))
                .andExpect(jsonPath("$.repoTokenRef").value("cred_gl_002"));
    }
}

