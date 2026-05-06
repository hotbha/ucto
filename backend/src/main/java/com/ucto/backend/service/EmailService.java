package com.ucto.backend.service;

/**
 * Email service interface for sending transactional emails.
 * MVP uses Zoho SMTP; can be swapped for SendGrid, SES, etc. in production.
 */
public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);
    void sendEmail(String to, String subject, String body);
}
