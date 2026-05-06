package com.ucto.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.config.TestRedisConfig;
import com.ucto.backend.dto.AuthResponse;
import com.ucto.backend.dto.LoginRequest;
import com.ucto.backend.dto.RegisterRequest;
import com.ucto.backend.service.AuditLogService;
import com.ucto.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuditLogService auditLogService;

    @Test
    void register_ShouldReturn201() throws Exception {
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(1L, "test@test.com", "FOUNDER", "Test");
        AuthResponse mockResponse = new AuthResponse("access", "refresh", userDto);

        when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockResponse);

        RegisterRequest request = new RegisterRequest("test@test.com", "password123", "FOUNDER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.user.email").value("test@test.com"));
    }

    @Test
    void register_WithExistingEmail_ShouldReturn400() throws Exception {
        when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Email already registered"));

        RegisterRequest request = new RegisterRequest("existing@test.com", "password", "FOUNDER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void register_WithInvalidRole_ShouldReturn400() throws Exception {
        String invalidJson = """
                {
                    "email": "test@test.com",
                    "password": "password123",
                    "role": "INVALID_ROLE"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturn200() throws Exception {
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(1L, "test@test.com", "FOUNDER", "Test");
        AuthResponse mockResponse = new AuthResponse("access", "refresh", userDto);

        when(userService.authenticate(anyString(), anyString(), anyString()))
                .thenReturn(mockResponse);

        LoginRequest request = new LoginRequest("test@test.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        when(userService.authenticate(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid email or password"));

        LoginRequest request = new LoginRequest("wrong@test.com", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void oauthLogin_ShouldReturn200() throws Exception {
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(1L, "google_user@example.com", "FOUNDER", "User");
        AuthResponse mockResponse = new AuthResponse("access", "refresh", userDto);

        when(userService.authenticateWithOAuth(anyString(), anyString(), anyString()))
                .thenReturn(mockResponse);

        String oauthJson = """
                {
                    "provider": "google",
                    "token": "google_oauth_token"
                }
                """;

        mockMvc.perform(post("/api/auth/oauth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oauthJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void sendOtp_ShouldReturn200() throws Exception {
        when(userService.sendOtp(anyString(), anyString()))
                .thenReturn("OTP sent successfully");

        String otpJson = """
                {
                    "phoneNumber": "+919876543210"
                }
                """;

        mockMvc.perform(post("/api/auth/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(otpJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));
    }

    @Test
    void verifyOtp_ShouldReturn200() throws Exception {
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(1L, "user@phone.ucto.app", "FOUNDER", null);
        AuthResponse mockResponse = new AuthResponse("access", "refresh", userDto);

        when(userService.verifyOtp(anyString(), anyString(), anyString()))
                .thenReturn(mockResponse);

        String verifyJson = """
                {
                    "phoneNumber": "+919876543210",
                    "otp": "123456"
                }
                """;

        mockMvc.perform(post("/api/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void refreshToken_ShouldReturn200() throws Exception {
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(1L, "test@test.com", "FOUNDER", "Test");
        AuthResponse mockResponse = new AuthResponse("newAccess", "newRefresh", userDto);

        when(userService.refreshToken(anyString()))
                .thenReturn(mockResponse);

        String refreshJson = """
                {
                    "refreshToken": "valid_refresh_token"
                }
                """;

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess"));
    }
}
