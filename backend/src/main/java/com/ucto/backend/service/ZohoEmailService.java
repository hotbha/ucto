package com.ucto.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service implementation using Zoho SMTP.
 * Configure spring.mail.* properties in application.properties.
 */
@Service
public class ZohoEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String appBaseUrl;

    public ZohoEmailService(JavaMailSender mailSender,
                            @Value("${spring.mail.username}") String fromAddress,
                            @Value("${app.base-url:http://localhost:3000}") String appBaseUrl) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify your UCTO account";
        String body = """
            Welcome to UCTO!
            
            Please verify your email address by clicking the link below:
            %s/api/email/verify?token=%s
            
            This link expires in 24 hours.
            
            If you did not create an account, please ignore this email.
            
            - UCTO Team
            """.formatted(appBaseUrl, token);

        sendEmail(to, subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Reset your UCTO password";
        String body = """
            You requested a password reset for your UCTO account.
            
            Click the link below to reset your password:
            %s/reset-password?token=%s
            
            This link expires in 1 hour.
            
            If you did not request a password reset, please ignore this email.
            
            - UCTO Team
            """.formatted(appBaseUrl, token);

        sendEmail(to, subject, body);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email sent to " + to + " with subject: " + subject);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            // Fall back to console log for development
            System.out.println("=== EMAIL (console fallback) ===");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Body: " + body);
            System.out.println("=== END EMAIL ===");
        }
    }
}
