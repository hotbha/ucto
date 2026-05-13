package com.ucto.backend.controller;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucto.backend.config.TestRedisConfig;
import com.ucto.backend.dto.BAChatHistoryResponse;
import com.ucto.backend.dto.BAChatResponse;
import com.ucto.backend.security.JwtService;
import com.ucto.backend.service.AgentOrchestrationService;
import com.ucto.backend.service.BAChatService;

/**
 * MockMvc tests for BAChatController endpoints.
 *
 * Covers per docs/exhaustive_test_cases.md:
 * - AGNT-01: Trigger BA agent (via POST /api/ba/chat)
 * - AGNT-03: Usage limit enforcement → 402
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class BAChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private BAChatService baChatService;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer " + jwtService.generateAccessToken(1L, "test@test.com", "FOUNDER");
    }

    @Test
    void sendMessage_ShouldReturn200() throws Exception {
        BAChatResponse mockResponse = new BAChatResponse();
        mockResponse.setId(1L);
        mockResponse.setUserMessage("Hello");
        mockResponse.setBaResponse("Hi, how can I help with your project?");
        mockResponse.setRoundNumber(1);
        mockResponse.setMessageType("GREETING");
        mockResponse.setClarificationComplete(false);
        mockResponse.setNeedsEscalation(false);

        when(baChatService.processMessage(anyLong(), anyLong(), anyString(), anyString()))
                .thenReturn(mockResponse);

        String requestJson = """
                {
                    "projectId": 10,
                    "message": "Hello"
                }
                """;

        mockMvc.perform(post("/api/ba/chat")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.messageType").value("GREETING"))
                .andExpect(jsonPath("$.roundNumber").value(1))
                .andExpect(jsonPath("$.clarificationComplete").value(false))
                .andExpect(jsonPath("$.needsEscalation").value(false));
    }

    @Test
    void sendMessage_WhenLimitExceeded_ShouldReturn402() throws Exception {
        when(baChatService.processMessage(anyLong(), anyLong(), anyString(), anyString()))
                .thenThrow(new AgentOrchestrationService.AgentRunLimitExceededException("Agent run limit exceeded"));

        String requestJson = """
                {
                    "projectId": 10,
                    "message": "Hello"
                }
                """;

        mockMvc.perform(post("/api/ba/chat")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.error").value("Agent run limit exceeded"));
    }

    @Test
    void getChatHistory_ShouldReturn200() throws Exception {
        BAChatResponse msg = new BAChatResponse();
        msg.setId(1L);
        msg.setUserMessage("Hello");
        msg.setBaResponse("Hi!");
        msg.setRoundNumber(1);
        msg.setMessageType("GREETING");

        BAChatHistoryResponse mockHistory = new BAChatHistoryResponse();
        mockHistory.setMessages(Collections.singletonList(msg));
        mockHistory.setCurrentRound(1);

        when(baChatService.getChatHistory(anyLong(), anyLong()))
                .thenReturn(mockHistory);

        mockMvc.perform(get("/api/ba/chat/10")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages[0].messageType").value("GREETING"))
                .andExpect(jsonPath("$.currentRound").value(1));
    }
}
