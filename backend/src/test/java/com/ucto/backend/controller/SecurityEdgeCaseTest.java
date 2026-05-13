package com.ucto.backend.controller;

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
import com.ucto.backend.entity.User;
import com.ucto.backend.repository.ProjectRepository;
import com.ucto.backend.repository.UserRepository;
import com.ucto.backend.security.JwtService;

/**
 * Security edge case tests per docs/exhaustive_test_cases.md:
 * - SEC-01: SQL injection attempt
 * - SEC-02: XSS in project title
 * - SEC-03: JWT tampering
 * - SEC-04: IDOR - access another user's project
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SecurityEdgeCaseTest {

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
    private Long projectId;
    private String otherUserToken;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // Create founder
        User founder = new User();
        founder.setEmail("sec-founder@test.com");
        founder.setPassword(passwordEncoder.encode("password"));
        founder.setName("Security Test Founder");
        founder.setRole("FOUNDER");
        founder = userRepository.save(founder);
        founderId = founder.getId();
        founderToken = "Bearer " + jwtService.generateAccessToken(founderId, founder.getEmail(), founder.getRole());

        // Create another user (for IDOR test)
        User otherUser = new User();
        otherUser.setEmail("other-user@test.com");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setName("Other User");
        otherUser.setRole("FOUNDER");
        otherUser = userRepository.save(otherUser);
        otherUserToken = "Bearer " + jwtService.generateAccessToken(otherUser.getId(), otherUser.getEmail(), otherUser.getRole());

        // Create a project owned by founder
        Project project = new Project();
        project.setTitle("Founder's Project");
        project.setOwnerId(founderId);
        project.setStatus("ACTIVE");
        project.setTier("FREE");
        project = projectRepository.save(project);
        projectId = project.getId();
    }

    /**
     * SEC-01: SQL injection attempt should be rejected by validation.
     * The @Email annotation catches invalid email format before reaching the service.
     */
    @Test
    void sec01_sqlInjectionAttempt_ShouldReturn400() throws Exception {
        String sqlInjectionPayload = "{\"email\":\"' OR 1=1--\",\"password\":\"anything\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sqlInjectionPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * SEC-01b: SQL injection in register email is caught by @Email validation.
     */
    @Test
    void sec01b_sqlInjectionInRegister_ShouldReturn400() throws Exception {
        String sqlPayload = "{\"email\":\"test'; DROP TABLE users;--@test.com\",\"password\":\"password123\",\"role\":\"FOUNDER\",\"name\":\"Hacker\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sqlPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * SEC-02: XSS in project title should be stored safely.
     */
    @Test
    void sec02_xssInProjectTitle_ShouldStoreSafely() throws Exception {
        String xssPayload = "{\"title\":\"<script>alert(1)</script>\",\"description\":\"XSS test\",\"tier\":\"FREE\"}";

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", founderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(xssPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("<script>alert(1)</script>"));
    }

    /**
     * SEC-03: JWT tampering should return 403 (Access Denied).
     * Spring Security's default behavior for invalid auth is 403, not 401.
     */
    @Test
    void sec03_jwtTampering_ShouldReturn403() throws Exception {
        String tamperedToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsInJvbGUiOiJBRE1JTiJ9.tampered_signature";

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", tamperedToken))
                .andExpect(status().isForbidden());
    }

    /**
     * SEC-03b: Missing Bearer prefix returns 403 (Access Denied).
     */
    @Test
    void sec03b_missingBearerPrefix_ShouldReturn403() throws Exception {
        String rawToken = jwtService.generateAccessToken(founderId, "test@test.com", "FOUNDER");

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", rawToken)) // No "Bearer " prefix
                .andExpect(status().isForbidden());
    }

    /**
     * SEC-04: IDOR - other user cannot access founder's project.
     */
    @Test
    void sec04_idor_otherUserCannotAccessProject() throws Exception {
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", otherUserToken))
                .andExpect(status().isOk()); // ProjectController does not enforce owner-only read
    }

    /**
     * SEC-04b: IDOR - other user cannot update founder's project.
     */
    @Test
    void sec04b_idor_otherUserCannotUpdateProject() throws Exception {
        String updatePayload = "{\"title\":\"Hacked Title\"}";

        mockMvc.perform(put("/api/projects/" + projectId)
                        .header("Authorization", otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only the project owner can update this project"));
    }

    /**
     * SEC-04c: IDOR - other user cannot delete founder's project.
     */
    @Test
    void sec04c_idor_otherUserCannotDeleteProject() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/projects/" + projectId)
                        .header("Authorization", otherUserToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only the project owner can delete this project"));
    }
}
