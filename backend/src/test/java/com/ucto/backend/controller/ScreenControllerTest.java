package com.ucto.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.config.TestRedisConfig;
import com.ucto.backend.entity.Project;
import com.ucto.backend.entity.Screen;
import com.ucto.backend.entity.User;
import com.ucto.backend.repository.ProjectRepository;
import com.ucto.backend.repository.ScreenRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class ScreenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String founderToken;
    private Long founderId;
    private Long projectId;

    @BeforeEach
    void setUp() {
        screenRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        User founder = new User();
        founder.setEmail("founder@screens.com");
        founder.setPassword(passwordEncoder.encode("password"));
        founder.setName("Founder");
        founder.setRole("FOUNDER");
        founder = userRepository.save(founder);
        founderId = founder.getId();
        founderToken = "Bearer " + jwtService.generateAccessToken(founderId, founder.getEmail(), founder.getRole());

        Project project = new Project();
        project.setTitle("Screen Test Project");
        project.setOwnerId(founderId);
        project.setStatus("DRAFT");
        project.setTier("FREE");
        project = projectRepository.save(project);
        projectId = project.getId();
    }

    @Test
    void createScreen_ShouldReturn201() throws Exception {
        Map<String, Object> request = Map.of(
                "projectId", projectId,
                "type", "WIREFRAME",
                "storageUrl", "https://storage.example.com/screen1.png",
                "mimeType", "image/png"
        );

        mockMvc.perform(post("/api/screens")
                .header("Authorization", founderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("WIREFRAME"))
                .andExpect(jsonPath("$.revisionCount").value(0));
    }

    @Test
    void listScreensByProject_ShouldReturn200() throws Exception {
        // Create a screen
        Screen screen = new Screen();
        screen.setProjectId(projectId);
        screen.setType("WIREFRAME");
        screen.setStatus("PENDING");
        screen.setStorageUrl("https://storage.example.com/screen1.png");
        screen.setMimeType("image/png");
        screen.setRevisionCount(0);
        screenRepository.save(screen);

        mockMvc.perform(get("/api/screens/project/" + projectId)
                .header("Authorization", founderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").value("WIREFRAME"));
    }

    @Test
    void approveScreen_ShouldReturn200() throws Exception {
        Screen screen = createPendingScreen();

        Map<String, String> request = Map.of("status", "APPROVED");

        mockMvc.perform(put("/api/screens/" + screen.getId() + "/status")
                .header("Authorization", founderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").exists())
                .andExpect(jsonPath("$.approvedAt").exists());
    }

    @Test
    void rejectScreen_ShouldReturn200() throws Exception {
        Screen screen = createPendingScreen();

        Map<String, String> request = Map.of("status", "REJECTED");

        mockMvc.perform(put("/api/screens/" + screen.getId() + "/status")
                .header("Authorization", founderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.revisionCount").value(1));
    }

    @Test
    void requestChangesOnScreen_ShouldReturn200() throws Exception {
        Screen screen = createPendingScreen();

        Map<String, String> request = Map.of(
                "status", "CHANGES_REQUESTED",
                "feedback", "Please fix the layout"
        );

        mockMvc.perform(put("/api/screens/" + screen.getId() + "/status")
                .header("Authorization", founderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHANGES_REQUESTED"))
                .andExpect(jsonPath("$.revisionCount").value(1))
                .andExpect(jsonPath("$.feedback").value("Please fix the layout"));
    }

    @Test
    void approveAlreadyApprovedScreen_ShouldReturn400() throws Exception {
        Screen screen = createPendingScreen();
        screen.setStatus("APPROVED");
        screen.setApprovedBy(founderId);
        screen.setApprovedAt(LocalDateTime.now());
        screenRepository.save(screen);

        Map<String, String> request = Map.of("status", "APPROVED");

        mockMvc.perform(put("/api/screens/" + screen.getId() + "/status")
                .header("Authorization", founderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only PENDING screens can be approved"));
    }

    @Test
    void screenRevisionLimit_ShouldReturn400() throws Exception {
        // Create screen with revisionCount already at 2
        Screen screen = createPendingScreen();
        screen.setRevisionCount(2);
        screenRepository.save(screen);

        Map<String, String> request = Map.of("status", "CHANGES_REQUESTED");

        mockMvc.perform(put("/api/screens/" + screen.getId() + "/status")
                .header("Authorization", founderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Maximum revision limit (3) reached for this screen. Screen must be approved or re-created."));
    }

    @Test
    void invalidStateTransition_ShouldReturn400() throws Exception {
        Screen screen = createPendingScreen();
        screen.setStatus("APPROVED");
        screen.setApprovedBy(founderId);
        screen.setApprovedAt(LocalDateTime.now());
        screenRepository.save(screen);

        // Try to reject an already approved screen
        Map<String, String> request = Map.of("status", "REJECTED");

        mockMvc.perform(put("/api/screens/" + screen.getId() + "/status")
                .header("Authorization", founderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only PENDING screens can be rejected"));
    }

    @Test
    void screenNotFound_ShouldReturn404() throws Exception {
        Map<String, String> request = Map.of("status", "APPROVED");

        mockMvc.perform(put("/api/screens/99999/status")
                .header("Authorization", founderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidStatusValue_ShouldReturn400() throws Exception {
        Screen screen = createPendingScreen();

        Map<String, String> request = Map.of("status", "INVALID_STATUS");

        mockMvc.perform(put("/api/screens/" + screen.getId() + "/status")
                .header("Authorization", founderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid status: INVALID_STATUS. Must be one of: APPROVED, REJECTED, CHANGES_REQUESTED"));
    }

    private Screen createPendingScreen() {
        Screen screen = new Screen();
        screen.setProjectId(projectId);
        screen.setType("WIREFRAME");
        screen.setStatus("PENDING");
        screen.setStorageUrl("https://storage.example.com/screen1.png");
        screen.setMimeType("image/png");
        screen.setRevisionCount(0);
        return screenRepository.save(screen);
    }
}
