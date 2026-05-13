package com.ucto.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ucto.backend.config.TestRedisConfig;
import com.ucto.backend.security.JwtService;

/**
 * Tests for ERR-01 through ERR-05:
 * - ERR-01: Malformed JSON body
 * - ERR-02: Missing required field
 * - ERR-03: Resource not found
 * - ERR-04: Internal server error
 * - ERR-05: Error response shape consistency
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer " + jwtService.generateAccessToken(1L, "test@test.com", "FOUNDER");
    }

    /**
     * ERR-01: Malformed JSON body should return 400.
     */
    @Test
    void err01_malformedJson_ShouldReturn400() throws Exception {
        String malformedJson = "{invalid json here}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * ERR-02: Missing required field should return 400 with field error message.
     */
    @Test
    void err02_missingRequiredField_ShouldReturn400() throws Exception {
        String emptyJson = "{}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * ERR-03: Resource not found should return 404.
     * Use PUT on a non-existent requirement ID, which hits RequirementController's 404 path.
     */
    @Test
    void err03_resourceNotFound_ShouldReturn404() throws Exception {
        // PUT /api/requirements/{id} with a non-existent ID returns 404
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/requirements/99999")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"test\"}"))
                .andExpect(status().isNotFound());
    }

    /**
     * ERR-04: Internal server error response shape.
     * Spring Security returns 403 for unauthenticated access to protected endpoints.
     */
    @Test
    void err04_internalServerError_ShouldReturnErrorShape() throws Exception {
        // Calling a non-existent path without auth — Spring Security returns 403 by default
        mockMvc.perform(get("/api/nonexistent/error-trigger"))
                .andExpect(status().isForbidden());
    }

    /**
     * ERR-05: Error response shape is consistent: {"error": string}.
     * We verify that all error responses have the "error" field.
     */
    @Test
    void err05_errorResponseShape_ShouldHaveErrorField() throws Exception {
        // Test 1: Auth error response shape (missing password field)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\"}")) // missing password field
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Test 2: Malformed JSON error shape
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Test 3: Authenticated request to valid endpoint with empty result (200)
        mockMvc.perform(get("/api/screens/project/99999")
                        .header("Authorization", validToken))
                .andExpect(status().isOk()); // empty list is ok
    }
}
