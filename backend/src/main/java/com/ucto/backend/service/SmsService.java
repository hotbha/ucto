package com.ucto.backend.service;

/**
 * SMS service interface for sending OTP and notification messages.
 * MVP uses console/log fallback; can integrate MSG91, TextLocal, Twilio in production.
 */
public interface SmsService {
    void sendOtp(String phoneNumber, String otp);
    void sendSms(String phoneNumber, String message);
}
