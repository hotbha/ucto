package com.ucto.backend.config;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Logging interceptor that captures request/response details for error monitoring.
 * 
 * Provides:
 * - Unique request ID per request for traceability
 * - Request method, URI, and timing logging
 * - Error-level logging for 5xx responses
 * - Warning-level logging for 4xx responses (excluding 401)
 */
@Component
public class SentryLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SentryLoggingInterceptor.class);

    private static final ThreadLocal<Long> requestStartTime = new ThreadLocal<>();
    private static final ThreadLocal<String> requestId = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        requestStartTime.set(System.currentTimeMillis());
        requestId.set(UUID.randomUUID().toString().substring(0, 8));

        log.info("[{}] {} {} from {}",
                requestId.get(),
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        Long startTime = requestStartTime.get();
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        String rid = requestId.get() != null ? requestId.get() : "????";

        int status = response.getStatus();

        if (status >= 500) {
            log.error("[{}] {} {} → {} ({}ms) - SERVER ERROR",
                    rid, request.getMethod(), request.getRequestURI(), status, duration, ex);
        } else if (status >= 400 && status != 401) {
            log.warn("[{}] {} {} → {} ({}ms) - CLIENT ERROR",
                    rid, request.getMethod(), request.getRequestURI(), status, duration);
        } else {
            log.info("[{}] {} {} → {} ({}ms)",
                    rid, request.getMethod(), request.getRequestURI(), status, duration);
        }

        requestStartTime.remove();
        requestId.remove();
    }
}
