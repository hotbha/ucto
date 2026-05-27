package com.ucto.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for Fast2SmsService.
 *
 * Covers:
 * - sendOtp builds correct message and delegates to sendSms
 * - sendSms sends correct HTTP headers (authorization, content-type)
 * - sendSms sends correct JSON body (sender_id, message, language, route, numbers)
 * - sendSms posts to correct base URL
 * - Edge cases: empty phone, special characters in message
 */
@ExtendWith(MockitoExtension.class)
class Fast2SmsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder builder;

    @Captor
    private ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor;

    private Fast2SmsService fast2SmsService;
    private static final String API_KEY = "test-api-key";
    private static final String SENDER_ID = "UCTOTST";
    private static final String BASE_URL = "https://www.fast2sms.com/dev/bulkV2";

    @BeforeEach
    void setUp() {
        when(builder.build()).thenReturn(restTemplate);
        fast2SmsService = new Fast2SmsService(API_KEY, SENDER_ID, BASE_URL, builder);
    }

    // ── sendOtp tests ────────────────────────────────────────────────

    @Test
    void sendOtp_ShouldBuildCorrectMessageAndDelegateToSendSms() {
        fast2SmsService.sendOtp("9876543210", "123456");

        verify(restTemplate).postForEntity(eq(BASE_URL), requestCaptor.capture(), eq(String.class));

        Map<String, Object> body = requestCaptor.getValue().getBody();
        assertNotNull(body);
        assertEquals("Your UCTO verification code is: 123456. Valid for 5 minutes.", body.get("message"));
        assertEquals("9876543210", body.get("numbers"));
    }

    @Test
    void sendOtp_DifferentOtpValues_ShouldUseCorrectCode() {
        fast2SmsService.sendOtp("9876543210", "000000");
        fast2SmsService.sendOtp("9876543211", "999999");

        verify(restTemplate, times(2)).postForEntity(eq(BASE_URL), requestCaptor.capture(), eq(String.class));

        var allBodies = requestCaptor.getAllValues();
        assertTrue(((String) allBodies.get(0).getBody().get("message")).contains("000000"));
        assertTrue(((String) allBodies.get(1).getBody().get("message")).contains("999999"));
    }

    // ── sendSms HTTP headers tests ───────────────────────────────────

    @Test
    void sendSms_ShouldSetAuthorizationHeader() {
        fast2SmsService.sendSms("9876543210", "Test message");

        verify(restTemplate).postForEntity(eq(BASE_URL), requestCaptor.capture(), eq(String.class));

        HttpHeaders headers = requestCaptor.getValue().getHeaders();
        assertEquals(API_KEY, headers.getFirst("authorization"));
    }

    @Test
    void sendSms_ShouldSetContentTypeToJson() {
        fast2SmsService.sendSms("9876543210", "Test message");

        verify(restTemplate).postForEntity(eq(BASE_URL), requestCaptor.capture(), eq(String.class));

        HttpHeaders headers = requestCaptor.getValue().getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
    }

    // ── sendSms request body tests ───────────────────────────────────

    @Test
    void sendSms_ShouldSendAllRequiredFields() {
        fast2SmsService.sendSms("9876543210", "Test message");

        verify(restTemplate).postForEntity(eq(BASE_URL), requestCaptor.capture(), eq(String.class));

        Map<String, Object> body = requestCaptor.getValue().getBody();
        assertNotNull(body);
        assertEquals(SENDER_ID, body.get("sender_id"));
        assertEquals("Test message", body.get("message"));
        assertEquals("english", body.get("language"));
        assertEquals("p", body.get("route"));
        assertEquals("9876543210", body.get("numbers"));
    }

    @Test
    void sendSms_ShouldPostToCorrectUrl() {
        fast2SmsService.sendSms("9876543210", "Test message");

        verify(restTemplate).postForEntity(eq(BASE_URL), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void sendSms_ShouldUseRestTemplatePostForEntity() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(null);

        fast2SmsService.sendSms("9876543210", "Test message");

        verify(restTemplate).postForEntity(eq(BASE_URL), any(HttpEntity.class), eq(String.class));
    }

    // ── Edge cases ───────────────────────────────────────────────────

    @Test
    void sendSms_WithEmptyPhoneNumber_ShouldPassEmptyString() {
        fast2SmsService.sendSms("", "Test message");

        verify(restTemplate).postForEntity(eq(BASE_URL), requestCaptor.capture(), eq(String.class));

        Map<String, Object> body = requestCaptor.getValue().getBody();
        assertNotNull(body);
        assertEquals("", body.get("numbers"));
    }

    @Test
    void sendSms_WithSpecialCharactersInMessage_ShouldPreserveContent() {
        String specialMessage = "Hello & goodbye <test> @ucto! OTP: 123-456";
        fast2SmsService.sendSms("9876543210", specialMessage);

        verify(restTemplate).postForEntity(eq(BASE_URL), requestCaptor.capture(), eq(String.class));

        Map<String, Object> body = requestCaptor.getValue().getBody();
        assertNotNull(body);
        assertEquals(specialMessage, body.get("message"));
    }

    @Test
    void sendSms_WithMultiplePhoneNumbers_ShouldPassConcatenatedNumbers() {
        fast2SmsService.sendSms("9876543210,9876543211", "Bulk message");

        verify(restTemplate).postForEntity(eq(BASE_URL), requestCaptor.capture(), eq(String.class));

        Map<String, Object> body = requestCaptor.getValue().getBody();
        assertNotNull(body);
        assertEquals("9876543210,9876543211", body.get("numbers"));
    }
}
