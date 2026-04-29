package com.ucto.backend.controller;

import com.ucto.backend.dto.*;
import com.ucto.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                       HttpServletRequest httpRequest) {
        try {
            AuthResponse response = userService.registerUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getRole(),
                    request.getName(),
                    httpRequest.getRemoteAddr()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                    HttpServletRequest httpRequest) {
        try {
            AuthResponse response = userService.authenticate(
                    request.getEmail(),
                    request.getPassword(),
                    httpRequest.getRemoteAddr()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/oauth")
    public ResponseEntity<?> oauthLogin(@Valid @RequestBody OAuthRequest request,
                                         HttpServletRequest httpRequest) {
        try {
            AuthResponse response = userService.authenticateWithOAuth(
                    request.getProvider(),
                    request.getToken(),
                    httpRequest.getRemoteAddr()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpRequest request,
                                      HttpServletRequest httpRequest) {
        try {
            String message = userService.sendOtp(
                    request.getPhoneNumber(),
                    httpRequest.getRemoteAddr()
            );
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyRequest request,
                                        HttpServletRequest httpRequest) {
        try {
            AuthResponse response = userService.verifyOtp(
                    request.getPhoneNumber(),
                    request.getOtp(),
                    httpRequest.getRemoteAddr()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            AuthResponse response = userService.refreshToken(request.get("refreshToken"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
