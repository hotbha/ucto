package com.ucto.backend.service;

import com.ucto.backend.dto.AuthResponse;
import com.ucto.backend.entity.User;
import com.ucto.backend.repository.UserRepository;
import com.ucto.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuditLogService auditLogService;

    // In-memory OTP store (in production, use Redis with TTL)
    private final java.util.Map<String, String> otpStore = new java.util.concurrent.ConcurrentHashMap<>();

    public AuthResponse registerUser(String email, String password, String role, String name, String ipAddress) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setName(name);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        auditLogService.logAuthAction(user.getId(), "REGISTER", "User registered with email: " + email, ipAddress, true);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    public AuthResponse authenticate(String email, String password, String ipAddress) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty() || !passwordEncoder.matches(password, userOptional.get().getPassword())) {
            if (userOptional.isPresent()) {
                auditLogService.logAuthAction(userOptional.get().getId(), "LOGIN_FAILED",
                        "Failed login attempt", ipAddress, false);
            }
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOptional.get();

        // If user was created via OAuth without password, password login should fail
        if (user.getPassword() == null) {
            auditLogService.logAuthAction(user.getId(), "LOGIN_FAILED",
                    "Account has no password (OAuth-only)", ipAddress, false);
            throw new RuntimeException("This account uses social login. Please sign in with Google or Facebook.");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        auditLogService.logAuthAction(user.getId(), "LOGIN", "User logged in", ipAddress, true);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    public AuthResponse authenticateWithOAuth(String provider, String oauthToken, String ipAddress) {
        // In production, validate the OAuth token with Google/Facebook API
        // For now, extract email from a mock (in real impl, call Google/Facebook token info endpoint)
        // This is where you'd call Google Token Info or Facebook Debug Token API
        String email = provider + "_user_" + oauthToken.hashCode() + "@example.com";
        String name = "User";
        String socialId = provider + "_" + oauthToken.hashCode();

        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Link social account if not already linked
            if ("google".equals(provider) && user.getGoogleId() == null) {
                user.setGoogleId(socialId);
            } else if ("facebook".equals(provider) && user.getFacebookId() == null) {
                user.setFacebookId(socialId);
            }
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        } else {
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRole("FOUNDER"); // Default role for OAuth users
            user.setEmailVerified(true);
            if ("google".equals(provider)) {
                user.setGoogleId(socialId);
            } else if ("facebook".equals(provider)) {
                user.setFacebookId(socialId);
            }
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        auditLogService.logAuthAction(user.getId(), "OAUTH_LOGIN",
                "User logged in via " + provider, ipAddress, true);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    public String sendOtp(String phoneNumber, String ipAddress) {
        // In production, integrate with Zoho SMS / MSG91 / TextLocal
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(phoneNumber, otp);

        // TODO: Send OTP via SMS gateway
        // smsService.send(phoneNumber, "Your UCTO OTP is: " + otp);

        System.out.println("OTP for " + phoneNumber + ": " + otp); // Debug log

        auditLogService.logAuthAction(null, "OTP_SENT",
                "OTP sent to " + phoneNumber, ipAddress, true);

        return "OTP sent successfully";
    }

    public AuthResponse verifyOtp(String phoneNumber, String otp, String ipAddress) {
        String storedOtp = otpStore.get(phoneNumber);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            auditLogService.logAuthAction(null, "OTP_VERIFY_FAILED",
                    "Invalid OTP for " + phoneNumber, ipAddress, false);
            throw new RuntimeException("Invalid OTP");
        }

        otpStore.remove(phoneNumber);

        Optional<User> existingUser = userRepository.findByPhoneNumber(phoneNumber);
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // Create new user with phone authentication
            user = new User();
            user.setPhoneNumber(phoneNumber);
            user.setEmail(phoneNumber + "@phone.ucto.app");
            user.setRole("FOUNDER");
            user.setEmailVerified(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        auditLogService.logAuthAction(user.getId(), "OTP_LOGIN",
                "User logged in via OTP", ipAddress, true);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    public AuthResponse refreshToken(String refreshTokenStr) {
        if (!jwtService.validateToken(refreshTokenStr)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Long userId = jwtService.getUserIdFromToken(refreshTokenStr);
        String email = jwtService.getEmailFromToken(refreshTokenStr);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(userId, email, user.getRole());
        String newRefreshToken = jwtService.generateRefreshToken(userId, email);

        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getName()
        );
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setEmailVerified(user.isEmailVerified());

        return new AuthResponse(accessToken, refreshToken, userDto);
    }
}
