package com.ucto.backend.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rate limiting filter for auth endpoints to protect against brute force attacks.
 * 
 * Uses an in-memory sliding window counter per IP address.
 * Limits: /api/auth/login, /api/auth/register, /api/auth/otp/* = 10 requests per minute per IP
 * All other auth endpoints = 20 requests per minute per IP
 * 
 * For production, replace with Redis-based rate limiter (Bucket4j or similar).
 */
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    // Rate limit configuration
    private static final int AUTH_WINDOW_SECONDS = 60;
    private static final int AUTH_MAX_REQUESTS = 10;
    private static final int DEFAULT_MAX_REQUESTS = 20;

    // In-memory store: IP -> (windowStartEpoch, count)
    private final Map<String, WindowCounter> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Only apply rate limiting to /api/auth/* endpoints
        if (!path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        int maxRequests = isSensitiveEndpoint(path) ? AUTH_MAX_REQUESTS : DEFAULT_MAX_REQUESTS;

        if (isRateLimited(clientIp, maxRequests)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                "{\"error\":\"Too many requests. Please try again in " + AUTH_WINDOW_SECONDS + " seconds.\"," +
                "\"code\":\"RATE_LIMIT_EXCEEDED\",\"retryAfterSeconds\":" + AUTH_WINDOW_SECONDS + "}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request should be rate limited.
     * Uses sliding window: if current time exceeds window, start new window.
     * Otherwise, increment counter and check if over limit.
     */
    private boolean isRateLimited(String clientIp, int maxRequests) {
        long now = Instant.now().getEpochSecond();
        
        WindowCounter counter = requestCounts.compute(clientIp, (key, existing) -> {
            if (existing == null || now - existing.windowStart >= AUTH_WINDOW_SECONDS) {
                // Start a new window
                return new WindowCounter(now, 1);
            }
            // Increment within existing window
            existing.count++;
            return existing;
        });

        return counter.count > maxRequests;
    }

    /**
     * Sensitive endpoints get stricter limits.
     */
    private boolean isSensitiveEndpoint(String path) {
        return path.equals("/api/auth/login") ||
               path.equals("/api/auth/register") ||
               path.startsWith("/api/auth/otp/");
    }

    /**
     * Extract client IP from request, respecting proxy headers.
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Simple sliding window counter.
     */
    private static class WindowCounter {
        final long windowStart; // epoch seconds
        int count;

        WindowCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
