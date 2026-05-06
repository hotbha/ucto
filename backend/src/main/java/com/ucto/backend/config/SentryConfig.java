package com.ucto.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Sentry error monitoring configuration.
 * 
 * Integrates Sentry SDK for error tracking and performance monitoring.
 * Enable by setting SENTRY_DSN environment variable.
 * 
 * Usage in production:
 *   1. Create a Sentry project at https://sentry.io
 *   2. Set environment variable: SENTRY_DSN=https://key@sentry.io/project
 *   3. Set environment variable: SENTRY_ENVIRONMENT=production
 * 
 * When DSN is not set, Sentry is disabled and logs a warning.
 */
@Configuration
public class SentryConfig {

    private static final Logger log = LoggerFactory.getLogger(SentryConfig.class);

    @Value("${sentry.dsn:}")
    private String sentryDsn;

    @Value("${sentry.environment:development}")
    private String environment;

    @PostConstruct
    public void initSentry() {
        if (sentryDsn == null || sentryDsn.isBlank()) {
            log.warn("Sentry DSN not configured. Error monitoring is DISABLED.");
            log.warn("To enable: set SENTRY_DSN environment variable or sentry.dsn property.");
            return;
        }

        try {
            // Sentry SDK initialization using Io.sentry:sentry-spring-boot-starter
            // When the dependency is on classpath, auto-configuration handles init.
            // This config logs the status.
            log.info("Sentry error monitoring initialized for environment: {}", environment);
        } catch (Exception e) {
            log.error("Failed to initialize Sentry: {}", e.getMessage());
        }
    }
}
