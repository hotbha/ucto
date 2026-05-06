package com.ucto.backend.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ucto.backend.dto.PasswordResetRequest;
import com.ucto.backend.repository.UserRepository;
import com.ucto.backend.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    // In-memory verification tokens (use Redis in production)
    private static final java.util.Map<String, String> verificationTokens = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, String> passwordResetTokens = new java.util.concurrent.ConcurrentHashMap<>();

    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerification(Authentication auth) {
        String email = (String) auth.getPrincipal();
        String token = UUID.randomUUID().toString();
        verificationTokens.put(token, email);

        // In production: send via Zoho SMTP
        System.out.println("Verification email to " + email + ": " + appBaseUrl + "/api/email/verify?token=" + token);

        auditLogService.logAuthAction(null, "VERIFICATION_EMAIL_SENT",
                "Verification email sent to " + email, "", true);

        return ResponseEntity.ok(Map.of("message", "Verification email sent (check console for debug)"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        String email = verificationTokens.remove(token);
        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
        }

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordResetRequest request,
                                             HttpServletRequest httpRequest) {
        var userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            // Don't reveal whether email exists
            return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent."));
        }

        String token = UUID.randomUUID().toString();
        passwordResetTokens.put(token, request.getEmail());

        // Flutter web will read the token from the URL and call /api/email/reset-password
        System.out.println("Password reset for " + request.getEmail()
                + ": " + appBaseUrl + "/reset-password?token=" + token);

        auditLogService.logAuthAction(userOpt.get().getId(), "PASSWORD_RESET_REQUESTED",
                "Password reset requested", httpRequest.getRemoteAddr(), true);

        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        String email = passwordResetTokens.remove(token);
        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired reset token"));
        }

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        var user = userOpt.get();
        user.setPassword(org.springframework.security.crypto.factory.PasswordEncoderFactories
                .createDelegatingPasswordEncoder().encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
