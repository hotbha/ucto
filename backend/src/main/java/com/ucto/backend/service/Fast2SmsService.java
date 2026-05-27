package com.ucto.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Fast2SMS integration for sending OTP and transactional SMS.
 * API Docs: https://docs.fast2sms.com/reference
 */
@Service
@Primary
public class Fast2SmsService implements SmsService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String senderId;
    private final String baseUrl;

    public Fast2SmsService(
            @Value("${fast2sms.api.key}") String apiKey,
            @Value("${fast2sms.sender.id:UCTOAP}") String senderId,
            @Value("${fast2sms.base.url:https://www.fast2sms.com/dev/bulkV2}") String baseUrl,
            RestTemplateBuilder builder) {
        this.apiKey = apiKey;
        this.senderId = senderId;
        this.baseUrl = baseUrl;
        this.restTemplate = builder.build();
    }

    @Override
    public void sendOtp(String phoneNumber, String otp) {
        String message = "Your UCTO verification code is: " + otp + ". Valid for 5 minutes.";
        sendSms(phoneNumber, message);
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", apiKey);

        Map<String, Object> body = Map.of(
            "sender_id", senderId,
            "message", message,
            "language", "english",
            "route", "p",
            "numbers", phoneNumber
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(baseUrl, request, String.class);
    }
}
