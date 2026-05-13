package com.ucto.backend.controller;

import java.util.Map;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.config.TestRedisConfig;
import com.ucto.backend.entity.Project;
import com.ucto.backend.entity.Requirement;
import com.ucto.backend.entity.User;
import com.ucto.backend.repository.ProjectRepository;
import com.ucto.backend.repository.RequirementRepository;
import com.ucto.backend.repository.UserRepository;
import com.ucto.backend.security.JwtService;

/**
 * MockMvc tests for RequirementController.
 *
 * Covers per docs/exhaustive_test_cases.md §4:
 * - REQ-01: Create requirement → 201
 * - REQ-02: List project requirements → 200
 * - REQ-03: Update requirement → 200
 * - REQ-04: BA clarification round increments
 * - REQ-05: Clarification cap at 3 rounds → 400
 * - REQ-06: Status transition DRAFT → CLARIFIED
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RequirementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RequirementRepository requirementRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String founderToken;
    private Long projectId;
    private Long founderId;

    @BeforeEach
    void setUp() {
        requirementRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        User founder = new User();
        founder.setEmail("req-founder@test.com");
        founder.setPassword(passwordEncoder.encode("password"));
        founder.setName("Requirement Test Founder");
        founder.setRole("FOUNDER");
        founder = userRepository.save(founder);
        founderId = founder.getId();
        founderToken = "Bearer " + jwtService.generateAccessToken(founderId, founder.getEmail(), founder.getRole());

        Project project = new Project();
        project.setTitle("Requirement Test Project");
        project.setOwnerId(founderId);
        project.setStatus("DRAFT");
        project.setTier("FREE");
        project = projectRepository.save(project);
        projectId = project.getId();
    }

    @Test
    void createRequirement_ShouldReturn201() throws Exception {
        Map<String, Object> request = Map.of(
                "projectId", projectId,
                "title", "User Authentication",
                "description", "Users should be able to login with email/password"
        );

        mockMvc.perform(post("/api/requirements")
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("User Authentication"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    // ── REQ-02: List project requirements ───────────────────────────

    @Test
    void listRequirements_ShouldReturn200() throws Exception {
        Requirement req = new Requirement();
        req.setProjectId(projectId);
        req.setTitle("Test Requirement");
        req.setDescription("Test description");
        req.setStatus("DRAFT");
        req.setCreatedBy(founderId);
        requirementRepository.save(req);

        mockMvc.perform(get("/api/requirements/project/" + projectId)
                        .header("Authorization", founderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Requirement"));
    }

    // ── REQ-03: Update requirement ──────────────────────────────────

    @Test
    void updateRequirement_ShouldReturn200() throws Exception {
        Requirement req = new Requirement();
        req.setProjectId(projectId);
        req.setTitle("Original Title");
        req.setDescription("Original description");
        req.setStatus("DRAFT");
        req.setCreatedBy(founderId);
        req = requirementRepository.save(req);

        Map<String, String> update = Map.of(
                "title", "Updated Title",
                "description", "Updated description"
        );

        mockMvc.perform(put("/api/requirements/" + req.getId())
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    // ── REQ-04: Clarification round increments ──────────────────────

    @Test
    void clarificationRound_IncrementsOnUpdate() throws Exception {
        Requirement req = new Requirement();
        req.setProjectId(projectId);
        req.setTitle("Clarification Test");
        req.setDescription("Test description");
        req.setStatus("DRAFT");
        req.setClarificationRound(0);
        req.setCreatedBy(founderId);
        req = requirementRepository.save(req);

        Map<String, Object> update = Map.of(
                "title", "Clarification Test",
                "clarificationRound", 1
        );

        mockMvc.perform(put("/api/requirements/" + req.getId())
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clarificationRound").value(1));
    }

    // ── REQ-05: Clarification cap at 3 rounds ───────────────────────

    @Test
    void clarificationRound_CapAt3_ShouldReturn400() throws Exception {
        Requirement req = new Requirement();
        req.setProjectId(projectId);
        req.setTitle("Max Rounds Test");
        req.setDescription("Test");
        req.setStatus("DRAFT");
        req.setClarificationRound(3);
        req.setCreatedBy(founderId);
        req = requirementRepository.save(req);

        Map<String, Object> update = Map.of(
                "title", "Max Rounds Test",
                "clarificationRound", 4
        );

        mockMvc.perform(put("/api/requirements/" + req.getId())
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Maximum clarification rounds (3) reached"));
    }

    // ── REQ-06: Status transition ───────────────────────────────────

    @Test
    void updateStatus_ToClarified_ShouldReturn200() throws Exception {
        Requirement req = new Requirement();
        req.setProjectId(projectId);
        req.setTitle("Status Transition Test");
        req.setDescription("Test");
        req.setStatus("DRAFT");
        req.setCreatedBy(founderId);
        req = requirementRepository.save(req);

        Map<String, String> update = Map.of(
                "title", "Status Transition Test",
                "status", "CLARIFIED"
        );

        mockMvc.perform(put("/api/requirements/" + req.getId())
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLARIFIED"));
    }

    // ── Negative tests ──────────────────────────────────────────────

    @Test
    void createRequirement_WithoutProject_ShouldReturn400() throws Exception {
        Map<String, String> request = Map.of(
                "title", "Orphan Requirement",
                "description", "No project"
        );

        mockMvc.perform(post("/api/requirements")
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequirement_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/requirements/project/99999")
                        .header("Authorization", founderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
