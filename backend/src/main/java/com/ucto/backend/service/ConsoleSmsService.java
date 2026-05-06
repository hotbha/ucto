package com.ucto.backend.service;

import org.springframework.stereotype.Service;

/**
 * Console/log-based SMS service for MVP.
 * In production, replace with MSG91, TextLocal, or Twilio integration.
 */
@Service
public class ConsoleSmsService implements SmsService {

    @Override
    public void sendOtp(String phoneNumber, String otp) {
        // In production: integrate with SMS gateway (MSG91, TextLocal, Twilio)
        // For now, log to console for development/testing
        String message = "Your UCTO OTP is: " + otp;
        sendSms(phoneNumber, message);
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        System.out.println("=== SMS (console fallback) ===");
        System.out.println("To: " + phoneNumber);
        System.out.println("Message: " + message);
        System.out.println("=== END SMS ===");
        System.out.println("[PRODUCTION] Send via SMS gateway: " + phoneNumber);
    }
}
